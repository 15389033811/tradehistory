package com.example.demo.service;

import com.example.demo.common.Result;

import java.io.InputStream;

public interface TradeHistoryService {

    public Result<String> insertFileInfo(InputStream inputStream, String requestId) throws Exception;

    public byte[] getNewData(String requestId) throws Exception;

    public byte[] getNewData2(String requestId) throws Exception;

    public Result<String> getNewData3(String requestId) throws Exception;
}
