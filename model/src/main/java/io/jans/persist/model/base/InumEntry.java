/*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package io.jans.persist.model.base;

import io.jans.orm.annotation.AttributeName;
import io.jans.orm.annotation.DataEntry;
import io.jans.orm.annotation.ObjectClass;

/**
 * Provides global inum search ability.
 *
 * @author Oleksiy Tataryn
 */
@DataEntry
@ObjectClass
public class InumEntry extends Entry {

    @AttributeName(ignoreDuringUpdate = true)
    private String inum;

    /**
     * @param inum
     *            the inum to set
     */
    public void setInum(String inum) {
        this.inum = inum;
    }

    /**
     * @return the inum
     */
    public String getInum() {
        return inum;
    }

    @Override
    public String toString() {
        return String.format("Entry [dn=%s, inum=%s]", getDn(), getInum());
    }

}
