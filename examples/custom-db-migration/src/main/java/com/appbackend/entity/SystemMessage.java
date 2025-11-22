package com.appbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SYSTEM_MESSAGE")
public class SystemMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "MESSAGE", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "CREATOR", nullable = false, length = 255)
    private String creator;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = false;

    public SystemMessage() {
    }

    public SystemMessage(String message, String creator, Boolean active) {
        this.message = message;
        this.creator = creator;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "SystemMessage{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", creator='" + creator + '\'' +
                ", active=" + active +
                '}';
    }
}
