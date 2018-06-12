/*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */

package org.gluu.persist.couchbase.operation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.gluu.persist.couchbase.model.BucketMapping;
import org.gluu.persist.couchbase.model.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.util.StringHelper;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;

/**
 * Perform cluster initialization and open required buckets
 *
 * @author Yuriy Movchan Date: 05/10/2018
 */
// TODO: getBucketMappingByKey
public class CouchbaseConnectionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseConnectionProvider.class);

    private Properties props;

    private String[] servers;
    private String[] buckets;

    private String userName;
    private String userPassword;

    private CouchbaseCluster cluster;
    private int creationResultCode;
    private HashMap<String, BucketMapping> bucketToBaseNameMapping;
    private HashMap<String, BucketMapping> baseNameToBucketMapping;

    private ArrayList<String> binaryAttributes, certificateAttributes;

    protected CouchbaseConnectionProvider() {
    }

    public CouchbaseConnectionProvider(Properties props) {
        this.props = props;
    }

    public void create() {
        try {
            init();
        } catch (Exception ex) {
            this.creationResultCode = ResultCode.OPERATIONS_ERROR_INT_VALUE;

            Properties clonedProperties = (Properties) props.clone();
            if (clonedProperties.getProperty("userName") != null) {
                clonedProperties.setProperty("userPassword", "REDACTED");
            }

            LOG.error("Failed to create connection with properties: '{}'", clonedProperties, ex);
        }
    }

    protected void init() {
        this.servers = StringHelper.split(props.getProperty("servers"), ",");

        this.userName = props.getProperty("userName");
        this.userPassword = props.getProperty("userPassword");

        this.buckets = StringHelper.split(props.getProperty("buckets"), ",");
        this.bucketToBaseNameMapping = new HashMap<String, BucketMapping>();
        this.baseNameToBucketMapping = new HashMap<String, BucketMapping>();

        openWithWaitImpl();
        LOG.info("Opended: '{}' buket with base names: '{}'", bucketToBaseNameMapping.keySet(), baseNameToBucketMapping.keySet());

        this.binaryAttributes = new ArrayList<String>();
        if (props.containsKey("binaryAttributes")) {
            String[] binaryAttrs = StringHelper.split(props.get("binaryAttributes").toString().toLowerCase(), ",");
            this.binaryAttributes.addAll(Arrays.asList(binaryAttrs));
        }
        LOG.debug("Using next binary attributes: '{}'", binaryAttributes);

        this.certificateAttributes = new ArrayList<String>();
        if (props.containsKey("certificateAttributes")) {
            String[] binaryAttrs = StringHelper.split(props.get("certificateAttributes").toString().toLowerCase(), ",");
            this.certificateAttributes.addAll(Arrays.asList(binaryAttrs));
        }
        LOG.debug("Using next binary certificateAttributes: '{}'", certificateAttributes);

        this.creationResultCode = ResultCode.SUCCESS_INT_VALUE;
    }

    private void openWithWaitImpl() {
        String connectionMaxWaitTime = props.getProperty("connection-max-wait-time");
        int connectionMaxWaitTimeSeconds = 30;
        if (StringHelper.isNotEmpty(connectionMaxWaitTime)) {
            connectionMaxWaitTimeSeconds = Integer.parseInt(connectionMaxWaitTime);
        }
        LOG.debug("Using Couchbase connection timeout: '{}'", connectionMaxWaitTimeSeconds);

        CouchbaseException lastException = null;

        int attempt = 0;
        long currentTime = System.currentTimeMillis();
        long maxWaitTime = currentTime + connectionMaxWaitTimeSeconds * 1000;
        do {
            attempt++;
            if (attempt > 0) {
                LOG.info("Attempting to create connection: '{}'", attempt);
            }

            try {
                open();
                break;
            } catch (CouchbaseException ex) {
                lastException = ex;
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                LOG.error("Exception happened in sleep", ex);
                return;
            }
            currentTime = System.currentTimeMillis();
        } while (maxWaitTime > currentTime);

        if (lastException != null) {
            throw lastException;
        }
    }

    private void open() {
        this.cluster = CouchbaseCluster.create(servers);
        cluster.authenticate(userName, userPassword);

        // Open required buckets
        for (String bucketName : buckets) {
            String baseNamesProp = props.getProperty(String.format("bucket.%s.mapping", bucketName), "");
            String[] baseNames = StringHelper.split(baseNamesProp, ",");

            Bucket bucket = cluster.openBucket(bucketName);

            BucketMapping bucketMapping = new BucketMapping(bucketName, bucket);

            // Store in separate map to speed up search by base name
            bucketToBaseNameMapping.put(bucketName, bucketMapping);
            for (String baseName : baseNames) {
                baseNameToBucketMapping.put(baseName, bucketMapping);
            }

            // Create primary index if needed
            bucket.bucketManager().createN1qlPrimaryIndex(true, false);
        }
    }

    public boolean destory() {
        for (BucketMapping bucketMapping : bucketToBaseNameMapping.values()) {
            try {
                bucketMapping.getBucket().close();
            } catch (CouchbaseException ex) {
                LOG.error("Failed to close bucket '{}'", bucketMapping.getBucketName(), ex);

                return false;
            }
        }

        return cluster.disconnect();
    }

    public boolean isConnected() {
        if (cluster == null) {
            return false;
        }

        boolean isConnected = true;
        Statement query = Select.select("1");
        for (BucketMapping bucketMapping : bucketToBaseNameMapping.values()) {
            try {
                Bucket bucket = bucketMapping.getBucket();
                if (bucket.isClosed() || !bucket.query(query).finalSuccess()) {
                    LOG.error("Bucket '{}' is invalid", bucketMapping.getBucketName());
                    isConnected = false;
                    break;
                }
            } catch (CouchbaseException ex) {
                LOG.error("Failed to check bucket", ex);
            }
        }

        return isConnected;
    }

    public BucketMapping getBucketMapping(String baseName) {
        BucketMapping bucketMapping = baseNameToBucketMapping.get(baseName);
        if (bucketMapping == null) {
            return null;
        }

        return bucketMapping;
    }

    public BucketMapping getBucketMappingByKey(String key) {
        // TODO Auto-generated method stub
        return baseNameToBucketMapping.values().iterator().next();
    }

    public int getCreationResultCode() {
        return creationResultCode;
    }

    public boolean isCreated() {
        return ResultCode.SUCCESS_INT_VALUE == creationResultCode;
    }

    public String[] getServers() {
        return servers;
    }

    public ArrayList<String> getBinaryAttributes() {
        return binaryAttributes;
    }

    public ArrayList<String> getCertificateAttributes() {
        return certificateAttributes;
    }

    public boolean isBinaryAttribute(String attributeName) {
        if (StringHelper.isEmpty(attributeName)) {
            return false;
        }

        return binaryAttributes.contains(attributeName.toLowerCase());
    }

    public boolean isCertificateAttribute(String attributeName) {
        if (StringHelper.isEmpty(attributeName)) {
            return false;
        }

        return certificateAttributes.contains(attributeName.toLowerCase());
    }

}