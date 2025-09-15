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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.binding.giraone.internal.util.ThingStateFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GiraOneDefaultThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneDefaultThingHandler extends GiraOneBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GiraOneDefaultThingHandler.class);

    public GiraOneDefaultThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Builds the openhab channelId from the given {@link GiraOneDataPoint}. The
     * generated value must match the channel.id property in the thing.xml in order
     * to match the wanted openhab channel.
     *
     * @param dataPoint The {@link GiraOneDataPoint}
     * @return referencing id for the thing channel
     */
    protected String buildThingChannelId(GiraOneDataPoint dataPoint) {
        return CaseFormatter.lowerCaseHyphen(dataPoint.getName());
    }

    /**
     * Builds the openhab channelId from the given {@link GiraOneValue}. The
     * generated value must match the channel.id property in the thing.xml in order
     * to match the wanted openhab channel.
     *
     * @param value {@link GiraOneValue}
     * @return referencing id for the thing channel
     */
    protected String buildThingChannelId(GiraOneValue value) {
        return buildThingChannelId(value.getGiraOneDataPoint());
    }

    /**
     * Handler function for receiving {@link GiraOneValue} which contains
     * the value for an item channel.
     * 
     * @param value The value to apply on a Openhab Thing
     */
    protected void onGiraOneValue(GiraOneValue value) {
        logger.debug("onGiraOneValue :: {}", value);
        String thingChannelId = buildThingChannelId(value);
        updateState(thingChannelId, ThingStateFactory.from(thingChannelId, value.getValue()));
    }

    /**
     * This function takes a OpenHab-Channel identifier as arguments and finds the concerning
     * GiraOneDataPoint within the project configuration.
     *
     * @param ohChannelId the channel id in OpenHab context
     * @return An Optional of GiraOneDataPoint
     */
    protected Optional<GiraOneDataPoint> findGiraOneDataPointForOhChannel(final String ohChannelId) {
        Optional<GiraOneChannel> channel = lookupGiraOneProjectChannel();
        String channelId = normalizeOpenhabChannelName(ohChannelId);
        return channel.flatMap(giraOneProjectChannel -> giraOneProjectChannel.getDataPoints().stream()
                .filter(f -> CaseFormatter.lowerCaseHyphen(f.getName()).equals(channelId)).findFirst());
    }

    @Override
    public final void handleCommand(ChannelUID channelUID, Command command) {
        Optional<GiraOneDataPoint> datapoint = findGiraOneDataPointForOhChannel(channelUID.getId());
        if (datapoint.isPresent()) {
            logger.debug("handleCommand :: channelUID={}, command={}", channelUID, command);
            switch ((Object) command) {
                case RefreshType cmd -> handleRefreshTypeCommand(cmd);
                case DecimalType cmd -> handleDecimalTypeCommand(datapoint.get(), cmd);
                case OnOffType cmd -> handleOnOffTypeCommand(datapoint.get(), cmd);
                case UpDownType cmd -> handleUpDownTypeCommand(datapoint.get(), cmd);
                case StopMoveType cmd -> handleStopMoveTypeCommand(datapoint.get(), cmd);
                case StringType cmd -> handleStringTypeCommand(datapoint.get(), cmd);
                case QuantityType<?> cmd -> handleQuantityTypeCommand(datapoint.get(), cmd);
                default -> throw new IllegalStateException(
                        "Unsupported Command '" + command.getClass().getSimpleName() + "' with value of +" + command);
            }
        } else {
            logger.debug("not responsible for handling handleCommand {}, {}", channelUID, command);
        }
    }

    /**
     * Override this method for special tasks on receiving a {@link RefreshType} command from openhab.
     * 
     * @param command The {@link RefreshType} command.
     */
    protected void handleRefreshTypeCommand(RefreshType command) {
        Optional<GiraOneChannel> channel = lookupGiraOneProjectChannel();
        if (channel.isPresent()) {
            logger.trace("handleRefreshTypeCommand :: channel={}, command={}", channel.get(), command);
            getGiraOneBridge().lookupGiraOneChannelValues(channel.get());
        }
    }

    /**
     * Override this method for special tasks on receiving a {@link DecimalType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link DecimalType} command.
     */
    protected void handleDecimalTypeCommand(GiraOneDataPoint datapoint, DecimalType command) {
        logger.trace("handleDecimalTypeCommand :: datapoint={}, command={}", datapoint, command.intValue());
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.toString());
    }

    /**
     * Override this method for special tasks on receiving a {@link OnOffType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link OnOffType} command.
     */
    protected void handleOnOffTypeCommand(GiraOneDataPoint datapoint, OnOffType command) {
        logger.trace("handleOnOffTypeCommand :: datapoint={}, command={}", datapoint, command.name());
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, (command == OnOffType.ON ? 1 : 0));
    }

    /**
     * Override this method for special tasks on receiving a {@link UpDownType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link UpDownType} command.
     */
    protected void handleUpDownTypeCommand(GiraOneDataPoint datapoint, UpDownType command) {
        logger.trace("handleUpDownType :: datapoint={}, command={}", datapoint, command.name());
        switch (command) {
            case DOWN -> getGiraOneBridge().setGiraOneDataPointValue(datapoint, 100);
            case UP -> getGiraOneBridge().setGiraOneDataPointValue(datapoint, 0);
        }
    }

    /**
     * Override this method for special tasks on receiving a {@link StopMoveType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link StopMoveType} command.
     */
    protected void handleStopMoveTypeCommand(GiraOneDataPoint datapoint, StopMoveType command) {
        logger.warn("handleStopMoveType is not implemented :: datapoint={}, command={}", datapoint, command.name());
    }

    /**
     * Override this method for special tasks on receiving a {@link StringType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link StringType} command.
     */
    protected void handleStringTypeCommand(GiraOneDataPoint datapoint, StringType command) {
        logger.trace("handleStringType :: datapoint={}, command={}", datapoint, command);
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.toString());
    }

    /**
     * Override this method for special tasks on receiving a {@link QuantityType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link QuantityType} command.
     */
    protected void handleQuantityTypeCommand(GiraOneDataPoint datapoint, QuantityType<?> command) {
        logger.trace("handleQuantityType :: datapoint={}, command={}", datapoint, command);
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.floatValue());
    }
}
