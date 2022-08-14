package com.example.demo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.dao.TbOriginMapper;
import com.example.demo.entity.TbOrigin;
import com.example.demo.service.TradeHistoryService;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

@Service
public class TradeHistoryServiceImpl implements TradeHistoryService {

    @Autowired
    TbOriginMapper tbOriginMapper;

    @Transactional
    public byte[] insertFileInfo(InputStream inputStream, String requestId) throws Exception {
        // 1、获取文件的路径
        // 1.1、从桌面获取文件
        // 1.2、从绝对路径获取文件
        // String filePath = "D:\\testexcel.xls";

        // 2、通过流获取本地文件
        // FileInputStream fileInputStream = new FileInputStream(filePath);
        // BufferedInputStream bufferedInputStream = new
        // BufferedInputStream(inputStream);
        // POIFSFileSystem fileSystem = new POIFSFileSystem(bufferedInputStream);
        Workbook workbook = WorkbookFactory.create(inputStream);
        // 3、创建工作簿对象，并获取工作表1
        // HSSFWorkbook workbook = new XSSFWorkbook(fileSystem);
        Sheet sheet = workbook.getSheet("Sheet1");

        // 4、从工作表中获取行数，并遍历
        int lastRowIndex = sheet.getLastRowNum();
        System.out.println("总行数为：" + lastRowIndex);
        ArrayList<HashMap> list = new ArrayList<>();
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
                        tbOrigin.setDate(row.getCell(j).getStringCellValue());
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
                        System.out.println(Double.valueOf(row.getCell(j).getStringCellValue()));
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

        return getNewData2(requestId);

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



    public byte[] getNewData2(String requestId) throws Exception {
        String LongDirection = "多";
        String ShortDirection = "空";
        String upBar = "location.abovebar";
        String downBar = "location.belowbar";
        String res = "";
        String longPicture = "shape.arrowup";
        String longColor = "#72e577";
        String shortPicture = "shape.arrowdown";
        String shortColor = "#ff5252";


        List<TbOrigin> tbCointList = tbOriginMapper.selectList(new
                QueryWrapper<TbOrigin>().lambda().groupBy(TbOrigin::getCoin));
        for (TbOrigin tbCoinItem : tbCointList) {
            String coinStr = "---" + tbCoinItem.getCoin() + "--- \n";
            List<TbOrigin> tbOriginNewList = tbOriginMapper.selectList(new
                    QueryWrapper<TbOrigin>()
                    .select("id, date,avg_price,direction,coin,SUM(tb_origin.transaction_amount) as transaction_amount,SUM(tb_origin.profit) as profit")
                    .eq("coin", tbCoinItem.getCoin()).groupBy("date,direction"));
            // 对每一笔操作生成字符串
            for (TbOrigin item : tbOriginNewList) {
                Boolean isOpen = false;
                Boolean isLongDirection = false;
                String location = "";
                String text ="";
                String picture = "";
                String color = "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                isOpen = item.getProfit() == 0?true:false;
                isLongDirection = item.getDirection().equals("BUY")?true:false;
                //根据操作方向设置图标
                if (isLongDirection){
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
                    if  (isLongDirection) {
                        text = String.format("\"平%s \\n price : %s \\n amout : %s\\n profit : %s \"", ShortDirection ,item.getAvgPrice(),item.getTransactionAmount(),item.getProfit());
                        location = downBar;
                    } else {
                        text = String.format("\"平%s \\n price : %s \\n amout : %s\\n profit : %s \"", LongDirection, item.getAvgPrice(), item.getTransactionAmount(), item.getProfit());
                        location = upBar;
                    }
                }



                coinStr += String.format("plotshape((time_close - timeframe.in_seconds()*1000) <= %s and time_close > %s,text = %s, style = %s,color = %s ,textcolor = %s ,location  = %s) \n"
                        ,sdf.parse(item.getDate()).getTime(),sdf.parse(item.getDate()).getTime(),text,picture,color,color,location);


            }
            res += coinStr;

        }

        tbOriginMapper.delete(new LambdaQueryWrapper<TbOrigin>().eq(TbOrigin::getRequestId, requestId));
        return res.getBytes();
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

}