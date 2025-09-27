package de.codebarista.shopware.appserver.api.dto.action;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * To open a modal with the embedded link in the iframe, you can use the openModal action type.
 * You need to pass the url that should be opened as the iframeUrl property and the size property inside the payload.
 */

public class ActionResponseModalDto {
    public enum ModalSize {
        @JsonProperty("small")
        SMALL,
        @JsonProperty("medium")
        MEDIUM,
        @JsonProperty("large")
        LARGE,
        @JsonProperty("fullscreen")
        FULLSCREEN
    }
    /**
     * e.g. : "http://google.com",
     */
    private final String iframeUrl;

    /**
     * The size of the modal in openModal type, including small, medium, large, fullscreen, default medium
     */
    private final ModalSize size;

    /**
     * The expansion of the modal in openModal type, including true, false, default false
     */
    private final boolean expand;

    public ActionResponseModalDto(String iframeUrl, ModalSize size, boolean expand) {
        this.iframeUrl = iframeUrl;
        this.size = size;
        this.expand = expand;
    }

    /**
     * Gets the {@link #iframeUrl}.
     */
    public String getIframeUrl() {
        return iframeUrl;
    }

    /**
     * Gets the {@link #size}.
     */
    public ModalSize getSize() {
        return size;
    }

    /**
     * Gets the {@link #expand}.
     */
    public boolean isExpand() {
        return expand;
    }
}
