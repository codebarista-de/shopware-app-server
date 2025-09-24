package de.codebarista.shopware.appbackend.sdk.api.dto.action;

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

    public static ActionResponseDto<ActionResponseNotificationDto> successNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("success", message));
    }

    public static ActionResponseDto<ActionResponseNotificationDto> errorNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("error", message));
    }

    public static ActionResponseDto<ActionResponseNotificationDto> infoNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("info", message));
    }

    public static ActionResponseDto<ActionResponseNotificationDto> warningNotification(String message) {
        return new ActionResponseDto<>("notification", new ActionResponseNotificationDto("warning", message));
    }

    public static ActionResponseDto<ActionResponseModalDto> openModal(String iframeUrl, ActionResponseModalDto.ModalSize size, boolean expand) {
        return new ActionResponseDto<>("openModal", new ActionResponseModalDto(iframeUrl, size, expand));
    }

    public static ActionResponseDto<ActionResponseOpenNewTabDto> openNewTab(String redirectUrl) {
        return new ActionResponseDto<>("openNewTab", new ActionResponseOpenNewTabDto(redirectUrl));
    }

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
