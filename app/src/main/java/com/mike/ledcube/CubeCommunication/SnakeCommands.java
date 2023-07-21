package com.mike.ledcube.CubeCommunication;

public enum SnakeCommands {
    UP(0),
    DOWN(1),
    LEFT(2),
    RIGHT(3),
    FORWARD(4),
    BACKWARD(5),
    QUIT(6);
    private final byte command;
    SnakeCommands(int command){
        this.command = (byte) command;
    }

    public byte getCommand() {
        return command;
    }
}
