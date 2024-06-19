package com.example.grocery.models;

public class ModelOrderUser {
    String orderId,orderTime,orderTo,orderBy,orderCost,orderStatus;

    public ModelOrderUser() {
    }

    public ModelOrderUser(String orderId, String orderTime, String orderTo, String orderBy, String orderCost, String orderStatus) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.orderTo = orderTo;
        this.orderBy = orderBy;
        this.orderCost = orderCost;
        this.orderStatus = orderStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo(String orderTo) {
        this.orderTo = orderTo;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(String orderCost) {
        this.orderCost = orderCost;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
