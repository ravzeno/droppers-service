package com.ravzeno.droppers_service.commons.security.enum_type;

public enum Role {
    ADMIN,
    DEVICE;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
