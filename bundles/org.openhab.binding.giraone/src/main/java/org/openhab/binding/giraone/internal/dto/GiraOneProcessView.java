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
package org.openhab.binding.giraone.internal.dto;

import java.util.List;

/**
 * This class represents the current ProcessView with all datapoints and their values.
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneProcessView {

    private List<GiraOneDataPointState> datapoints;

    public List<GiraOneDataPointState> getDatapoints() {
        return datapoints;
    }
}
