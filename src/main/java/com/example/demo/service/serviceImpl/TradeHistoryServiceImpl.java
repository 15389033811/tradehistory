package com.example.demo.service.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.Result;
import com.example.demo.common.xslutil.ExeclUtil;
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
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class TradeHistoryServiceImpl implements TradeHistoryService {


    @Autowired
    ExeclUtil execlUtil;


    private static final Logger logger = LoggerFactory.getLogger(TradeHistoryServiceImpl.class);


    public Result<String> insertFileInfoByMap(InputStream inputStream, String requestId, String market, String fileName) throws Exception {
        List<TbOrigin> parseList = new ArrayList();
        try {
            if  (!fileName.endsWith("xls") && !fileName.endsWith("xlsx") && !fileName.endsWith("csv")) {
                return Result.failWithMsg("请上传后缀为xls、xlsx、csv的文件");
            }

            if (fileName.endsWith("csv")) {
                 inputStream = execlUtil.getWorkbookByCsv(inputStream);
            }
            logger.info("market {}", market);

            switch (market) {
                case "BINANCE":
                    parseList = parseBinanceDataByMap(inputStream);
                    break;
                case "OKX":
                    parseList = parseOkxDataByMap(inputStream);
                    break;
                default:
                    return Result.failWithMsg("敬请等待支持更多交易所");
            }
            logger.info("parseList {}", parseList);
            Result<String> dataRes = getTradeDataStr(parseList, market);
            System.out.println(dataRes.getResult());

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




    public List<TbOrigin> parseOkxDataByMap(InputStream inputStream) throws Exception {
        List<TbOrigin> resList = new ArrayList<>();
//        InputStream xlsInputStream = execlUtil.getWorkbookByCsv(inputStream);
        Workbook workbook = WorkbookFactory.create(inputStream);
        // 3、创建工作簿对象，并获取工作表1
        // HSSFWorkbook workbook = new XSSFWorkbook(fileSystem);
        Sheet sheet = workbook.getSheetAt(0);
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
//                        tbOrigin.setTransactionAmount(
//                                Double.valueOf(row.getCell(11).getStringCellValue())
//                                        * Double.valueOf(row.getCell(j).getStringCellValue()));
                        tbOrigin.setTransactionAmount(Double.valueOf(row.getCell(j).getStringCellValue()));
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

        //List<TbOrigin> resTbOriginTemp = calculateOriginInfo(resList);

        return resList;
    }


    public String generatorBingData(String jsonString) {
        class CustomClass {
            String name;
            Double value;
            JSONObject itemStyle;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Double getValue() {
                return value;
            }

            public void setValue(Double value) {
                this.value = value;
            }

            public JSONObject getItemStyle() {
                return itemStyle;
            }

            public void setItemStyle(JSONObject itemStyle) {
                this.itemStyle = itemStyle;
            }

            @Override
            public String toString() {
                return "{ value: " + value + ", name: \"" + name + "\", itemStyle: " + itemStyle + " }";
            }
        }

        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        List<CustomClass> customClassList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            String key = item.keySet().iterator().next();
            Double val = item.getDouble(key);

            CustomClass customClass = new CustomClass();
            customClass.setName(key);


            JSONObject itemStyle = new JSONObject();
            if (val < 0) {
                itemStyle.put("color", "#ff6600");
                val = -val;
            } else {
                itemStyle.put("color", "#66ff66");
            }
            customClass.setValue(val);
            customClass.setItemStyle(itemStyle);

            customClassList.add(customClass);

        }
        return customClassList.toString();
    }


    public List<TbOrigin> parseBinanceDataByMap(InputStream inputStream) throws Exception {
        List<TbOrigin> resList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        // 3、创建工作簿对象，并获取工作表1
        // HSSFWorkbook workbook = new XSSFWorkbook(fileSystem);
        Sheet sheet = workbook.getSheetAt(0);
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

    public List<TbOrigin> calculateOriginInfo(List<TbOrigin> resList) {

        Map<String, TbOrigin> resultMap = resList.stream()
                .collect(Collectors.toMap(
                        o -> o.getDate() + "_" + o.getCoin(),
                        o -> o,
                        (o1, o2) -> {
                            o1.setProfit(o1.getProfit() + o2.getProfit());
                            return o1;
                        }));
        List<TbOrigin> mergedObjects = new ArrayList<>(resultMap.values());


        Collections.sort(mergedObjects, new Comparator<TbOrigin>() {
            @Override
            public int compare(TbOrigin o1, TbOrigin o2) {
                return Long.compare(Long.parseLong(o1.getDate()), Long.parseLong(o2.getDate()));
            }
        });

        logger.info("折线 date {}",mergedObjects.stream().filter(e->e.getProfit() != 0).map(e-> {

            // 格式化日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return "'" +  sdf.format(Long.parseLong(e.getDate())) + ":" + e.getCoin() + ":" + (e.getDirection().equals("买入")?"空":"多") +"'";
        }).collect(Collectors.joining(",")));

        AtomicReference<Double> startProfit = new AtomicReference<>(0D);
        logger.info("折线 数据 {}",mergedObjects.stream().filter(e->e.getProfit() != 0).map(e-> {
            startProfit.updateAndGet(v -> v + e.getProfit());
            return String.valueOf(startProfit.get());
        }).collect(Collectors.joining(",")));




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


    public Result<String> getTradeDataStr(List<TbOrigin> tbOrigins, String market) throws Exception {
        String LongDirection = "多";
        String ShortDirection = "空";
        String res = "";
        String direction = "";
        StringBuilder stringBuilder = new StringBuilder();
        String amountUnit = "";
        if (market.equals(MarketEnum.BINANCE.getMarket())){
            amountUnit = "u";
        } else if (market.equals(MarketEnum.OKX.getMarket())) {
            amountUnit = "张";
        }
        HashMap<String, Double> hashMap = new HashMap();
        HashMap<String, String> coinMap = new HashMap<>();
        HashMap<String, String> sortByCoinMap = new HashMap<>();
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
                        text = String.format("开%s p:%s m:%.2f%s", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), amountUnit);
                    } else {
                        text = String.format("开%s p:%s m:%.2f%s", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), amountUnit);
                    }
                } else {
                    if (direction.equals("long")) {
                        text = String.format("平%s p:%s m:%.2f%s p:%.2fu", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), amountUnit, item.getProfit());
                    } else {
                        text = String.format("平%s p:%s m:%.2f%s p:%.2fu", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), amountUnit, item.getProfit());
                    }

                    if (hashMap.containsKey(item.getCoin())) {
                        hashMap.put(item.getCoin(), hashMap.get(item.getCoin()) + item.getProfit());
                    } else {
                        hashMap.put(item.getCoin(), item.getProfit());
                    }



                }

                if (coinMap.containsKey(item.getCoin())) {
                    coinMap.put(item.getCoin(), coinMap.get(item.getCoin()) + String.format("%s:%s.P_%s_%s_%s_%s,", market, item.getCoin(), item.getDate(), text, direction, isOpen));
                } else {
                    coinMap.put(item.getCoin(), String.format("%s:%s.P_%s_%s_%s_%s,", market, item.getCoin(), item.getDate(), text, direction, isOpen));
                }
//                coinStr += String.format("%s:%sPERP_%s_%s_%s_%s,\n", market,item.getCoin(), sdf.parse(item.getDate()).getTime(), text, direction, isOpen);
                res += String.format("%s:%s.P_%s_%s_%s_%s,", market, item.getCoin(), item.getDate(), text, direction, isOpen);
            }


            int delete_char_index = res.lastIndexOf(",");
            if (delete_char_index != -1) {
                stringBuilder = new StringBuilder(res);
                stringBuilder = stringBuilder.deleteCharAt(delete_char_index);
                res = stringBuilder.toString();

            }

            // 将 HashMap 转换为 List
            List<Map.Entry<String, Double>> list = new LinkedList<>(hashMap.entrySet());

            // 根据值（value）进行排序
            Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });

            // 将排序后的 List 转换为 LinkedHashMap
            LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Double> entry : list) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }



            System.out.println(generatorBingData(JSON.toJSONString(list)));
            logger.info("sort By Coin");
            coinMap.forEach((k,v) -> {
                logger.info(v);
            });
            return Result.ok(coinMap.toString());
        } catch (Exception e) {
            logger.error("解析数据发生异常", e);
            e.printStackTrace();
            return Result.failWithMsg("生成数据异常，请检查文件后再次尝试");
        }
    }







//    public byte[] getNewData2(String requestId) throws Exception {
//        String LongDirection = "多";
//        String ShortDirection = "空";
//        String upBar = "location.abovebar";
//        String downBar = "location.belowbar";
//        String res = "//@version=5\n" +
//                "indicator(\"input.bool\", overlay=true) \n";
//        String longPicture = "shape.arrowup";
//        String longColor = "#72e577";
//        String shortPicture = "shape.arrowdown";
//        String shortColor = "#ff5252";
//
//
//        List<TbOrigin> tbCointList = tbOriginMapper.selectList(new
//                QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
//        for (TbOrigin tbCoinItem : tbCointList) {
//            String coinStr = "//---" + tbCoinItem.getCoin() + "--- \n";
//            List<TbOrigin> tbOriginNewList = tbOriginMapper.selectList(new
//                    QueryWrapper<TbOrigin>()
//                    .select("id, date,avg_price,direction,coin,SUM(tb_origin.transaction_amount) as transaction_amount,SUM(tb_origin.profit) as profit")
//                    .eq("coin", tbCoinItem.getCoin()).groupBy("date,direction"));
//            // 对每一笔操作生成字符串
//            for (TbOrigin item : tbOriginNewList) {
//                Boolean isOpen = false;
//                Boolean isLongDirection = false;
//                String location = "";
//                String text = "";
//                String picture = "";
//                String color = "";
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                isOpen = item.getProfit() == 0 ? true : false;
//                isLongDirection = item.getDirection().equals("买入") || item.getDirection().equals("BUY") ? true : false;
//                //根据操作方向设置图标
//                if (isLongDirection) {
//                    picture = longPicture;
//                    color = longColor;
//                } else {
//                    picture = shortPicture;
//                    color = shortColor;
//                }
//
//
//                //设置开单or平单
//                if (isOpen) {
//                    if (isLongDirection) {
//                        text = String.format("\"开%s \\n price : %s \\n amout : %s \"", LongDirection, item.getAvgPrice(), item.getTransactionAmount());
//                        location = downBar;
//                    } else {
//                        text = String.format("\"开%s \\n price : %s \\n amout : %s \"", ShortDirection, item.getAvgPrice(), item.getTransactionAmount());
//                        location = upBar;
//                    }
//                } else {
//                    if (isLongDirection) {
//                        text = String.format("\"平%s \\n price : %s \\n amout : %s\\n profit : %s \"", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
//                        location = downBar;
//                    } else {
//                        text = String.format("\"平%s \\n price : %s \\n amout : %s\\n profit : %s \"", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
//                        location = upBar;
//                    }
//                }
//
//
//                coinStr += String.format("plotshape((syminfo.tickerid == \"BINANCE:%sPERP\") and (time_close - timeframe.in_seconds()*1000) <= %s and time_close > %s,text = %s, style = %s,color = %s ,textcolor = %s ,location  = %s,display = display.pane) \n"
//                        , item.getCoin(), sdf.parse(item.getDate()).getTime(), sdf.parse(item.getDate()).getTime(), text, picture, color, color, location);
//
//
//            }
//            res += coinStr;
//
//        }
//
//        tbOriginMapper.delete(new LambdaQueryWrapper<TbOrigin>().eq(TbOrigin::getRequestId, requestId));
//        return res.getBytes();
//    }
}