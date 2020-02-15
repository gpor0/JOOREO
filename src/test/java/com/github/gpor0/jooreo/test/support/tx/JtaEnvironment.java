package com.github.gpor0.jooreo.test.support.tx;

import com.arjuna.ats.jta.utils.JNDIManager;
import org.jnp.server.NamingBeanImpl;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JtaEnvironment implements BeforeEachCallback, AfterEachCallback {

    private NamingBeanImpl NAMING_BEAN;

    @Override
    public void beforeEach(ExtensionContext e) throws Exception {
        NAMING_BEAN = new NamingBeanImpl();
        NAMING_BEAN.start();

        JNDIManager.bindJTAImplementation();
        TransactionalConnectionProvider.bindDataSource();
    }

    @Override
    public void afterEach(ExtensionContext e) {
        NAMING_BEAN.stop();
    }
}
