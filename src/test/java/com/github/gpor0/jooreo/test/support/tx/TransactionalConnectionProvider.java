package com.github.gpor0.jooreo.test.support.tx;

import com.arjuna.ats.jdbc.TransactionalDriver;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.util.Properties;

public class TransactionalConnectionProvider implements ConnectionProvider {

    public static final String DS_URL = "jdbc:h2:mem:test;INIT=SET SCHEMA PUBLIC;DB_CLOSE_DELAY=-1";

    public static final String DATASOURCE_JNDI = "java:testDS";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

    private final TransactionalDriver transactionalDriver;

    ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public TransactionalConnectionProvider() {
        transactionalDriver = new TransactionalDriver();
    }

    public static void bindDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(DS_URL);
        dataSource.setUser("");
        dataSource.setPassword("");

        try {
            InitialContext initialContext = new InitialContext();
            initialContext.bind(DATASOURCE_JNDI, dataSource);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Connection acquire() throws DataAccessException {
        try {

            Connection connection = connectionThreadLocal.get();
            if (connection != null) {
                return connection;
            }

            Properties properties = new Properties();
            //  properties.setProperty(TransactionalDriver.userName, USERNAME);
            //   properties.setProperty(TransactionalDriver.password, PASSWORD);
            Connection connect = transactionalDriver.connect("jdbc:arjuna:" + DATASOURCE_JNDI, properties);
            connect.setAutoCommit(false);
            connectionThreadLocal.set(connect);

            return connectionThreadLocal.get();
        } catch (Exception e) {
            throw new DataAccessException("acquire error", e);
        }
    }

    @Override
    public void release(Connection connection) throws DataAccessException {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            throw new DataAccessException("release error", e);
        }
    }
}
