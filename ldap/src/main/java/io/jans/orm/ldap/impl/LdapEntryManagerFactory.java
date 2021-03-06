/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.orm.ldap.impl;

import java.util.HashMap;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import io.jans.orm.PersistenceEntryManagerFactory;
import io.jans.orm.exception.operation.ConfigurationException;
import io.jans.orm.ldap.operation.impl.LdapAuthConnectionProvider;
import io.jans.orm.ldap.operation.impl.LdapConnectionProvider;
import io.jans.orm.ldap.operation.impl.LdapOperationServiceImpl;
import io.jans.orm.service.BaseFactoryService;
import io.jans.orm.util.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LDAP Entry Manager Factory
 *
 * @author Yuriy Movchan Date: 02/02/2018
 */
@ApplicationScoped
public class LdapEntryManagerFactory implements PersistenceEntryManagerFactory {

    public static final String PERSISTENCE_TYPE = "ldap";
    public static final String LDAP_DEFAULT_PROPERTIES_FILE = "jans-ldap.properties";

	private static final Logger LOG = LoggerFactory.getLogger(LdapEntryManagerFactory.class);

    @Override
    public String getPersistenceType() {
        return PERSISTENCE_TYPE;
    }

    @Override
    public HashMap<String, String> getConfigurationFileNames() {
    	HashMap<String, String> confs = new HashMap<String, String>();
    	confs.put(PERSISTENCE_TYPE, LDAP_DEFAULT_PROPERTIES_FILE);

    	return confs;
    }

	@Override
    public LdapEntryManager createEntryManager(Properties conf) {
		Properties entryManagerConf = PropertiesHelper.filterProperties(conf, PERSISTENCE_TYPE);

		LdapConnectionProvider connectionProvider = new LdapConnectionProvider(entryManagerConf);
		connectionProvider.create();
        if (!connectionProvider.isCreated()) {
            throw new ConfigurationException(
                    String.format("Failed to create LDAP connection pool! Result code: '%s'", connectionProvider.getCreationResultCode()));
        }
        LOG.debug("Created connectionProvider '{}' with code '{}'", connectionProvider, connectionProvider.getCreationResultCode());

        LdapConnectionProvider bindConnectionProvider = new LdapAuthConnectionProvider(entryManagerConf);
		connectionProvider.create();
        if (!bindConnectionProvider.isCreated()) {
            throw new ConfigurationException(
                    String.format("Failed to create LDAP bind connection pool! Result code: '%s'", bindConnectionProvider.getCreationResultCode()));
        }
        LOG.debug("Created bindConnectionProvider '{}' with code '{}'", bindConnectionProvider, bindConnectionProvider.getCreationResultCode());

        LdapEntryManager ldapEntryManager = new LdapEntryManager(new LdapOperationServiceImpl(connectionProvider, bindConnectionProvider));
        LOG.info("Created LdapEntryManager: {}", ldapEntryManager.getOperationService());

        return ldapEntryManager;
    }

	@Override
	public void initStandalone(BaseFactoryService persistanceFactoryService) {}

}
