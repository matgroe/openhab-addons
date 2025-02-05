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
package org.openhab.binding.giraone.internal.communication.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ServerCommand} {@link RegisterApplication}
 * 
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
class RegisterApplicationTest {
    private static final String APP_ID = "APP_ID_123";
    private static final String APP_TYPE = "APP_TYPE";
    private static final String INSTANCE_ID = "InstanceId";

    @Test
    void builder() {
        RegisterApplication cmd = RegisterApplication.builder().with(RegisterApplication::setApplicationId, APP_ID)
                .with(RegisterApplication::setApplicationType, APP_TYPE)
                .with(RegisterApplication::setInstanceId, INSTANCE_ID).build();

        assertEquals(APP_ID, cmd.getApplicationId());
        assertEquals(APP_TYPE, cmd.getApplicationType());
        assertEquals(INSTANCE_ID, cmd.getInstanceId());
    }
}
