package de.codebarista.shopware.appserver.service.dto;

public class CreateShopwareNotificationDto {
    private final String status;
    private final String message;

    public CreateShopwareNotificationDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static CreateShopwareNotificationDto success(String message) {
        return new CreateShopwareNotificationDto("success", message);
    }

    public static CreateShopwareNotificationDto error(String message) {
        return new CreateShopwareNotificationDto("error", message);
    }

    public static CreateShopwareNotificationDto info(String message) {
        return new CreateShopwareNotificationDto("info", message);
    }

    public static CreateShopwareNotificationDto warning(String message) {
        return new CreateShopwareNotificationDto("warning", message);
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
