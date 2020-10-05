/*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */

package io.jans.persist.model;

/**
 * @author Yuriy Movchan Date: 02/07/2018
 */
public abstract class ProcessBatchOperation<T> implements BatchOperation<T> {

    public boolean collectSearchResult(int size) {
        return false;
    }

}
