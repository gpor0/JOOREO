package com.github.gpor0.jooreo.test.support;

import com.github.gpor0.jooreo.test.support.tx.TransactionalConnectionProvider;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.ThreadLocalTransactionProvider;

import jakarta.enterprise.inject.Produces;
import java.sql.Connection;

/**
 * Author: gpor0
 */
public class DslProducer {

    public static final ConnectionProvider provider = new TransactionalConnectionProvider();

    @Produces
    public DSLContext produceDsl() {
        try {
            return DSL.using(provider, SQLDialect.H2,
                    new Settings().withExecuteWithOptimisticLocking(true));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Produces
    public Connection produceConnection() {
        try {
            Connection connection = provider.acquire();
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
