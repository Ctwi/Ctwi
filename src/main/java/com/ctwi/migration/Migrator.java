package com.ctwi.migration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Migrator {
    private String basedir;
    private String executor;

    public Migrator(String basedir, String executor){
        this.basedir = basedir;
        this.executor = executor;
    }

    public void execute(String url, String username, String password) throws SQLException,Exception{

        try(var con = DriverManager.getConnection(url, username, password)) {
            createTables(con);

            List<MigrationHistory> histories = fetchMigrationHistories(con);
            Set<String> ignoreSet = histories.stream()
                    .map(MigrationHistory::getName)
                    .collect(Collectors.toSet());

            List<MigrationFile> files = loadMigrationFiles(basedir);
            List<MigrationFile> filteredFiles = files.stream()
                    .filter(f -> !ignoreSet.contains(f.getName()))
                    .toList();
            if (filteredFiles.isEmpty()) return;

            semaphoreLock(con);
            try {
                for (MigrationFile k : filteredFiles) {
                    migrate(con, k.getQueries());
                    insertMigrationHistory(con, k.getName(), k.getQueries());
                }
            } finally {
                semaphoreUnlock(con);
            }
        }
    }

    private void createTables(Connection con) throws SQLException{
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
            System.out.println("-----------");
            System.out.println(q);
            System.out.println("-----------");
            try (var ps = con.prepareStatement(q)) {
                ps.execute();
            }
        }
    }

    private List<MigrationHistory> fetchMigrationHistories(Connection con) throws SQLException {
        String sql = """
                select filename, executed_at from _migrations
                """;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MigrationHistory> buffer = new ArrayList<>();

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

    private List<MigrationFile> loadMigrationFiles(String dir) throws Exception {
        Path path = Paths.get(dir);
        List<MigrationFile> buffer = new ArrayList<>();

        try (Stream<Path> files = Files.list(path)) {
            for (Path f : files.toList()) {
                String name = f.getFileName().toString();
                String queries = Files.readString(f);
                buffer.add(new MigrationFile(name,queries));
            }
        }

        return buffer;

    }

    private void semaphoreLock(Connection con) throws SQLException {
        String sql = """
                INSERT INTO _semaphores (username, description) VALUES ('migration', ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.executor);
            ps.executeUpdate();
        }
    }

    private void migrate(Connection con, String queries) throws SQLException {
        for (String q : queries.split(";")) {
            System.out.println("-----------");
            System.out.println(q);
            System.out.println("-----------");
            try (var ps = con.prepareStatement(q)) {
                ps.execute();
            }
        }
    }

    private void insertMigrationHistory(Connection con, String name, String queries) throws SQLException {
        String sql = """
                insert into _migrations (filename, queries) values (?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
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

