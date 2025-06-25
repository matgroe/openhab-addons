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

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneProject;

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
     * Observes the {@link GiraOneConnectionState} of the {@link GiraOneBridge}
     *
     * @param consumer - The consumer method to receive GiraOneConnectionState Events.
     * @return A {@link Disposable}
     */
    Disposable subscribeOnConnectionState(Consumer<GiraOneConnectionState> consumer);

    /**
     * This function triggers the {@link GiraOneBridge} to lookup all datapoints
     * for the given channelViewId. The concerning values are getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneChannelValue}.
     *
     * @param channel The {@link GiraOneChannel}, a value lookup should be triggered for.
     */
    void lookupGiraOneChannelValues(final GiraOneChannel channel);

    /**
     * Observes all {@link GiraOneChannelValue} for the given channel.
     *
     * @param channel The {@link GiraOneChannel} to observe
     * @param consumer - The consumer method to receive GiraOneConnectionState Events.
     * @return A {@link Disposable}
     */
    Disposable subscribeOnGiraOneChannelValue(final GiraOneChannel channel, Consumer<GiraOneChannelValue> consumer);

    /**
     * Sets the value on a {@link GiraOneDataPoint}. Any change is getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneChannelValue}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param value The new value
     */
    void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, String value);

    /**
     * Sets the value on a {@link GiraOneDataPoint}. Any change is getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneChannelValue}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param value The new value
     */
    void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Float value);

    /**
     * Sets the value on a {@link GiraOneDataPoint}. Any change is getting reported as
     * {@link GiraOneDataPoint} via subscription on {@link GiraOneBridge#subscribeOnGiraOneChannelValue}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param value The new value
     */
    void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Integer value);
}
