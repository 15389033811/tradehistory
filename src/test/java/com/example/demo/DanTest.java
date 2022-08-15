package com.example.demo;


import com.example.demo.entity.TbOrigin;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

@SpringBootTest
public class DanTest {
//
//    @Test
//    void contextLoads() throws Exception {
//        //        1、获取文件的路径
////        1.1、从桌面获取文件
//        FileSystemView fsv = FileSystemView.getFileSystemView();
//        String desktop = fsv.getHomeDirectory().getPath();
//        String filePath = desktop + "/code/java/demo2/ttt.xls";
////        1.2、从绝对路径获取文件
////        String filePath = "D:\\testexcel.xls";
//
////        2、通过流获取本地文件
//        FileInputStream fileInputStream = new FileInputStream(filePath);
//        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//        POIFSFileSystem fileSystem = new POIFSFileSystem(bufferedInputStream);
//
////        3、创建工作簿对象，并获取工作表1
//        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem);
//        HSSFSheet sheet = workbook.getSheet("Sheet0");
//
////        4、从工作表中获取行数，并遍历
//        int lastRowIndex = sheet.getLastRowNum();
//        System.out.println("总行数为：" + lastRowIndex);
//        ArrayList<HashMap> list = new ArrayList<>();
//        for (int i = 4; i <= lastRowIndex; i++) {
////            4.1 获取每行的数据
//            HSSFRow row = sheet.getRow(i);
//            if (row == null) {
//                break;
//            }
//
//            int j = 1;
////                设置返回值的类型
////                获取每列的数据
//            row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
//            System.out.println(row);
//
//        }
////        6、关闭资源、输出封装数据
//        bufferedInputStream.close();
//        System.out.println(list.toString());
//
//    }

}
