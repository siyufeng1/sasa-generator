package com.siyufeng.cli.pattern;

public class Device {
    private String name;

    public Device(String name) {
        this.name = name;
    }

    // 提供打开方法
    public void turnOn() {
        System.out.println(name + "设备打开");
    }

    // 提供关闭方法
    public void turnOff() {
        System.out.println(name + "设备关闭");
    }
}