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
import org.openhab.binding.giraone.internal.dto.GiraOneChannelDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneProjectChannel;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.binding.giraone.internal.util.ThingStateFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
    private Disposable disposableOnConnectionState = Disposable.empty();

    public GiraOneThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("initialize {}", getThing().getUID());
        disposeSubscriptions();
        if (getBridge() != null) {
            this.channelViewId = Integer
                    .parseInt(Objects.requireNonNull(getThing().getProperties().get("channelViewId")));

            giraOneBridge = (GiraOneBridge) getBridge().getHandler();
            if (giraOneBridge != null) {
                this.disposableOnDataPointState = giraOneBridge.subscribeOnGiraOneDataPointStates(this.channelViewId,
                        this::onDataPointState);
                this.disposableOnConnectionState = giraOneBridge.subscribeOnConnectionState(this::onConnectionState);
            }
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void disposeSubscriptions() {
        try {
            this.disposableOnDataPointState.dispose();
            this.disposableOnConnectionState.dispose();
        } finally {
            this.disposableOnDataPointState = Disposable.empty();
            this.disposableOnConnectionState = Disposable.empty();
        }
    }

    @Override
    public void dispose() {
        disposeSubscriptions();
        super.dispose();
    }

    private void onDataPointState(GiraOneChannelDataPoint giraOneDataPointState) {
        logger.debug("onDataPointState {}", giraOneDataPointState);
        if (giraOneDataPointState.getValue() != null) {
            String channelId = CaseFormatter.lowerCaseHyphen(giraOneDataPointState.getName());
            updateState(channelId, ThingStateFactory.from(channelId, giraOneDataPointState.getValue()));
        }
    }

    private void onConnectionState(GiraOneConnectionState connectionState) {
        if (connectionState == GiraOneConnectionState.Connected) {
            Objects.requireNonNull(giraOneBridge).lookupGiraOneChannelDataPointValues(this.channelViewId);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_OFFLINE);
        }
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

    private Optional<GiraOneDataPoint> findGiraOneDataPoint(final String ohChannel) {
        Optional<GiraOneProjectChannel> channel = Objects.requireNonNull(this.giraOneBridge).lookupGiraOneProject()
                .lookupChannelByChannelViewId(this.channelViewId);
        return channel.flatMap(giraOneProjectChannel -> giraOneProjectChannel.getDataPoints().stream()
                .filter(f -> CaseFormatter.lowerCaseHyphen(f.getName()).equals(ohChannel)).findFirst());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Optional<GiraOneDataPoint> datapoint = findGiraOneDataPoint(channelUID.getId());
        if (datapoint.isPresent()) {
            logger.debug("handleCommand {}, {}", channelUID, command);
            switch ((Object) command) {
                case RefreshType cmd -> handleRefreshTypeCommand(cmd);
                case DecimalType cmd -> handleDecimalTypeCommand(datapoint.get(), cmd);
                case OnOffType cmd -> handleOnOffTypeCommand(datapoint.get(), cmd);
                default -> throw new IllegalStateException("Unsupported value: " + (Object) command);
            }
        } else {
            logger.debug("not responsible for handling handleCommand {}, {}", channelUID, command);
        }
    }

    void handleRefreshTypeCommand(RefreshType command) {
        Objects.requireNonNull(this.giraOneBridge).lookupGiraOneChannelDataPointValues(this.channelViewId);
    }

    void handleDecimalTypeCommand(GiraOneDataPoint datapoint, DecimalType command) {
        Objects.requireNonNull(this.giraOneBridge).setGiraOneDataPointValue(datapoint, command.intValue());
    }

    void handleOnOffTypeCommand(GiraOneDataPoint datapoint, OnOffType command) {
        String value = command == OnOffType.ON ? "1" : "0";
        Objects.requireNonNull(this.giraOneBridge).setGiraOneDataPointValue(datapoint, value);
    }
}
