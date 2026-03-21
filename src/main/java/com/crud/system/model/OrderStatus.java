package com.crud.system.model;

public enum OrderStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
