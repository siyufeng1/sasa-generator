package com.siyufeng.cli.pattern;

public class TurnOnCommand implements Command {
    private Device device;

    public TurnOnCommand(Device device){
        this.device = device;
    }

    public void execute(){
        device.turnOn();
    }
}