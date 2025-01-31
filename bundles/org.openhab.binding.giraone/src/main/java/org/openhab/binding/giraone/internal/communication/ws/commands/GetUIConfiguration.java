package org.openhab.binding.giraone.internal.communication.ws.commands;

import org.openhab.binding.giraone.internal.util.GenericBuilder;

public class GetUIConfiguration extends ServerCommand {
    public static GenericBuilder<GetUIConfiguration> builder() {
        return GenericBuilder.of(GetUIConfiguration::new);
    }

    protected GetUIConfiguration() {
        super(GiraOneCommand.GetUIConfiguration);
    }
}
