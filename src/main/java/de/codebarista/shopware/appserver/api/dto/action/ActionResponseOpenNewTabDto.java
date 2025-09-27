package de.codebarista.shopware.appserver.api.dto.action;

/**
 * To open a new tab in the user browser you can use the openNewTab action type.
 * You need to pass the url that should be opened as the redirectUrl property inside the payload.
 */
public class ActionResponseOpenNewTabDto {
    private final String redirectUrl;

    public ActionResponseOpenNewTabDto(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * Gets the {@link #redirectUrl}.
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }
}
