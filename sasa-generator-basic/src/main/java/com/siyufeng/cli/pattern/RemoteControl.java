package com.siyufeng.cli.pattern;

public class RemoteControl {
    private Command command;

    private TurnOnCommand turnOnCommand;

    private TurnOffCommand turnOffCommand;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void setTurnOnCommand(TurnOnCommand turnOnCommand) {
        this.turnOnCommand = turnOnCommand;
    }

    public void setTurnOffCommand(TurnOffCommand turnOffCommand) {
        this.turnOffCommand = turnOffCommand;
    }

    public void pressButton() {
        // 按下按钮，执行命令
        command.execute();
    }

    public void turnOn() {
        turnOnCommand.execute();
    }

    public void turnOff() {
        turnOffCommand.execute();
    }
}