package com.example.demo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.csvreader.CsvReader;
import com.example.demo.dao.TbOriginMapper;
import com.example.demo.entity.TbOrigin;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class Demo2ApplicationTests {
    //
//    @Autowired
//    TbOriginMapper tbOriginMapper;

    @Value("${uri-white.white-path:#{null}}")
    Map<String,Boolean> whiteList;

    @Value("${uri-white.header:}")
    String whiteHeader;


    @Test
    void contds() throws Exception{
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

        String jsonString = "[{\"MAVUSDT\":-24.1864},{\"HOTUSDT\":-19.313809980000002},{\"RENUSDT\":-10.54114998},{\"BNTUSDT\":-9.24169998},{\"BTCUSDT\":-7.0024},{\"UNFIUSDT\":-5.5490999599999995},{\"SOLUSDT\":-5.319},{\"ETHUSDT\":-4.5869},{\"BCHUSDT\":-4.18665},{\"FRONTUSDT\":-4.1548},{\"ARKMUSDT\":-3.7692000000000005},{\"STGUSDT\":-3.67489998},{\"TUSDT\":-3.23775},{\"WLDUSDT\":-2.4435000000000002},{\"BSVUSDT\":-2.258},{\"GMXUSDT\":-2.0035},{\"BANDUSDT\":-1.82835998},{\"NMRUSDT\":-1.46},{\"KLAYUSDT\":-1.0026599999999999},{\"RUNEUSDT\":-0.986},{\"PENDLEUSDT\":-0.7238},{\"LTCUSDT\":-0.3404},{\"XRPUSDT\":-0.28832},{\"TRUUSDT\":-0.2436},{\"MINAUSDT\":0.542},{\"CYBERUSDT\":0.8532},{\"BLZUSDT\":1.04832},{\"BNXUSDT\":1.13277},{\"OGNUSDT\":1.218},{\"MATICUSDT\":1.7839},{\"OPUSDT\":2.1508},{\"IOTXUSDT\":3.0626100000000003},{\"GTCUSDT\":3.4661},{\"SFPUSDT\":3.853400029999999},{\"LQTYUSDT\":5.58104},{\"ALGOUSDT\":6.26413999},{\"LUNA2USDT\":19.76909999}]\n";
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
            System.out.println("xxx" + key);

        }
        System.out.println(customClassList.toString());
    }


//    @Test
//    void contextLoads() throws Exception {
//        // 1、获取文件的路径
//        // 1.1、从桌面获取文件
//        String filePath = "D:\\code\\java\\tradefile\\tradehistory\\re1.xls";
//        // 1.2、从绝对路径获取文件
//        // String filePath = "D:\\testexcel.xls";
//        ArrayList<TbOrigin> baseInfoList =new ArrayList( );
//        // 2、通过流获取本地文件
//        FileInputStream fileInputStream = new FileInputStream(filePath);
//        BufferedInputStream bufferedInputStream = new
//                BufferedInputStream(fileInputStream);
//        POIFSFileSystem fileSystem = new POIFSFileSystem(bufferedInputStream);
//
//        // 3、创建工作簿对象，并获取工作表1
//        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem);
//        HSSFSheet sheet = workbook.getSheet("Sheet1");
//
//        // 4、从工作表中获取行数，并遍历
//        int lastRowIndex = sheet.getLastRowNum();
//        System.out.println("总行数为：" + lastRowIndex);
//        ArrayList<HashMap> list = new ArrayList<>();
//        for (int i = 1; i <= lastRowIndex; i++) {
//            // 4.1 获取每行的数据
//            HSSFRow row = sheet.getRow(i);
//            if (row == null) {
//                break;
//            }
//
//            // 5、从每一列中获取参数
//            HashMap<String, String> map = new HashMap<>();
//            short lastCellNum = row.getLastCellNum();
//            TbOrigin tbOrigin = new TbOrigin();
//            for (int j = 0; j < lastCellNum; j++) {
//                // 设置返回值的类型
//                // 获取每列的数据
//
//                switch (j) {
//                    case 0:
//                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
//                        tbOrigin.setDate(row.getCell(j).getStringCellValue());
//                        break;
//                    case 1:
//                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
//                        tbOrigin.setCoin(row.getCell(j).getStringCellValue());
//                        break;
//                    case 2:
//                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
//                        tbOrigin.setDirection(row.getCell(j).getStringCellValue());
//                        break;
//                    case 3:
//                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
//                        tbOrigin.setAvgPrice(Double.valueOf(row.getCell(j).getStringCellValue()));
//                        break;
//                    case 5:
//                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
//                        System.out.println(Double.valueOf(row.getCell(j).getStringCellValue()));
//                        tbOrigin.setTransactionAmount(Double.valueOf(row.getCell(j).getStringCellValue()));
//                        break;
//                    case 8:
//                        // row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
//                        tbOrigin.setProfit(Double.valueOf(row.getCell(j).getStringCellValue()));
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//            tbOriginMapper.insert(tbOrigin);
//            baseInfoList.add(tbOrigin);
//        }
//
//
//        Map<String,List<TbOrigin>> groupByMap = baseInfoList
//                .stream()
//                .collect(Collectors
//                        .groupingBy(item -> item.getDirection() + "_" +item.getCoin() + "_" +item.getDate()));
//
//        List<TbOrigin> resTbOriginTemp = new ArrayList<>();
//        groupByMap.forEach((originKey,originVal) ->{
//            TbOrigin tbOriginTemp = new TbOrigin();
//            BeanUtils.copyProperties(originVal.get(0),tbOriginTemp);
//            originVal.forEach((tbOriginItem) -> {
//                tbOriginTemp.setProfit(tbOriginTemp.getProfit() + tbOriginItem.getProfit());
//                tbOriginTemp.setTransactionAmount(tbOriginTemp.getTransactionAmount()+tbOriginItem.getTransactionAmount());
//            });
//            resTbOriginTemp.add(tbOriginTemp);
//        });
//
//        getNewData4(resTbOriginTemp);
//
//        // 6、关闭资源、输出封装数据
//        bufferedInputStream.close();
//
//    }


    @Test
    void getNewData4(List<TbOrigin> tbOriginNewList) throws Exception {
        String LongDirection = "多";
        String ShortDirection = "空";
        String upBar = "location.abovebar";
        String downBar = "location.belowbar";
        String res = "";
        String longPicture = "shape.arrowup";
        String longColor = "#ff5252";
        String shortPicture = "shape.arrowdown";
        String shortColor = "#72e577";
        String textColor = "";
        String direction = "";
        StringBuilder stringBuilder = new StringBuilder();

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
                isLongDirection = item.getDirection().equals("BUY") ? true : false;
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
                        text = String.format("开%s  price : %s  amout : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount());
                        location = downBar;
                    } else {
                        text = String.format("开%s  price : %s  amout : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount());
                        location = upBar;
                    }
                } else {
                    if (isLongDirection) {
                        text = String.format("平%s price : %s  amout : %s profit : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                        location = downBar;
                    } else {
                        text = String.format("平%s  price : %s  amout : %s profit : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                        location = upBar;
                    }
                }

                res += String.format("BINANCE:%sPERP_%s_%s_%s_%s,", item.getCoin(), sdf.parse(item.getDate()).getTime(), text, direction, isOpen);

//                    coinStr += String.format("plotshape((time_close - timeframe.in_seconds()*1000) <= %s and time_close > %s,text = %s, style = %s,color = %s ,textcolor = %s ,location  = %s) \n"
//                            ,sdf.parse(item.getDate()).getTime(),sdf.parse(item.getDate()).getTime(),text,picture,color,color,location);
//
//                    coinStr += String.format("plotshape(trade_time <= %s and time_close > %s,text = %s, style = %s,color = %s ,textcolor = %s ,location  = %s) \n"
//                            ,sdf.parse(item.getDate()).getTime(),sdf.parse(item.getDate()).getTime(),text,picture,color,color,location);

            }

            int delete_char_index = res.lastIndexOf(",");
            if (delete_char_index != -1) {
                stringBuilder = new StringBuilder(res);
                stringBuilder = stringBuilder.deleteCharAt(delete_char_index);
                res = stringBuilder.toString();
            }


        System.out.println(res);
    }


    //
//    @Test
//    void getNewData1() throws Exception {
//
//        FileOutputStream fos = new
//                FileOutputStream("D:\\code\\java\\tradefile\\tradehistory\\aaa.xls", true);
//        // true表示在文件末尾追加
//
//        String frontStrFront = " = input.time(";
//        String frontStrEnd = ", \"Date\")";
//        String endStrFront = "label.new(";
//        String endStrEnd = ", xloc=xloc.bar_time, textcolor=color.black,color=color.yellow,size=size.normal,style = label.style_label_down)";
//
//        List<TbOrigin> tbCointList = tbOriginMapper
//                .selectList(new
//                        QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
//        for (TbOrigin tbCoinItem : tbCointList) {
//            String frontSum = "indicator(\"input.time\", overlay=true) \n";
//            String endSum = "";
//            System.out.println("---" + tbCoinItem.getCoin() + "---");
//            List<TbOrigin> tbOriginNewList = tbOriginMapper.selectList(new
//                    QueryWrapper<TbOrigin>()
//                    .select("id, date,avg_price,direction,coin,SUM(tb_origin.transaction_amount) as transaction_amount,SUM(tb_origin.profit) as profit")
//                    .eq("coin", tbCoinItem.getCoin()).groupBy("date,direction"));
//            // 对每一笔操作生成字符串
//            for (TbOrigin item : tbOriginNewList) {
//                String randomStr = createRandomStr1(8);
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                frontSum += randomStr + frontStrFront + sdf.parse(item.getDate()).getTime() +
//                        frontStrEnd + "\n";
//                endSum += endStrFront + randomStr + ", " + item.getAvgPrice() + ", \"" +
//                        item.getDirection() + "-"
//                        + item.getTransactionAmount();
//                if (item.getProfit() == 0) {
//                    endSum += "\"" + endStrEnd + "\n";
//                } else {
//                    endSum += "-" + item.getProfit() + "\"" + endStrEnd + "\n";
//                }
//
//            }
//            System.out.println(frontSum);
//            System.out.println(endSum);
//            fos.write(("---" + tbCoinItem.getCoin() + "---\n").getBytes());
//            fos.write((frontSum + "\n").getBytes());
//            fos.write((endSum + "\n").getBytes());
//
//        }
//        fos.close();
//    }
//
//    public static String createRandomStr1(int length) {
//        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        Random random = new Random();
//        StringBuffer stringBuffer = new StringBuffer();
//        for (int i = 0; i < length; i++) {
//            int number = random.nextInt(52);
//            stringBuffer.append(str.charAt(number));
//        }
//        return stringBuffer.toString();
//
//    }
//
//    //
//    @Test
//    void getNewData() throws Exception {
//        String LongDirection = "多";
//        String ShortDirection = "空";
//        String res = "";
//        String direction = "";
//        StringBuilder stringBuilder = new StringBuilder();
//
//        List<TbOrigin> tbCointList = tbOriginMapper.selectList(new
//                QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
//        for (TbOrigin tbCoinItem : tbCointList) {
//            String coinStr = "";
//            List<TbOrigin> tbOriginNewList = tbOriginMapper.selectList(new
//                    QueryWrapper<TbOrigin>()
//                    .select("id, date,avg_price,direction,coin,SUM(tb_origin.transaction_amount) as transaction_amount,SUM(tb_origin.profit) as profit")
//                    .eq("coin", tbCoinItem.getCoin()).groupBy("date,direction"));
//            // 对每一笔操作生成字符串
//            for (TbOrigin item : tbOriginNewList) {
//                Boolean isOpen = false;
//                String text = "";
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                isOpen = item.getProfit() == 0 ? true : false;
//                direction = item.getDirection().equals("BUY") || item.getDirection().equals("买入") ? "long" : "short";
//
//                //设置开单or平单
//                if (isOpen) {
//                    if (direction.equals("long")) {
//                        text = String.format("开%s  price : %s  amout : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount());
//                    } else {
//                        text = String.format("开%s  price : %s  amout : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount());
//                    }
//                } else {
//                    if (direction.equals("long")) {
//                        text = String.format("平%s  price : %s  amout : %s profit : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
//                    } else {
//                        text = String.format("平%s  price : %s  amout : %s profit : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
//                    }
//                }
//                coinStr += String.format("BINANCE:%sPERP_%s_%s_%s_%s,\n", item.getCoin(), sdf.parse(item.getDate()).getTime(), text, direction, isOpen);
//
//            }
//            res += coinStr;
//        }
//        int delete_char_index = res.lastIndexOf(",");
//        if (delete_char_index != -1) {
//            stringBuilder = new StringBuilder(res);
//            stringBuilder = stringBuilder.deleteCharAt(delete_char_index);
//            res = stringBuilder.toString();
//
//        }
//        System.out.println(res);
//    }
//
//
//    @Test
//    void getNewData3() throws Exception {
//        String LongDirection = "多";
//        String ShortDirection = "空";
//        String upBar = "location.abovebar";
//        String downBar = "location.belowbar";
//        String res = "";
//        String longPicture = "shape.arrowup";
//        String longColor = "#ff5252";
//        String shortPicture = "shape.arrowdown";
//        String shortColor = "#72e577";
//        String textColor = "";
//        String direction = "";
//        StringBuilder stringBuilder = new StringBuilder();
//
//        List<TbOrigin> tbCointList = tbOriginMapper.selectList(new
//                QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
//        for (TbOrigin tbCoinItem : tbCointList) {
//            String coinStr = "";
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
//                isLongDirection = item.getDirection().equals("BUY") ? true : false;
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
//                        text = String.format("开%s  price : %s  amout : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount());
//                        location = downBar;
//                    } else {
//                        text = String.format("开%s  price : %s  amout : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount());
//                        location = upBar;
//                    }
//                } else {
//                    if (isLongDirection) {
//                        text = String.format("平%s price : %s  amout : %s profit : %s ", ShortDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
//                        location = downBar;
//                    } else {
//                        text = String.format("平%s  price : %s  amout : %s profit : %s ", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
//                        location = upBar;
//                    }
//                }
//
//                coinStr += String.format("BINANCE:%sPERP_%s_%s_%s_%s,", item.getCoin(), sdf.parse(item.getDate()).getTime(), text, direction, isOpen);
//
////                    coinStr += String.format("plotshape((time_close - timeframe.in_seconds()*1000) <= %s and time_close > %s,text = %s, style = %s,color = %s ,textcolor = %s ,location  = %s) \n"
////                            ,sdf.parse(item.getDate()).getTime(),sdf.parse(item.getDate()).getTime(),text,picture,color,color,location);
////
////                    coinStr += String.format("plotshape(trade_time <= %s and time_close > %s,text = %s, style = %s,color = %s ,textcolor = %s ,location  = %s) \n"
////                            ,sdf.parse(item.getDate()).getTime(),sdf.parse(item.getDate()).getTime(),text,picture,color,color,location);
//
//            }
//            res += coinStr;
//            int delete_char_index = res.lastIndexOf(",");
//            if (delete_char_index != -1) {
//                stringBuilder = new StringBuilder(res);
//                stringBuilder = stringBuilder.deleteCharAt(delete_char_index);
//                res = stringBuilder.toString();
//            }
//
//        }
//        System.out.println(res);
//    }
//
//    private static final Logger logger = LoggerFactory.getLogger(Demo2ApplicationTests.class);
//    /**
//     * csv格式的流转成xlsx格式的File
//     *
//     * @return
//     */
//    @Test
//    void getWorkbookByCsv() throws IOException {
//        OutputStream os = null;
//        SXSSFWorkbook workbook = null;
//        CsvReader reader = null;
//        File xlsFile=null;
//        String csvFilePath = "/Users/nut/code/java/demo2/222.csv";
//        try {
//
//            // 文件的编码，这里设为UTF_8
//            reader = new CsvReader(new FileInputStream(csvFilePath), ',', StandardCharsets.UTF_8);
//
//
//            ArrayList<String[]> dataList = new ArrayList<>();
//            while (reader.readRecord()) {
//                dataList.add(reader.getValues());
//            }
//
//            workbook = new SXSSFWorkbook(1024);//缓存
//            SXSSFSheet sheet = workbook.createSheet("账单页");
////      HSSFSheet sheet = (HSSFSheet) sheet1;
//
//            for (int rowNum = 0; rowNum < dataList.size(); rowNum++) {
//                String[] data = dataList.get(rowNum);
//                SXSSFRow row = sheet.createRow(rowNum);
//                for (int columnNum = 0; columnNum < data.length; columnNum++) {
//                    SXSSFCell cell = row.createCell(columnNum);
//                    cell.setCellValue(data[columnNum]);
//                }
//            }
////      sheet.autoSizeColumn();
//
//            String xlsPath = csvFilePath.replaceAll(".csv", ".xlsx");
//            xlsFile = new File(xlsPath);
//            xlsFile.createNewFile();
//            os = new FileOutputStream(xlsFile);
//            workbook.write(os);
//
//        } catch (Exception e) {
//            logger.error("j",e);
//        } finally {
//            try {
//                workbook.close();
//                workbook.dispose();//删除临时文件
//                reader.close();
//                os.close();
//            } catch (Exception e) {
//                logger.error("j",e);
//            }
//        }
//    }
}
