package de.codebarista.shopware.appbackend.sdk.api.dto.action;

/**
 * To send a notification, you can use the notification action type.
 * You need to pass the status property and the content of the notification as message property inside the payload.
 */
public class ActionResponseNotificationDto {
    /**
     * Notification status, including success, error, info, warning
     */
    private final String status;
    private final String message;

    public ActionResponseNotificationDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Gets the {@link #status}.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the {@link #message}.
     */
    public String getMessage() {
        return message;
    }
}
