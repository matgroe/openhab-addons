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

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class for Uniform Resource Name (URN).
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneURN {
    public static final GiraOneURN INVALID = GiraOneURN.of("urn:ns:invalid:invalid");

    private static final String DELIMITER = ":";
    private final String[] urnParts;

    /**
     *
     * @param urn The URN String representation. *
     * @return The parsed URN
     */
    public static GiraOneURN of(final String urn) {
        return new GiraOneURN(urn);
    }

    /**
     * @param deviceUrn The device urn String
     * @param resource the URN resource part
     * @return The parsed URN
     */
    public static GiraOneURN of(final String deviceUrn, final String resource) {
        return new GiraOneURN(String.format("%s:%s", deviceUrn, resource));
    }

    /**
     *
     * @param urn The URN String representation. *
     * @return The parsed URN
     */
    public static GiraOneURN of(final GiraOneURN urn, final String resource) {
        return new GiraOneURN(String.format("%s:%s", urn, resource));
    }

    private GiraOneURN(final String urn) {
        this.urnParts = urn.split(DELIMITER);
        if (!"urn".equals(urnParts[0])) {
            throw new IllegalArgumentException("The String '" + urn + "' cannot get parsed as URN");
        }
    }

    private GiraOneURN(final String[] urnParts) {
        this.urnParts = urnParts;
    }

    /**
     * @return Returns the last part of the parsed URN
     */
    public String getResourceName() {
        if (urnParts.length > 0) {
            return urnParts[urnParts.length - 1];
        }
        return toString();
    }

    public GiraOneURN getParent() {
        if (urnParts.length > 1) {
            String[] b = new String[urnParts.length - 1];
            System.arraycopy(urnParts, 0, b, 0, b.length);
            return new GiraOneURN(b);
        }
        return new GiraOneURN(urnParts);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GiraOneURN that = (GiraOneURN) o;
        return Objects.deepEquals(urnParts, that.urnParts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(urnParts);
    }

    @Override
    public String toString() {
        return String.join(DELIMITER, urnParts);
    }
}
