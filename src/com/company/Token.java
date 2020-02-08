package com.company;

public class Token {
    Integer type;
    private String value;
    private Integer intValue;
    private Double doubleValue;
    Token(Integer type, String value){
        this.type = type;
        this.value = value;
    }
    Token(Integer type, Integer value){
        this.type = type;
        this.intValue = value;
    }
    Token(Integer type, Double value){
        this.type = type;
        this.doubleValue = value;
    }

    public String toString() {
        return this.type.toString();
    }
}
