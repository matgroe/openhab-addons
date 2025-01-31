package org.openhab.binding.giraone.internal.communication.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.binding.giraone.internal.communication.ws.commands.RegisterApplication;

class RegisterApplicationTest {
    private static final String APP_ID = "APP_ID_123";
    private static final String APP_TYPE = "APP_TYPE";
    private static final String INSTANCE_ID = "InstanceId";

    @Test
    void builder() {
        RegisterApplication cmd = RegisterApplication.builder().with(RegisterApplication::setApplicationId, APP_ID)
                .with(RegisterApplication::setApplicationType, APP_TYPE)
                .with(RegisterApplication::setInstanceId, INSTANCE_ID).build();

        assertEquals(APP_ID, cmd.getApplicationId());
        assertEquals(APP_TYPE, cmd.getApplicationType());
        assertEquals(INSTANCE_ID, cmd.getInstanceId());
    }
}
