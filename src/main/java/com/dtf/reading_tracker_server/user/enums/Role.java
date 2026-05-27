package com.dtf.reading_tracker_server.user.enums;

public enum Role {
    USER,
    ADMIN;

    // Optional: map to Spring Security GrantedAuthority
    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}