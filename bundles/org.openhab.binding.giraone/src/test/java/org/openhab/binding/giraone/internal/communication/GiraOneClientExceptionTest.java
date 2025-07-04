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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link GiraOneClientException}
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({})
public class GiraOneClientExceptionTest {

    @Test
    void testExceptionWithSimpleMessage() {
        GiraOneClientException thrown = assertThrows(GiraOneClientException.class, () -> {
            throw new GiraOneClientException("XXX");
        });
        assertEquals("XXX", thrown.getMessage());
    }

    @Test
    void testExceptionWithMessageAndPlaceholder() {
        GiraOneClientException thrown = assertThrows(GiraOneClientException.class, () -> {
            throw new GiraOneClientException("XXX", "a");
        });
        assertEquals("XXX [\"a\"]", thrown.getMessage());
    }

    @Test
    void testExceptionWithMessageAndMultiplePlaceholders() {
        GiraOneClientException thrown = assertThrows(GiraOneClientException.class, () -> {
            throw new GiraOneClientException("XXX", "a", "b", "c");
        });
        assertEquals("XXX [\"a\"] [\"b\"] [\"c\"]", thrown.getMessage());
    }

    @Test
    void testExceptionWithMessageAndCausingException() {
        GiraOneClientException thrown = assertThrows(GiraOneClientException.class, () -> {
            throw new GiraOneClientException("XC", new IllegalArgumentException("CAUSE"));
        });
        assertEquals("XC [\"CAUSE\"]", thrown.getMessage());
    }
}
