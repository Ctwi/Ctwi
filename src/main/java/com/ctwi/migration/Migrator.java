package com.ctwi.migration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Migrator {
    private String basedir; // マイグレーションファイルのベースディレクトリ
    private String executor; // マイグレーションを実行するユーザーの名前

    // コンストラクタ: マイグレーションのベースディレクトリと実行者を初期化
    public Migrator(String basedir, String executor) {
        this.basedir = basedir;
        this.executor = executor;
    }

    // マイグレーションを実行するメインメソッド
    public void execute(String url, String username, String password) throws SQLException, Exception {
        // try-with-resources文を使用してデータベース接続を確立し、自動的にクローズ
        try (var con = DriverManager.getConnection(url, username, password)) {
            createTables(con); // マイグレーションテーブルとセマフォテーブルを作成

            List<MigrationHistory> histories = fetchMigrationHistories(con);
            Set<String> ignoreSet = histories.stream()
                    .map(MigrationHistory::getName)
                    .collect(Collectors.toSet());

            // 過去のマイグレーション履歴を取得
            List<MigrationFile> files = loadMigrationFiles(basedir);
            // 過去のマイグレーションファイル名のセットを作成
            List<MigrationFile> filteredFiles = files.stream()
                    .filter(f -> !ignoreSet.contains(f.getName()))
                    .toList();
            if (filteredFiles.isEmpty()) return;

            semaphoreLock(con);
            try { // フィルタリングされたマイグレーションファイルを順に実行
                for (MigrationFile k : filteredFiles) {
                    migrate(con, k.getQueries());
                    insertMigrationHistory(con, k.getName(), k.getQueries());
                }
            } finally {
                semaphoreUnlock(con);
            }
        }
    }

    private void createTables(Connection con) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS _migrations (
                    filename VARCHAR(255) NOT NULL,
                    queries TEXT NOT NULL,
                    executed_at DATETIME default CURRENT_TIMESTAMP,
                    PRIMARY KEY (filename)
                );
                CREATE TABLE IF NOT EXISTS _semaphores (
                    username VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    executed_at DATETIME default CURRENT_TIMESTAMP,
                    PRIMARY KEY (username)
                )
                """;

        for (String q : sql.split(";")) {
            try (var ps = con.prepareStatement(q)) {
                ps.execute(); // SQLステートメントを実行
            }
        }
    }

    // 過去のマイグレーション履歴をデータベースから取得するメソッド
    private List<MigrationHistory> fetchMigrationHistories(Connection con) throws SQLException {
        // マイグレーション履歴を取得するSQLクエリ
        String sql = """
                select filename, executed_at from _migrations
                """;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MigrationHistory> buffer = new ArrayList<>();

            // 結果セットを反復処理し、マイグレーション履歴オブジェクトを作成
            while (rs.next()) {
                MigrationHistory h = new MigrationHistory(
                        rs.getString("filename"),
                        rs.getTimestamp("executed_at").toLocalDateTime()
                );
                buffer.add(h);
            }

            return buffer;
        }
    }

    // マイグレーションファイルをディレクトリから読み込むメソッド
    private List<MigrationFile> loadMigrationFiles(String dir) throws Exception {
        Path path = Paths.get(dir);
        List<MigrationFile> buffer = new ArrayList<>();

        // ディレクトリ内のファイルをリストアップ
        try (Stream<Path> files = Files.list(path)) {
            for (Path f : files.toList()) {
                String name = f.getFileName().toString();
                String queries = Files.readString(f);
                buffer.add(new MigrationFile(name, queries));
            }
        }

        return buffer;

    }

    private void semaphoreLock(Connection con) throws SQLException {
        String sql = """
                INSERT INTO _semaphores (username, description) VALUES ('migration', ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.executor); // 実行者の名前を設定
            ps.executeUpdate();
        }
    }

    // 指定されたクエリをデータベースで実行するメソッド
    private void migrate(Connection con, String queries) throws SQLException {
        for (String q : queries.split(";")) { //あいうえお
            if (queries.trim().isEmpty()) continue;
            try (var ps = con.prepareStatement(q)) {
                ps.execute();
            }
        }
    }

    // マイグレーション履歴をデータベースに挿入するメソッド
    private void insertMigrationHistory(Connection con, String name, String queries) throws SQLException {
        String sql = """
                insert into _migrations (filename, queries) values (?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name); // ファイル名を設定
            ps.setString(2, queries);
            ps.executeUpdate();
        }
    }

    private void semaphoreUnlock(Connection con) throws SQLException {
        String sql = """
                delete from _semaphores where username = 'migration'
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}

class MigrationHistory {
    private String name;
    private LocalDateTime executedAt;

    public MigrationHistory(String name, LocalDateTime executedAt) {
        this.name = name;
        this.executedAt = executedAt;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
}

class MigrationFile {
    private String name;
    private String queries;

    public MigrationFile(String name, String queries) {
        this.name = name;
        this.queries = queries;
    }

    public String getName() {
        return name;
    }

    public String getQueries() {
        return queries;
    }
}

