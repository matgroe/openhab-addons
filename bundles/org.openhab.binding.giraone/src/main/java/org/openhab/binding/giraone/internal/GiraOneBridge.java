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
import org.openhab.binding.giraone.internal.dto.GiraOneDataPointState;
import org.openhab.binding.giraone.internal.dto.GiraOneProject;

import java.util.Collection;

public interface GiraOneBridge {

    /**
     * Provides the current
     *
     * @return the {@link GiraOneProject}
     */
    GiraOneProject lookupGiraOneProject();

    Disposable subscribeOnConnectionState(Consumer<GiraOneConnectionState> onNextEvent);

    Disposable subscribeOnGiraOneDataPointStates(int channelViewId, Consumer<GiraOneDataPointState> onNext);

}
