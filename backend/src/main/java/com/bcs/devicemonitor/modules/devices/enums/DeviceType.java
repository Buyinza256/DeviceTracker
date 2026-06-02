package com.bcs.devicemonitor.modules.devices.enums;

/**
 * The category of network asset a device represents. Mirrors the equipment
 * types described in the brief (CPE, Router, Switch, Access Point, Firewall, ONT).
 */
public enum DeviceType {
    CPE,
    ROUTER,
    SWITCH,
    ACCESS_POINT,
    FIREWALL,
    ONT,
    OTHER
}
