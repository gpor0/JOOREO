package com.github.gpor0.jooreo;

import org.h2.tools.RunScript;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SelectTest {

    private static final String URL = "jdbc:h2:mem:";

    private static Connection sqlConnection;
    private static DSLContext dslContext;

    @BeforeClass
    public static void initializeDatabase() throws SQLException {

        sqlConnection = DriverManager.getConnection(URL);
        dslContext = new DefaultDSLContext(sqlConnection, SQLDialect.H2);

        try {
            File script = new File(SelectTest.class.getResource("/setup.sql").getFile());
            RunScript.execute(sqlConnection, new FileReader(script));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("could not initialize with script");
        }
    }

    @Test
    public void selectTest() {

    }

}
