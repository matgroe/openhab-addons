package org.openhab.binding.giraone.internal.communication.webservice;

import com.google.gson.annotations.SerializedName;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;

import com.google.gson.JsonObject;

/**
 * {@link GiraOneCommandResponse} implementation for responses as received from the
 * Gira One Webservice interface.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneWebserviceResponse implements GiraOneCommandResponse {
    @SerializedName(value = "data")
    public final JsonObject responseBody;

    public GiraOneWebserviceResponse(final JsonObject responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public JsonObject getResponseBody() {
        return responseBody;
    }


}
