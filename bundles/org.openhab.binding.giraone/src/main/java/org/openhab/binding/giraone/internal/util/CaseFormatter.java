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
package org.openhab.binding.giraone.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class with some string case formatting functions.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public abstract class CaseFormatter {

    /**
     * Converts the given input String into it's lower-case-hyphen representation.
     *
     * @param input The String to format
     * @return the lower-case-hyphen formatted input String.
     */
    public static String lowerCaseHyphen(final String input) {
        return input.replaceAll("[a-z]+[0-9]*|[A-Z][a-z]+[0-9]*", "-$0-").replaceFirst("^-+", "")
                .replaceFirst("-+$", "").replaceAll("--+", "-").toLowerCase();
    }
}
