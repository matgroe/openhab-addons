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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.dto.GiraOneDataPointState;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * The {@link GiraOneThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GiraOneThingHandler.class);
    private int channelViewId;
    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob = null;
    private @Nullable GiraOneBridge giraOneBridge;
    private Disposable disposableOnDataPointState = Disposable.empty();

    public GiraOneThingHandler(Thing thing) {
        super(thing);
    }

    private void updateChannelValue() {
    }

    @Override
    public void initialize() {
        logger.debug("initialize {}", getThing().getUID());
        if (getBridge() != null) {
            this.channelViewId = Integer
                    .parseInt(Objects.requireNonNull(getThing().getProperties().get("channelViewId")));
            giraOneBridge = (GiraOneBridge) getBridge().getHandler();

            if (giraOneBridge != null) {
                this.disposableOnDataPointState = giraOneBridge.subscribeOnGiraOneDataPointStates(this.channelViewId, this::onDataPointState);
            }
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        try {
            this.disposableOnDataPointState.dispose();
            super.dispose();
        } finally {
            this.disposableOnDataPointState = Disposable.empty();
        }
    }

    private void onDataPointState(GiraOneDataPointState giraOneDataPointState) {
        logger.debug("onDataPointState {}", giraOneDataPointState);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("handleConfigurationUpdate {}", configurationParameters);
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("thingUpdated {}", thing);
        super.thingUpdated(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {}, {}", channelUID, command);
        if (command instanceof RefreshType) {
            /*Optional<GiraOneDataPointState> state = Objects.requireNonNull(this.giraOneBridge)
                    .lookupProcessGiraOneDataPointStates().stream()
                    .filter(f -> f.getChannelViewId() == this.channelViewId).findFirst();

            if (state.isPresent()) {
                logger.info("state is {}", state);
            }
            */
        }
    }
}
