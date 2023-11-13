package com.example.demo.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.TradeHistoryService;
@RequestMapping("/api")
@RestController
public class GeneratorTradeController {

    @Autowired
    TradeHistoryService tradeHistoryService;

//    @Resource
//    RedisTemplate redisTemplate;

    @PostMapping("/upload")
    public Result<String> parseFile(HttpServletResponse response
            , @RequestParam(value = "file", required = true) MultipartFile file
            , @RequestParam(value = "market", required = true) String market
            , @RequestParam(value = "mode", required = true) Integer mode) throws Exception {
        if (file.isEmpty()) {
            return Result.failWithMsg("上传失败，请选择文件");
        }
        System.out.println("________________"+ market);

//        //统计使用量
//        try {
//            Date date = new Date();//此时date为当前的时间
//            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");//设置当前时间的格式，为年-月-日
//            String dateInfo = dateFormat.format(date);
//            redisTemplate.opsForValue().increment(dateInfo, 1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 生成uuid
        String requestId = UUID.randomUUID().toString();

        InputStream fileInputStream = file.getInputStream();
        String fileName = file.getOriginalFilename();

        Result<String> result = tradeHistoryService.insertFileInfoByMap(fileInputStream, requestId,market,fileName, mode);;

        // 将文件写入输入流
        // FileInputStream fileInputStream = new FileInputStream(file);
        // InputStream fis = new BufferedInputStream(fileInputStream);
        // tradeHistoryService.getNewData(requestId);

//        // 清空response
//        response.reset();
//        // 设置response的Header
//        response.setCharacterEncoding("UTF-8");
//        // Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
//        // attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline;
//        // filename=文件名.mp3"
//        // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
//        response.addHeader("Content-Disposition",
//                "attachment;filename=" + URLEncoder.encode("trade_history.txt", "UTF-8"));
//        // 告知浏览器文件的大小
//        response.addHeader("Content-Length", "" + buffer.length);
//        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
//        response.setContentType("application/octet-stream");
//        outputStream.write(buffer);
//        outputStream.flush();
//        fileInputStream.close();
//        outputStream.close();
        return result;
    }
}