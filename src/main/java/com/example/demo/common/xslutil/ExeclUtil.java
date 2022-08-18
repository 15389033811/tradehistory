package com.example.demo.common.xslutil;

import com.csvreader.CsvReader;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class ExeclUtil {


    public InputStream getWorkbookByCsv(InputStream inputStream) throws IOException {
        OutputStream os = null;
        SXSSFWorkbook workbook = null;
        CsvReader reader = null;
        try {

            // 文件的编码，这里设为UTF_8
            reader = new CsvReader(inputStream, ',', StandardCharsets.UTF_8);


            ArrayList<String[]> dataList = new ArrayList<>();
            while (reader.readRecord()) {
                dataList.add(reader.getValues());
            }

            workbook = new SXSSFWorkbook(1024);//缓存
            SXSSFSheet sheet = workbook.createSheet("Sheet1");
//      HSSFSheet sheet = (HSSFSheet) sheet1;

            for (int rowNum = 0; rowNum < dataList.size(); rowNum++) {
                String[] data = dataList.get(rowNum);
                SXSSFRow row = sheet.createRow(rowNum);
                for (int columnNum = 0; columnNum < data.length; columnNum++) {
                    SXSSFCell cell = row.createCell(columnNum);
                    cell.setCellValue(data[columnNum]);
                }
            }
//      sheet.autoSizeColumn();


//            File xlsFile = new File("/Users/nut/code/java/demo2/sgdsag.xlsx");
//            xlsFile.createNewFile();
//            os = new FileOutputStream(xlsFile);
//            workbook.write(os);


            //临时缓冲区
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //创建临时文件
            workbook.write(out);



            byte [] bookByteAry = out.toByteArray();
            InputStream  resInputStream = new ByteArrayInputStream(bookByteAry);
            return resInputStream;
        } catch (Exception e) {
            return null;
        }
    }
}
