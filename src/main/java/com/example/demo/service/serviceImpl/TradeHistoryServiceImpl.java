package com.example.demo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.common.xslutil.ExeclUtil;
import com.example.demo.dao.TbOriginMapper;
import com.example.demo.entity.TbOrigin;
import com.example.demo.enums.DirectionEnum;
import com.example.demo.enums.MarketEnum;
import com.example.demo.service.TradeHistoryService;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TradeHistoryServiceImpl implements TradeHistoryService {

    @Autowired
    TbOriginMapper tbOriginMapper;

    @Autowired
    ExeclUtil execlUtil;

    private static final Logger logger = LoggerFactory.getLogger(TradeHistoryServiceImpl.class);

    @Transactional
    public Result<String> insertFileInfo(InputStream inputStream, String requestId, String market, String fileName) throws Exception {

        try {

            switch (market) {
                case "BINANCE":
                    if (!fileName.endsWith("xls") && !fileName.endsWith("xlsx")) {
                        return Result.failWithMsg("币安交易所请上传后缀为xls、xlsx的文件");
                    }
                    insertBinanceData(inputStream, requestId);
                    break;
                case "OKX":
                    if (!fileName.endsWith("csv")) {
                        return Result.failWithMsg("欧意交易所请上传后缀为csv的文件");
                    }
                    insertOkxData(inputStream, requestId);
                    break;
                default:
                    return Result.failWithMsg("敬请等待支持更多交易所");
            }
            Result<String> dataRes = getTradeDataStr(requestId, market);

            if (dataRes.getCode() == -1) {
                logger.error("解析数据异常", dataRes.getMessage());
                return Result.failWithMsg(dataRes.getMessage());
            }
            return dataRes;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("计算交易数据异常", e);
            return Result.failWithMsg("计算交易数据异常，请查看教程，确认文件是否合规");
        }
    }

    public Result<String> insertFileInfoByMap(InputStream inputStream, String requestId, String market, String fileName) throws Exception {
        List<TbOrigin> parseList = new ArrayList();
        try {
            switch (market) {
                case "BINANCE":
                    if (!fileName.endsWith("xls") && !fileName.endsWith("xlsx")) {
                        return Result.failWithMsg("币安交易所请上传后缀为xls、xlsx的文件");
                    }
                    parseList = parseBinanceDataByMap(inputStream);
                    break;
                case "OKX":
                    if (!fileName.endsWith("csv")) {
                        return Result.failWithMsg("欧意交易所请上传后缀为csv的文件");
                    }
                    parseList = parseOkxDataByMap(inputStream);
                    break;
                default:
                    return Result.failWithMsg("敬请等待支持更多交易所");
            }
            Result<String> dataRes = getTradeDataStr(parseList, market);

            if (dataRes.getCode() == -1) {
                logger.error("解析数据异常", dataRes.getMessage());
                return Result.failWithMsg(dataRes.getMessage());
            }
            return dataRes;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("计算交易数据异常", e);
            return Result.failWithMsg("计算交易数据异常，请查看教程，确认文件是否合规");
        }
    }


    public void insertOkxData(InputStream inputStream, String requestId) throws IOException, ParseException {
        InputStream xlsInputStream = execlUtil.getWorkbookByCsv(inputStream);
        Workbook workbook = WorkbookFactory.create(xlsInputStream);
        // 3、创建工作簿对象，并获取工作表1
        // HSSFWorkbook workbook = new XSSFWorkbook(fileSystem);
        Sheet sheet = workbook.getSheet("Sheet1");
        int lastRowIndex = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowIndex; i++) {
            // 4.1 获取每行的数据
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }

            // 5、从每一列中获取参数
            short lastCellNum = row.getLastCellNum();
            TbOrigin tbOrigin = new TbOrigin();
            tbOrigin.setRequestId(requestId);
            for (int j = 0; j < lastCellNum; j++) {
                String tradeStatus = row.getCell(14).getStringCellValue();
                if (!tradeStatus.equals("COMPLETE") && !tradeStatus.equals("完全成交")) {
                    continue;
                }
                switch (j) {
                    case 1:
                        // row.getCell(j).set
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                        tbOrigin.setDate(Long.toString(sdf.parse(row.getCell(j).getStringCellValue()).getTime()));
                        //tbOrigin.setDate(row.getCell(j).getStringCellValue());
                        break;
                    case 3:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);

                        String originStr = row.getCell(j).getStringCellValue();
                        String[] originArr = originStr.split("-");
                        String resStr = originArr[0] + originArr[1];
                        tbOrigin.setCoin(resStr);
                        break;
                    case 5:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        String originDirStr = row.getCell(j).getStringCellValue();
                        String directionStr = "";
                        if (originDirStr.equals("买入") || originDirStr.equals("Buy")) {
                            directionStr = DirectionEnum.BUY.getDireciont();
                        } else if (originDirStr.equals("卖") || originDirStr.equals("Sell")) {
                            directionStr = DirectionEnum.SELL.getDireciont();
                        }
                        tbOrigin.setDirection(directionStr);
                        break;
                    case 11:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setAvgPrice(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 8:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setTransactionAmount(
                                Double.valueOf(row.getCell(11).getStringCellValue())
                                        * Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 12:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setProfit(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    default:
                        break;
                }
            }

            tbOriginMapper.insert(tbOrigin);
        }
    }


    public List<TbOrigin> parseOkxDataByMap(InputStream inputStream) throws Exception {
        List<TbOrigin> resList = new ArrayList<>();
        InputStream xlsInputStream = execlUtil.getWorkbookByCsv(inputStream);
        Workbook workbook = WorkbookFactory.create(xlsInputStream);
        // 3、创建工作簿对象，并获取工作表1
        // HSSFWorkbook workbook = new XSSFWorkbook(fileSystem);
        Sheet sheet = workbook.getSheet("Sheet1");
        int lastRowIndex = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowIndex; i++) {
            // 4.1 获取每行的数据
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }

            // 5、从每一列中获取参数
            short lastCellNum = row.getLastCellNum();
            TbOrigin tbOrigin = new TbOrigin();
            for (int j = 0; j < lastCellNum; j++) {
                String tradeStatus = row.getCell(14).getStringCellValue();
                if (!tradeStatus.equals("COMPLETE") && !tradeStatus.equals("完全成交")) {
                    continue;
                }
                switch (j) {
                    case 1:
                        // row.getCell(j).set
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                        tbOrigin.setDate(Long.toString(sdf.parse(row.getCell(j).getStringCellValue()).getTime()));
                        //tbOrigin.setDate(row.getCell(j).getStringCellValue());
                        break;
                    case 3:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);

                        String originStr = row.getCell(j).getStringCellValue();
                        String[] originArr = originStr.split("-");
                        String resStr = originArr[0] + originArr[1];
                        tbOrigin.setCoin(resStr);
                        break;
                    case 5:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        String originDirStr = row.getCell(j).getStringCellValue();
                        String directionStr = "";
                        if (originDirStr.equals("买入") || originDirStr.equals("Buy")) {
                            directionStr = DirectionEnum.BUY.getDireciont();
                        } else if (originDirStr.equals("卖") || originDirStr.equals("Sell")) {
                            directionStr = DirectionEnum.SELL.getDireciont();
                        }
                        tbOrigin.setDirection(directionStr);
                        break;
                    case 11:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setAvgPrice(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 8:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setTransactionAmount(
                                Double.valueOf(row.getCell(11).getStringCellValue())
                                        * Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 12:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setProfit(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    default:
                        break;
                }
            }

            resList.add(tbOrigin);
        }

        List<TbOrigin> resTbOriginTemp = calculateOriginInfo(resList);

        return resList;
    }


    public void insertBinanceData(InputStream inputStream, String requestId) throws IOException, ParseException {
        Workbook workbook = WorkbookFactory.create(inputStream);
        // 3、创建工作簿对象，并获取工作表1
        // HSSFWorkbook workbook = new XSSFWorkbook(fileSystem);
        Sheet sheet = workbook.getSheet("Sheet1");

        // 4、从工作表中获取行数，并遍历
        int lastRowIndex = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowIndex; i++) {
            // 4.1 获取每行的数据
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }

            // 5、从每一列中获取参数
            short lastCellNum = row.getLastCellNum();
            TbOrigin tbOrigin = new TbOrigin();
            tbOrigin.setRequestId(requestId);
            for (int j = 0; j < lastCellNum; j++) {
                // 设置返回值的类型
                // 获取每列的数据
                switch (j) {
                    case 0:
                        // row.getCell(j).set
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        tbOrigin.setDate(Long.toString(sdf.parse(row.getCell(j).getStringCellValue()).getTime()));
                        break;
                    case 1:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setCoin(row.getCell(j).getStringCellValue());
                        break;
                    case 2:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setDirection(row.getCell(j).getStringCellValue());
                        break;
                    case 3:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setAvgPrice(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 5:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setTransactionAmount(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 8:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setProfit(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    default:
                        break;
                }
            }

            tbOriginMapper.insert(tbOrigin);
        }
    }

    public List<TbOrigin> parseBinanceDataByMap(InputStream inputStream) throws Exception {
        List<TbOrigin> resList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        // 3、创建工作簿对象，并获取工作表1
        // HSSFWorkbook workbook = new XSSFWorkbook(fileSystem);
        Sheet sheet = workbook.getSheet("Sheet1");

        // 4、从工作表中获取行数，并遍历
        int lastRowIndex = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowIndex; i++) {
            // 4.1 获取每行的数据
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }

            // 5、从每一列中获取参数
            short lastCellNum = row.getLastCellNum();
            TbOrigin tbOrigin = new TbOrigin();
            for (int j = 0; j < lastCellNum; j++) {
                // 设置返回值的类型
                // 获取每列的数据
                switch (j) {
                    case 0:
                        // row.getCell(j).set
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        tbOrigin.setDate(Long.toString(sdf.parse(row.getCell(j).getStringCellValue()).getTime()));
                        break;
                    case 1:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setCoin(row.getCell(j).getStringCellValue());
                        break;
                    case 2:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setDirection(row.getCell(j).getStringCellValue());
                        break;
                    case 3:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setAvgPrice(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 5:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setTransactionAmount(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    case 8:
                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                        tbOrigin.setProfit(Double.valueOf(row.getCell(j).getStringCellValue()));
                        break;
                    default:
                        break;
                }
            }

            resList.add(tbOrigin);
        }

        List<TbOrigin> resTbOriginTemp = calculateOriginInfo(resList);
        return resTbOriginTemp;
    }

    public List<TbOrigin> calculateOriginInfo(List<TbOrigin> resList){
        Map<String,List<TbOrigin>> groupByMap = resList
                .stream()
                .collect(Collectors
                        .groupingBy(item -> item.getDirection() + "_" +item.getCoin() + "_" +item.getDate()));

        List<TbOrigin> resTbOriginTemp = new ArrayList<>();
        groupByMap.forEach((originKey,originVal) ->{
            Double profitTemp = 0D;
            Double amountTemp = 0D;
            TbOrigin tbOriginTemp = new TbOrigin();
            BeanUtils.copyProperties(originVal.get(0),tbOriginTemp);
            for (TbOrigin tbOriginItem : originVal){
                profitTemp =profitTemp + tbOriginItem.getProfit();
                amountTemp += tbOriginItem.getTransactionAmount();
            }
            tbOriginTemp.setProfit(profitTemp);
            tbOriginTemp.setTransactionAmount(amountTemp);
            resTbOriginTemp.add(tbOriginTemp);
        });
        return resTbOriginTemp;
    }


    public byte[] getNewData(String requestId) throws Exception {

        String resultStr = "";
        // FileOutputStream fos = new
        // FileOutputStream("D:\\code\\java\\tradefile\\tradehistory\\aaa1.xls", true);
        // true表示在文件末尾追加

        String frontStrFront = " = input.time(";
        String frontStrEnd = ", \"Date\")";
        String endStrFront = "label.new(";
        String endStrEnd = ", xloc=xloc.bar_time, textcolor=color.black ,color=color.yellow,size=size.normal,style = label.style_label_down)";

        List<TbOrigin> tbCointList = tbOriginMapper
                .selectList(new QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
        for (TbOrigin tbCoinItem : tbCointList) {
            String frontSum = "//@version=5\n indicator(\"trade_history\", overlay=true) \n";
            String endSum = "";
            System.out.println("---" + tbCoinItem.getCoin() + "---");
            List<TbOrigin> tbOriginNewList = tbOriginMapper.selectList(new QueryWrapper<TbOrigin>()
                    .select("id, date,avg_price,direction,coin,SUM(tb_origin.transaction_amount) as transaction_amount,SUM(tb_origin.profit) as profit")
                    .eq("coin", tbCoinItem.getCoin()).groupBy("date,direction"));
            // 对每一笔操作生成字符串
            for (TbOrigin item : tbOriginNewList) {
                String randomStr = createRandomStr1(8);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                frontSum += randomStr + frontStrFront + sdf.parse(item.getDate()).getTime() + frontStrEnd + "\n";
                endSum += endStrFront + randomStr + ", " + item.getAvgPrice() + ", \"" + item.getDirection() + "-"
                        + item.getTransactionAmount();
                if (item.getProfit() == 0) {
                    endSum += "\"" + endStrEnd + "\n";
                } else {
                    endSum += "-" + item.getProfit() + "\"" + endStrEnd + "\n";
                }

            }

            resultStr += ("---" + tbCoinItem.getCoin() + "---\n");
            resultStr += (frontSum + "\n");
            resultStr += (endSum + "\n");
        }
        // 删除这次的数据
        tbOriginMapper.delete(new LambdaQueryWrapper<TbOrigin>().eq(TbOrigin::getRequestId, requestId));
        return resultStr.getBytes();
        // fos.close();
    }


    public Result<String> getTradeDataStr(String requestId, String market) throws Exception {
        String LongDirection = "多";
        String ShortDirection = "空";
        String res = "";
        String direction = "";
        StringBuilder stringBuilder = new StringBuilder();

        try {


            List<TbOrigin> tbCointList = tbOriginMapper.selectList(new
                    QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
            for (TbOrigin tbCoinItem : tbCointList) {
                String coinStr = "";
                List<TbOrigin> tbOriginNewList = tbOriginMapper.selectList(new
                        QueryWrapper<TbOrigin>()
                        .select("id, date,avg_price,direction,coin,SUM(tb_origin.transaction_amount) as transaction_amount,SUM(tb_origin.profit) as profit")
                        .eq("coin", tbCoinItem.getCoin()).eq("request_id", tbCoinItem.getRequestId()).groupBy("date,direction"));
                // 对每一笔操作生成字符串
                for (TbOrigin item : tbOriginNewList) {
                    Boolean isOpen = false;
                    String text = "";
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    isOpen = item.getProfit() == 0 ? true : false;
                    direction = item.getDirection().equals("BUY") || item.getDirection().equals("买入") ? "long" : "short";

                    //设置开单or平单
                    if (isOpen) {
                        if (direction.equals("long")) {
                            text = String.format("开%s  price : %s  amout : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount());
                        } else {
                            text = String.format("开%s  price : %s  amout : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount());
                        }
                    } else {
                        if (direction.equals("long")) {
                            text = String.format("平%s  price : %s  amout : %s profit : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                        } else {
                            text = String.format("平%s  price : %s  amout : %s profit : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                        }
                    }
//                coinStr += String.format("%s:%sPERP_%s_%s_%s_%s,\n", market,item.getCoin(), sdf.parse(item.getDate()).getTime(), text, direction, isOpen);
                    coinStr += String.format("%s:%sPERP_%s_%s_%s_%s,", market, item.getCoin(), item.getDate(), text, direction, isOpen);
                }
                res += coinStr;

            }
            int delete_char_index = res.lastIndexOf(",");
            if (delete_char_index != -1) {
                stringBuilder = new StringBuilder(res);
                stringBuilder = stringBuilder.deleteCharAt(delete_char_index);
                res = stringBuilder.toString();

            }
            System.out.println(res);
            tbOriginMapper.delete(new LambdaQueryWrapper<TbOrigin>().eq(TbOrigin::getRequestId, requestId));
            return Result.ok(res);
        } catch (Exception e) {
            logger.error("解析数据发生异常", e);
            e.printStackTrace();
            return Result.failWithMsg(e.toString());
        }
    }


    public Result<String> getTradeDataStr(List<TbOrigin> tbOrigins, String market) throws Exception {
        String LongDirection = "多";
        String ShortDirection = "空";
        String res = "";
        String direction = "";
        StringBuilder stringBuilder = new StringBuilder();

        try {
            // 对每一笔操作生成字符串
            for (TbOrigin item : tbOrigins) {
                Boolean isOpen = false;
                String text = "";
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                isOpen = item.getProfit() == 0 ? true : false;
                direction = item.getDirection().equals("BUY") || item.getDirection().equals("买入") ? "long" : "short";

                //设置开单or平单
                if (isOpen) {
                    if (direction.equals("long")) {
                        text = String.format("开%s  price : %s  amout : %.2f ", LongDirection, item.getAvgPrice(), item.getTransactionAmount());
                    } else {
                        text = String.format("开%s  price : %s  amout : %.2f ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount());
                    }
                } else {
                    if (direction.equals("long")) {
                        text = String.format("平%s  price : %s  amout : %.2f profit : %.2f ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                    } else {
                        text = String.format("平%s  price : %s  amout : %.2f profit : %.2f ", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                    }
                }
//                coinStr += String.format("%s:%sPERP_%s_%s_%s_%s,\n", market,item.getCoin(), sdf.parse(item.getDate()).getTime(), text, direction, isOpen);
                res += String.format("%s:%sPERP_%s_%s_%s_%s,", market, item.getCoin(), item.getDate(), text, direction, isOpen);
            }


            int delete_char_index = res.lastIndexOf(",");
            if (delete_char_index != -1) {
                stringBuilder = new StringBuilder(res);
                stringBuilder = stringBuilder.deleteCharAt(delete_char_index);
                res = stringBuilder.toString();

            }
            System.out.println(res);
            return Result.ok(res);
        } catch (Exception e) {
            logger.error("解析数据发生异常", e);
            e.printStackTrace();
            return Result.failWithMsg("生成数据异常，请检查文件后再次尝试");
        }
    }


    public static String createRandomStr1(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(52);
            stringBuffer.append(str.charAt(number));
        }
        return stringBuffer.toString();

    }





    public byte[] getNewData2(String requestId) throws Exception {
        String LongDirection = "多";
        String ShortDirection = "空";
        String upBar = "location.abovebar";
        String downBar = "location.belowbar";
        String res = "//@version=5\n" +
                "indicator(\"input.bool\", overlay=true) \n";
        String longPicture = "shape.arrowup";
        String longColor = "#72e577";
        String shortPicture = "shape.arrowdown";
        String shortColor = "#ff5252";


        List<TbOrigin> tbCointList = tbOriginMapper.selectList(new
                QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
        for (TbOrigin tbCoinItem : tbCointList) {
            String coinStr = "//---" + tbCoinItem.getCoin() + "--- \n";
            List<TbOrigin> tbOriginNewList = tbOriginMapper.selectList(new
                    QueryWrapper<TbOrigin>()
                    .select("id, date,avg_price,direction,coin,SUM(tb_origin.transaction_amount) as transaction_amount,SUM(tb_origin.profit) as profit")
                    .eq("coin", tbCoinItem.getCoin()).groupBy("date,direction"));
            // 对每一笔操作生成字符串
            for (TbOrigin item : tbOriginNewList) {
                Boolean isOpen = false;
                Boolean isLongDirection = false;
                String location = "";
                String text = "";
                String picture = "";
                String color = "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                isOpen = item.getProfit() == 0 ? true : false;
                isLongDirection = item.getDirection().equals("买入") || item.getDirection().equals("BUY") ? true : false;
                //根据操作方向设置图标
                if (isLongDirection) {
                    picture = longPicture;
                    color = longColor;
                } else {
                    picture = shortPicture;
                    color = shortColor;
                }


                //设置开单or平单
                if (isOpen) {
                    if (isLongDirection) {
                        text = String.format("\"开%s \\n price : %s \\n amout : %s \"", LongDirection, item.getAvgPrice(), item.getTransactionAmount());
                        location = downBar;
                    } else {
                        text = String.format("\"开%s \\n price : %s \\n amout : %s \"", ShortDirection, item.getAvgPrice(), item.getTransactionAmount());
                        location = upBar;
                    }
                } else {
                    if (isLongDirection) {
                        text = String.format("\"平%s \\n price : %s \\n amout : %s\\n profit : %s \"", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                        location = downBar;
                    } else {
                        text = String.format("\"平%s \\n price : %s \\n amout : %s\\n profit : %s \"", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                        location = upBar;
                    }
                }


                coinStr += String.format("plotshape((syminfo.tickerid == \"BINANCE:%sPERP\") and (time_close - timeframe.in_seconds()*1000) <= %s and time_close > %s,text = %s, style = %s,color = %s ,textcolor = %s ,location  = %s,display = display.pane) \n"
                        , item.getCoin(), sdf.parse(item.getDate()).getTime(), sdf.parse(item.getDate()).getTime(), text, picture, color, color, location);


            }
            res += coinStr;

        }

        tbOriginMapper.delete(new LambdaQueryWrapper<TbOrigin>().eq(TbOrigin::getRequestId, requestId));
        return res.getBytes();
    }
}