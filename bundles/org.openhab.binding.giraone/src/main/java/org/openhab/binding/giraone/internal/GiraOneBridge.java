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
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneValue;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Use the interface {@link GiraOneBridge} to access the GiraOne Bridge.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public interface GiraOneBridge {

    public

    /**
     * Provides the current
     *
     * @return the {@link GiraOneProject}
     */
    GiraOneProject lookupGiraOneProject();

    /**
     * Observes the {@link GiraOneBridgeState} of the {@link GiraOneBridge}
     *
     * @param consumer - The consumer method to receive GiraOneBridgeState Events.
     * @return A {@link Disposable}
     */
    Disposable subscribeOnConnectionState(Consumer<GiraOneBridgeState> consumer);

    /**
     * This function triggers the {@link GiraOneBridge} to lookup all datapoints
     * for the given channel. The concerning values are getting reported as
     * {@link GiraOneValue} via subscription on {@link GiraOneBridge#subscribeOnGiraOneDataPointValues}.
     *
     * @param channel The {@link GiraOneChannel}, a value lookup should be triggered for.
     */
    void lookupGiraOneChannelValues(final GiraOneChannel channel);

    /**
     * Initiates the value lookup for the given {@link GiraOneDataPoint}. The determined value
     * will be available through a registered consumer {@link GiraOneBridge#subscribeOnGiraOneDataPointValues}.
     *
     * @param dataPoint The Datapoint to lookup it's value.
     */
    void lookupGiraOneDatapointValue(GiraOneDataPoint dataPoint);

    /**
     * Observes all {@link GiraOneValue} for the given device urn.
     *
     * @param datapointUrnPattern
     * @param consumer - The consumer method to receive GiraOneBridgeState Events.
     * @return A {@link Disposable}
     */
    Disposable subscribeOnGiraOneDataPointValues(final String datapointUrnPattern, Consumer<GiraOneValue> consumer);

    /**
     * Sets the value on a {@link GiraOneDataPoint}. Any change is getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneDataPointValues}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param value The new value
     */
    void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, String value);

    /**
     * Sets the value on a {@link GiraOneDataPoint}. Any change is getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneDataPointValues}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param value The new value
     */
    void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Float value);

    /**
     * Sets the value on a {@link GiraOneDataPoint}. Any change is getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneDataPointValues}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param value The new value
     */
    void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Integer value);
}
