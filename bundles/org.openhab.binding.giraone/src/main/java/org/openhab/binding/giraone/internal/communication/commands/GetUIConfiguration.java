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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneServerCommand;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketRequest;
import org.openhab.binding.giraone.internal.util.GenericBuilder;

/**
 * {@link GiraOneWebsocketRequest} for reading the current project configuration
 * from Gira One Server.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
@GiraOneServerCommand(name = "GetUIConfiguration", responsePayload = "config")
public class GetUIConfiguration extends GiraOneCommand {
    public static GenericBuilder<GetUIConfiguration> builder() {
        return GenericBuilder.of(GetUIConfiguration::new);
    }

    private boolean urns = true;

    @Nullable
    private String guid = null;

    @Nullable
    private String instanceId = null;

    protected GetUIConfiguration() {
    }

    public boolean withUrns() {
        return urns;
    }

    public void withUrns(boolean urns) {
        this.urns = urns;
    }

    @Nullable
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Nullable
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
