package com.example.demo.service;

import com.example.demo.common.Result;

import java.io.InputStream;

public interface TradeHistoryService {

    Result<String> insertFileInfoByMap(InputStream inputStream, String requestId, String market, String fileName, Integer mode) throws  Exception;

}
