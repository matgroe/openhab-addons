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
package org.openhab.binding.giraone.internal.typeadapters;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants to name properties within JsonObjects.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault()
abstract class GiraOneJsonPropertyNames {

    private GiraOneJsonPropertyNames() {
    }

    static final String PROPERTY_CHANNEL_VIEW_ID = "channelViewID";
    static final String PROPERTY_URN = "urn";
    static final String PROPERTY_TYPE = "type";
    static final String PROPERTY_CHANNEL_VIEW_URN = "channelViewUrn";
    static final String PROPERTY_CHANNELS = "channels";
    static final String PROPERTY_CHANNEL_TYPE = "channelType";
    static final String PROPERTY_CHANNEL_TYPE_ID = "channelTypeId";
    static final String PROPERTY_DATAPOINTS = "datapoints";
    static final String PROPERTY_DATA_POINTS_CC = "dataPoints";
    static final String PROPERTY_FUNCTION_TYPE = "functionType";
    static final String PROPERTY_CONTENT = "content";
    static final String PROPERTY_NAME = "name";
    static final String PROPERTY_LOCATION = "location";
    static final String PROPERTY_MAINTYPE = "mainType";
    static final String PROPERTY_SUBLOCATIONS = "subLocations";
    static final String PROPERTY_COMPONENTS = "components";
}
