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

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNELVIEW_URN;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.dto.GiraOneChannelDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneProjectChannel;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.binding.giraone.internal.util.ThingStateFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
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
 * The {@link GiraOneDefaultThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneDefaultThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GiraOneDefaultThingHandler.class);
    private int channelViewId;
    private @Nullable GiraOneBridge giraOneBridge;
    private Disposable disposableOnDataPointState = Disposable.empty();
    private Disposable disposableOnConnectionState = Disposable.empty();

    public GiraOneDefaultThingHandler(Thing thing) {
        super(thing);
    }

    protected GiraOneBridge getGiraOneBridge() throws NullPointerException {
        Objects.requireNonNull(getBridge(), "getBridge() must not evaluate to null");
        return (GiraOneBridge) Objects.requireNonNull(getBridge().getHandler());
    }

    @Override
    public void initialize() {
        logger.debug("initialize {}", getThing().getUID());
        try {
            this.giraOneBridge = getGiraOneBridge();
            this.disposableOnConnectionState = giraOneBridge.subscribeOnConnectionState(this::onConnectionState);
        } catch (NullPointerException exp) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_OFFLINE, exp.getMessage());
        }
    }

    protected void disposeSubscriptions() {
        try {
            this.disposableOnConnectionState.dispose();
            this.disposableOnDataPointState.dispose();
        } finally {
            this.disposableOnConnectionState = Disposable.empty();
            this.disposableOnDataPointState = Disposable.empty();
        }
    }

    @Override
    public void dispose() {
        disposeSubscriptions();
        super.dispose();
    }

    /**
     * Handler function for receiving {@link GiraOneChannelDataPoint} which contains
     * the value for an item channel.
     * 
     * @param giraOneDataPointState
     */
    protected void onDataPointState(GiraOneChannelDataPoint giraOneDataPointState) {
        logger.debug("onDataPointState {}", giraOneDataPointState);
        if (giraOneDataPointState.getValue() != null) {
            String channelId = CaseFormatter.lowerCaseHyphen(giraOneDataPointState.getGiraOneDataPoint().getName());
            updateState(channelId, ThingStateFactory.from(channelId, giraOneDataPointState.getValue()));
        }
    }

    /**
     * Handler function for receiving updates on the {@link GiraOneBridge} connection state.
     *
     * @param connectionState The {@link GiraOneBridgeConnectionState}.
     */
    protected void onConnectionState(GiraOneBridgeConnectionState connectionState) {
        logger.debug("ConnectionStateChanged to {}", connectionState);
        if (connectionState == GiraOneBridgeConnectionState.Connected) {
            this.channelViewId = detectChannelViewId();
            if (this.channelViewId > 0 && giraOneBridge != null) {
                this.disposableOnDataPointState = giraOneBridge.subscribeOnGiraOneDataPointStates(this.channelViewId,
                        this::onDataPointState);
                giraOneBridge.lookupGiraOneChannelDataPointValues(this.channelViewId);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge has GiraOneBridgeConnectionState of %s", connectionState.name()));
        }
    }

    /**
     *
     * @return an {@link Optional<GiraOneProjectChannel>}
     */
    protected Optional<GiraOneProjectChannel> lookupGiraOneProjectChannel() {
        String channelViewUrn = Objects.requireNonNull(getThing().getProperties().get(PROPERTY_CHANNELVIEW_URN));
        return Objects.requireNonNull(this.giraOneBridge).lookupGiraOneProject()
                .lookupChannelByChannelViewUrn(channelViewUrn);
    }

    protected int detectChannelViewId() {
        return lookupGiraOneProjectChannel().map(GiraOneProjectChannel::getChannelViewId).orElse(0);
    }

    protected Optional<GiraOneDataPoint> findGiraOneDataPoint(final String ohChannel) {
        Optional<GiraOneProjectChannel> channel = Objects.requireNonNull(this.giraOneBridge).lookupGiraOneProject()
                .lookupChannelByChannelViewId(this.channelViewId);
        return channel.flatMap(giraOneProjectChannel -> giraOneProjectChannel.getDataPoints().stream()
                .filter(f -> CaseFormatter.lowerCaseHyphen(f.getName()).equals(ohChannel)).findFirst());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Optional<GiraOneDataPoint> datapoint = findGiraOneDataPoint(channelUID.getId());
        if (datapoint.isPresent()) {
            logger.debug("handleCommand :: channelUID={}, command={}", channelUID, command);
            switch ((Object) command) {
                case RefreshType cmd -> handleRefreshTypeCommand(cmd);
                case DecimalType cmd -> handleDecimalTypeCommand(datapoint.get(), cmd);
                case OnOffType cmd -> handleOnOffTypeCommand(datapoint.get(), cmd);
                case UpDownType cmd -> handleUpDownType(datapoint.get(), cmd);
                case StopMoveType cmd -> handleStopMoveType(datapoint.get(), cmd);
                case StringType cmd -> handleStringType(datapoint.get(), cmd);
                default -> throw new IllegalStateException("Unsupported value: " + (Object) command);
            }
        } else {
            logger.debug("not responsible for handling handleCommand {}, {}", channelUID, command);
        }
    }

    protected void handleRefreshTypeCommand(RefreshType command) {
        logger.trace("handleRefreshTypeCommand :: channelViewId={}, command={}", this.channelViewId, command);
        Objects.requireNonNull(this.giraOneBridge).lookupGiraOneChannelDataPointValues(this.channelViewId);
    }

    protected void handleDecimalTypeCommand(GiraOneDataPoint datapoint, DecimalType command) {
        logger.trace("handleDecimalTypeCommand :: datapoint={}, command={}", datapoint.getId(), command.intValue());
        Objects.requireNonNull(this.giraOneBridge).setGiraOneDataPointValue(datapoint, command.intValue());
    }

    protected void handleOnOffTypeCommand(GiraOneDataPoint datapoint, OnOffType command) {
        logger.trace("handleOnOffTypeCommand :: datapoint={}, command={}", datapoint.getId(), command.name());
        String value = command == OnOffType.ON ? "1" : "0";
        Objects.requireNonNull(this.giraOneBridge).setGiraOneDataPointValue(datapoint, value);
    }

    protected void handleUpDownType(GiraOneDataPoint datapoint, UpDownType command) {
        logger.trace("handleUpDownType :: datapoint={}, command={}", datapoint.getId(), command.name());
        switch (command) {
            case DOWN -> Objects.requireNonNull(this.giraOneBridge).setGiraOneDataPointValue(datapoint, 100);
            case UP -> Objects.requireNonNull(this.giraOneBridge).setGiraOneDataPointValue(datapoint, 0);
        }
    }

    protected void handleStopMoveType(GiraOneDataPoint datapoint, StopMoveType command) {
        logger.warn("handleStopMoveType is not implemented :: datapoint={}, command={}", datapoint.getId(),
                command.name());
    }

    protected void handleStringType(GiraOneDataPoint datapoint, StringType command) {
        logger.warn("handleStringType is not implemented :: datapoint={}, command={}", datapoint.getId(),
                command.toString());
    }
}
