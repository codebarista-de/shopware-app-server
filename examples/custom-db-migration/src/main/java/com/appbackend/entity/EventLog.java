package com.appbackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EVENT_LOG")
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SHOP_ID", nullable = false)
    private Long shopId;

    @Column(name = "EVENT_NAME", nullable = false, length = 255)
    private String eventName;

    @Column(name = "RECEIVED_AT", nullable = false)
    private LocalDateTime receivedAt;

    public EventLog() {
    }

    public EventLog(Long shopId, String eventName, LocalDateTime receivedAt) {
        this.shopId = shopId;
        this.eventName = eventName;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return "EventLog{" +
                "id=" + id +
                ", shopId=" + shopId +
                ", eventName='" + eventName + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
