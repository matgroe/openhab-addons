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

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.util.GenericBuilder;

/**
 * {@link ServerCommand} for registering the openhab bridge as
 * client application at Gira One Server.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class RegisterApplication extends ServerCommand {

    private String applicationId = "Gira.UniversalApp";
    private String applicationType = "ui";
    private String instanceId;

    public static GenericBuilder<RegisterApplication> builder() {
        return GenericBuilder.of(RegisterApplication::new);
    }

    protected RegisterApplication() {
        super(GiraOneCommand.RegisterApplication);
        this.instanceId = UUID.randomUUID().toString();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
