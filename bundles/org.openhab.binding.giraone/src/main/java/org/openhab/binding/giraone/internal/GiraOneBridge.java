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
package org.openhab.binding.giraone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.dto.GiraOneChannelDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneProject;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Use the interface {@link GiraOneBridge} to access the GiraOne Bridge.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public interface GiraOneBridge {

    /**
     * Provides the current
     *
     * @return the {@link GiraOneProject}
     */
    GiraOneProject lookupGiraOneProject();

    /**
     * This function triggers the {@link GiraOneBridge} to lookup all datapoints
     * for the given channelViewId. The concerning values are getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneDataPointStates}.
     *
     * @param channelViewId
     */
    void lookupGiraOneChannelDataPointValues(int channelViewId);

    /**
     * Sets the value on a {@link GiraOneDataPoint}. Any change is getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneDataPointStates}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param value The new value
     */
    void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Object value);

    /**
     * Observes the {@link GiraOneBridgeConnectionState} of the {@link GiraOneBridge}
     *
     * @param consumer - The consumer method to receive GiraOneBridgeConnectionState Events.
     * @return A {@link Disposable}
     */
    Disposable subscribeOnConnectionState(Consumer<GiraOneBridgeConnectionState> consumer);

    /**
     * Observes all {@link GiraOneChannelDataPoint} for the given channel.
     *
     * @param channelViewId - The channelViewId to observe.
     * @param consumer - The consumer method to receive GiraOneBridgeConnectionState Events.
     * @return A {@link Disposable}
     */
    Disposable subscribeOnGiraOneDataPointStates(int channelViewId, Consumer<GiraOneChannelDataPoint> consumer);
}
