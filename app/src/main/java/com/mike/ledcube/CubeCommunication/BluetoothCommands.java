package com.mike.ledcube.CubeCommunication;

import java.util.ArrayList;

public class BluetoothCommands {
    public static final char EOL = ';';
    public static final char DELIMITER = ',';

    // -- Preferences --
    public static char[] offCommand() {
        return new char[]{'0', DELIMITER, '0', DELIMITER, '0', EOL};
    }

    public static char[] onCommand() {
        return new char[]{'0', DELIMITER, '0', DELIMITER, '1', EOL};
    }

    public static char[] getStateCommand() {
        return new char[]{'0', DELIMITER, '0', EOL};
    }

    public static char[] getBrightnessCommand() {
        return new char[]{'0', DELIMITER, '1', EOL};
    }

    public static char[] setBrightnessCommand(int brightness) {
        if (brightness < 1 || brightness > 255) return null;
        String b = String.valueOf(brightness);
        if(brightness < 10){
            return new char[]{'0', DELIMITER, '1', DELIMITER, b.charAt(0), EOL};
        }
        else if (brightness < 100){

            return new char[]{'0', DELIMITER, '1', DELIMITER, b.charAt(0), b.charAt(1), EOL};
        }
        else{
            return new char[]{'0', DELIMITER, '1', DELIMITER, b.charAt(0), b.charAt(1), b.charAt(2), EOL};
        }
    }


    // -- Effects --
    public static char[] getEffectCommand(EffectTypes effect, String... params) {
        ArrayList<Character> command = new ArrayList<>();
        command.add('1');
        command.add(DELIMITER);
        switch (effect) {
            case Fill:
                command.add('0');
                command.add(DELIMITER);
                break;
            case Fire:
                command.add('1');
                command.add(DELIMITER);
                break;
        }
        for (String par : params){
            for(char p : par.toCharArray()) {
                command.add(p);
            }
            command.add(DELIMITER);
        }
        command.remove(command.size() - 1);
        command.add(EOL);
        char[] res = new char[command.size()];
        for(int i = 0; i < command.size(); i++) {
            res[i] = command.get(i);
        }
        return res;
    }

    // -- Games --
    public static char[] getGameInitializationCommand(GameTypes game, String[] params) {
        ArrayList<Character> command = new ArrayList<>();
        command.add('2');
        command.add(DELIMITER);
        switch (game) {
            case Snake:
                command.add('0');
                command.add(DELIMITER);
                break;
        }
        for (String par : params){
            for(char p : par.toCharArray()) {
                command.add(p);
            }
            command.add(DELIMITER);
        }
        command.remove(command.size() - 1);
        command.add(EOL);
        char[] res = new char[command.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = command.get(i);
        }
        return res;
    }

    public static char[] getCommand(char command){
        return new char[] {command, EOL};
    }

    public static String[] parseCommand(char[] command) {
        if (command == null) return null;
        ArrayList<String> res = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();
        for (char s: command) {
            if(s == DELIMITER || s == EOL){
                res.add(tmp.toString());
                tmp = new StringBuilder();
                continue;
            }
            tmp.append(s);
        }
        return res.toArray(new String[0]);
    }

    public static String getColorComponent(float value) {
        return String.valueOf((int)Math.floor(value == 1 ? 255 : value * 256));
    }
}