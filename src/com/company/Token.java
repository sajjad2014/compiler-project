package com.company;

public class Token {
    Integer type;
    String value;
    boolean constant = false;
    Token(Integer type, String value){
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
