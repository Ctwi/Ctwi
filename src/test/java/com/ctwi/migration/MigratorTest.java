package com.ctwi.migration;

import org.junit.jupiter.api.Test;

public class MigratorTest {
    @Test
    void testExecute() throws Exception {
        var m = new Migrator("src/test/resources/Migrator", "");
        m.execute("jdbc:mysql://127.0.0.1:13306/db","user","password");
    }

}

