/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.orm.sql;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusLogger;

import io.jans.orm.exception.EntryPersistenceException;
import io.jans.orm.model.BatchOperation;
import io.jans.orm.model.DefaultBatchOperation;
import io.jans.orm.model.ProcessBatchOperation;
import io.jans.orm.model.SearchScope;
import io.jans.orm.model.base.CustomAttribute;
import io.jans.orm.search.filter.Filter;
import io.jans.orm.sql.impl.SqlEntryManager;
import io.jans.orm.sql.model.SimpleClient;
import io.jans.orm.sql.model.SimpleSession;
import io.jans.orm.sql.model.SimpleToken;
import io.jans.orm.sql.persistence.SqlSampleEntryManager;

/**
 * @author Yuriy Movchan Date: 01/15/2020
 */
public final class SqlSampleBatchJob {
    private static final Logger LOG;

    static {
        StatusLogger.getLogger().setLevel(Level.OFF);
        LoggingHelper.configureConsoleAppender();
        LOG = Logger.getLogger(SqlSample.class);
    }

    private SqlSampleBatchJob() { }

    public static void main(String[] args) {
        // Prepare sample connection details
        SqlSampleEntryManager sqlSampleEntryManager = new SqlSampleEntryManager();

        // Create SQL entry manager
        final SqlEntryManager sqlEntryManager = sqlSampleEntryManager.createSqlEntryManager();

        BatchOperation<SimpleToken> tokenSQLBatchOperation = new ProcessBatchOperation<SimpleToken>() {
            private int processedCount = 0;

            @Override
            public void performAction(List<SimpleToken> objects) {
                for (SimpleToken simpleTokenSQL : objects) {
                    try {
                        CustomAttribute customAttribute = getUpdatedAttribute(sqlEntryManager, simpleTokenSQL.getDn(), "exp",
                                simpleTokenSQL.getAttribute("exp"));
                        simpleTokenSQL.setCustomAttributes(Arrays.asList(new CustomAttribute[] {customAttribute}));
                        sqlEntryManager.merge(simpleTokenSQL);
                        processedCount++;
                    } catch (EntryPersistenceException ex) {
                        LOG.error("Failed to update entry", ex);
                    }
                }

                LOG.info("Total processed: " + processedCount);
            }
        };

        final Filter filter1 = Filter.createPresenceFilter("exp");
        sqlEntryManager.findEntries("o=jans", SimpleToken.class, filter1, SearchScope.SUB, new String[] {"exp"},
                tokenSQLBatchOperation, 0, 0, 100);

        BatchOperation<SimpleSession> sessionBatchOperation = new ProcessBatchOperation<SimpleSession>() {
            private int processedCount = 0;

            @Override
            public void performAction(List<SimpleSession> objects) {
                for (SimpleSession simpleSession : objects) {
                    try {
                        CustomAttribute customAttribute = getUpdatedAttribute(sqlEntryManager, simpleSession.getDn(), "jansLastAccessTime",
                                simpleSession.getAttribute("jansLastAccessTime"));
                        simpleSession.setCustomAttributes(Arrays.asList(new CustomAttribute[] {customAttribute}));
                        sqlEntryManager.merge(simpleSession);
                        processedCount++;
                    } catch (EntryPersistenceException ex) {
                        LOG.error("Failed to update entry", ex);
                    }
                }

                LOG.info("Total processed: " + processedCount);
            }
        };

        final Filter filter2 = Filter.createPresenceFilter("jansLastAccessTime");
        sqlEntryManager.findEntries("o=jans", SimpleSession.class, filter2, SearchScope.SUB, new String[] {"jansLastAccessTime"},
                sessionBatchOperation, 0, 0, 100);

        BatchOperation<SimpleClient> clientBatchOperation = new ProcessBatchOperation<SimpleClient>() {
            private int processedCount = 0;

            @Override
            public void performAction(List<SimpleClient> objects) {
                for (SimpleClient simpleClient : objects) {
                    processedCount++;
                }

                LOG.info("Total processed: " + processedCount);
            }
        };

        final Filter filter3 = Filter.createPresenceFilter("exp");
        List<SimpleClient> result3 = sqlEntryManager.findEntries("o=jans", SimpleClient.class, filter3, SearchScope.SUB,
                new String[] {"exp"}, clientBatchOperation, 0, 0, 1000);

        LOG.info("Result count (without collecting results): " + result3.size());

        BatchOperation<SimpleClient> clientBatchOperation2 = new DefaultBatchOperation<SimpleClient>() {
            private int processedCount = 0;

            @Override
            public void performAction(List<SimpleClient> objects) {
                for (SimpleClient simpleClient : objects) {
                    processedCount++;
                }

                LOG.info("Total processed: " + processedCount);
            }
        };

        final Filter filter4 = Filter.createPresenceFilter("exp");
        List<SimpleClient> result4 = sqlEntryManager.findEntries("o=jans", SimpleClient.class, filter4, SearchScope.SUB,
                new String[] {"exp"}, clientBatchOperation2, 0, 0, 1000);

        LOG.info("Result count (with collecting results): " + result4.size());
    }

    private static CustomAttribute getUpdatedAttribute(SqlEntryManager sqlEntryManager, String baseDn, String attributeName, String attributeValue) {
        try {
            Calendar calendar = Calendar.getInstance();
            Date jansLastAccessTimeDate = new Date(); //TODO: Fix it StaticUtils.decodeGeneralizedTime(attributeValue);
            calendar.setTime(jansLastAccessTimeDate);
            calendar.add(Calendar.SECOND, -1);

            CustomAttribute customAttribute = new CustomAttribute();
            customAttribute.setName(attributeName);
            customAttribute.setValue(sqlEntryManager.encodeTime(baseDn, calendar.getTime()));
            return customAttribute;
        } catch (Exception ex) {
            LOG.error("Can't parse attribute", ex);
        }
        return null;
    }
}
