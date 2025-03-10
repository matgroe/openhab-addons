/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.giraone.internal.types;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class describes an project item within the GiraOne Project. A {@link GiraOneProjectItem} might
 * be a any kind of object which exists in the Gira World. The combination of {@link GiraOneItemMainType}
 * and {@link GiraOneItemSubType} defines the kind of item.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneProjectItem {
    private GiraOneItemMainType mainType;
    private GiraOneItemSubType subType;
    private String name;
    private String urn;
    private Collection<GiraOneProjectItem> children = new ArrayList<>();
    private Collection<GiraOneItemReference> itemReferences = new ArrayList<>();

    public GiraOneItemMainType getMainType() {
        return mainType;
    }

    public GiraOneItemSubType getSubType() {
        return subType;
    }

    public String getName() {
        return name;
    }

    public String getUrn() {
        return urn;
    }

    public Collection<GiraOneProjectItem> getChildren() {
        return children;
    }

    public boolean isReferencing(GiraOneItemReference ref) {
        return this.itemReferences.stream().anyMatch(f -> f.getReferenceId() == ref.getReferenceId());
    }

    public void setMainType(GiraOneItemMainType mainType) {
        this.mainType = mainType;
    }

    public void setSubType(GiraOneItemSubType subType) {
        this.subType = subType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public void setChildren(Collection<GiraOneProjectItem> children) {
        this.children = children;
    }

    public void setItemReferences(Collection<GiraOneItemReference> itemReferences) {
        this.itemReferences = itemReferences;
    }

    public Collection<GiraOneItemReference> getItemReferences() {
        return itemReferences;
    }

    @Override
    public String toString() {
        return String.format("%s(%s):{children=%d, itemReferences=%d}", getClass().getSimpleName(), urn,
                children.size(), itemReferences.size());
    }
}
