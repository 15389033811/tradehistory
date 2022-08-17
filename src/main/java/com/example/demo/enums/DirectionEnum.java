package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DirectionEnum {

    BUY("BUY"),
    SELL("SELL");


    // 位置1
    @JsonValue
    private String direciont;


    DirectionEnum(String direciont) {
            this.direciont = direciont;
    }


    public String getDireciont() {
        return direciont;
    }
}
