/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.orm.event;

public interface DeleteNotifier {

    void onBeforeRemove(String dn);

    void onAfterRemove(String dn);

}
