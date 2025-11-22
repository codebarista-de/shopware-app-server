package de.codebarista.shopware.appserver.service.dto.sync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncResult {
    @JsonProperty("success")
    private boolean success;

    public boolean successful() {
        return success;
    }
}
