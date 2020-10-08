/*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package io.jans.persist.model.base;

import java.io.Serializable;

import io.jans.orm.annotation.DataEntry;

/**
 * Dummy entry
 *
 * @author Yuriy Movchan Date: 07.13.2011
 */
@DataEntry
public class DummyEntry extends BaseEntry implements Serializable {

    private static final long serialVersionUID = -1111582184398161100L;

}
