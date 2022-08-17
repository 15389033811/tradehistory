package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.stereotype.Component;

public enum MarketEnum {

    BINANCE("BINANCE"),
    OKX("OKX");


    // 位置1
    @JsonValue
    private String market;

    MarketEnum(String market) {
        this.market = market;
    }

    public String getMarket() {
        return market;
    }
}
