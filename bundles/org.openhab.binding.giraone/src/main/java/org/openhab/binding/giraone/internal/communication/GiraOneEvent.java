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
package org.openhab.binding.giraone.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents a system event as received from the Gira One Sever
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({})
public class GiraOneEvent {
    @Nullable
    private String id;

    @Nullable
    private String urn;

    @Nullable
    @SerializedName(value = "new")
    private String newValue;

    @Nullable
    @SerializedName(value = "old")
    private String oldValue;

    @Nullable
    private String newInternal;

    @Nullable
    private String oldInternal;

    @Nullable
    private String state;

    @Nullable
    private String source;

    public String getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewInternal() {
        return newInternal;
    }

    public String getOldInternal() {
        return oldInternal;
    }

    public String getState() {
        return state;
    }

    public String getSource() {
        return source;
    }
}
