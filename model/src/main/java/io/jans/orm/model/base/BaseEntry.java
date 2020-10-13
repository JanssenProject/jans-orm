/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.orm.model.base;

import io.jans.orm.annotation.DN;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides DN attribute
 *
 * @author Yuriy Movchan Date: 10.07.2010
 */
public class BaseEntry {

    @DN
    private String dn;

    public BaseEntry() {
    }

    public BaseEntry(String dn) {
        super();
        this.dn = dn;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getBaseDn() {
        return dn;
    }

    public void setBaseDn(String dn) {
        this.dn = dn;
    }

    @Override
    public String toString() {
        return String.format("BaseEntry [dn=%s]", dn);
    }

    public static List<String> getDNs(Collection<? extends BaseEntry> collection) {
        return collection.stream().map(BaseEntry::getDn).collect(Collectors.toList());
    }
}
