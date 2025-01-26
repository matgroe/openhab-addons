package org.openhab.binding.giraone.internal.discovery;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.GiraOneBindingConstants;
import org.openhab.binding.giraone.internal.handler.GiraOneBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = GiraOneThingDiscoveryService.class)
public class GiraOneThingDiscoveryService extends AbstractThingHandlerDiscoveryService<GiraOneBridgeHandler> {
    private static final int TIMEOUT = 60;
    private final Logger logger = LoggerFactory.getLogger(GiraOneThingDiscoveryService.class);

    public GiraOneThingDiscoveryService() {
        super(GiraOneBridgeHandler.class, GiraOneBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(GiraOneBindingConstants.G1_SERVER_TYPE_UID);
    }

    @Override
    protected void startScan() {
        logger.info("startScan");
    }

    @Override
    public void deactivate() {
        logger.info("deactivate");
        removeOlderResults(Instant.now().toEpochMilli());
    }
}
