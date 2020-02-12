package com.company;

import java.util.ArrayList;

class PascalArray {
    ArrayList<Integer> dimensions = new ArrayList<>();
    ArrayList<String> strings = new ArrayList<>();
    String id;
    int type;
    public PascalArray(String id, int type) {
        this.id = id;
        this.type = type;
    }
    public void createStrings(){
        String string = "[" + dimensions.get(dimensions.size() - 1) + " x " + getType(type) + "]";
        strings.add(string);
        for (int i = dimensions.size() - 2; i >= 0; i--) {
            string = "[" + dimensions.get(i) + " x " + string + "]";
            strings.add(string);
        }
    }
    public String getType(int type) {
        switch (type) {
            case 43:
                return "i32";
            case 44:
                return "float";
            case 46:
            case 47:
                return "i8";
            default:
                return "i0";
        }
    }

}
