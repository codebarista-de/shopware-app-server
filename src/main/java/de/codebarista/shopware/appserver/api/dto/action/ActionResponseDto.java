package de.codebarista.shopware.appserver.api.dto.action;

import java.util.Collections;
import java.util.Map;

/**
 * If you want to trigger an action inside the Administration upon completing the action,
 * the app should return a response with a valid body and the header shopware-app-signature
 * containing the SHA256 HMAC of the whole response body signed with the app secret.<br>
 * Set the actionType and add the corresponding payload DTO.<br>
 * See <a href="https://developer.shopware.com/docs/guides/plugins/apps/administration/add-custom-action-button.html">
 * Shopware Documentation</a>
 */
public class ActionResponseDto<PAYLOAD> {
    /**
     * actionType: The type of action the app want to be triggered,
     * including notification, reload, openNewTab, openModal.<br>
     * <b>Mind to add the corresponding payload DTO!</b>
     */
    private final String actionType;
    private final PAYLOAD payload;

    public ActionResponseDto(String actionType, PAYLOAD payload) {
        this.actionType = actionType;
        this.payload = payload;
    }

    /**
     * Creates a success notification response.
     *
     * @param message the success message to display
     * @return an action response that shows a success notification
     */
    public static ActionResponseDto<ActionResponseNotificationDto> successNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("success", message));
    }

    /**
     * Creates an error notification response.
     *
     * @param message the error message to display
     * @return an action response that shows an error notification
     */
    public static ActionResponseDto<ActionResponseNotificationDto> errorNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("error", message));
    }

    /**
     * Creates an info notification response.
     *
     * @param message the info message to display
     * @return an action response that shows an info notification
     */
    public static ActionResponseDto<ActionResponseNotificationDto> infoNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("info", message));
    }

    /**
     * Creates a warning notification response.
     *
     * @param message the warning message to display
     * @return an action response that shows a warning notification
     */
    public static ActionResponseDto<ActionResponseNotificationDto> warningNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("warning", message));
    }

    /**
     * Creates a modal dialog response.
     *
     * @param iframeUrl the URL to load in the modal's iframe
     * @param size      the size of the modal window
     * @param expand    whether the modal should be expanded
     * @return an action response that opens a modal dialog
     */
    public static ActionResponseDto<ActionResponseModalDto> openModal(String iframeUrl, ActionResponseModalDto.ModalSize size, boolean expand) {
        return new ActionResponseDto<>("openModal", new ActionResponseModalDto(iframeUrl, size, expand));
    }

    /**
     * Creates a new tab response.
     *
     * @param redirectUrl the URL to open in the new tab
     * @return an action response that opens a new browser tab
     */
    public static ActionResponseDto<ActionResponseOpenNewTabDto> openNewTab(String redirectUrl) {
        return new ActionResponseDto<>("openNewTab", new ActionResponseOpenNewTabDto(redirectUrl));
    }

    /**
     * Creates a page reload response.
     *
     * @return an action response that reloads the current Administration page
     */
    public static ActionResponseDto<Map<?, ?>> reload() {
        return new ActionResponseDto<>("reload", Collections.emptyMap());
    }

    /**
     * Gets the {@link #actionType}.
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Gets the {@link #payload}.
     */
    public PAYLOAD getPayload() {
        return payload;
    }
}
