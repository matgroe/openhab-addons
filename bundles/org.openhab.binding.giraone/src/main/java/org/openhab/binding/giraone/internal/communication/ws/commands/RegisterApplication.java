package org.openhab.binding.giraone.internal.communication.ws.commands;

import org.openhab.binding.giraone.internal.util.GenericBuilder;

public class RegisterApplication extends ServerCommand {

    private String applicationId = "Gira.UniversalApp";
    private String applicationType = "ui";
    private String instanceId = null;

    public static GenericBuilder<RegisterApplication> builder() {
        return GenericBuilder.of(RegisterApplication::new);
    }

    protected RegisterApplication() {
        super(GiraOneCommand.RegisterApplication);
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

}
