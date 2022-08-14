package com.example.demo.service;

import java.io.InputStream;

public interface TradeHistoryService {

    public byte[] insertFileInfo(InputStream inputStream, String requestId) throws Exception;

    public byte[] getNewData(String requestId) throws Exception;

    public byte[] getNewData2(String requestId) throws Exception;
}
