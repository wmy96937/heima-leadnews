package com.heima.wemedia;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

/**
 * @author ASUS
 * @date 2024-11-15 10:38
 **/
@SpringBootTest
public class TextTest {
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private FileStorageService fileStorage;

    @Test
    public void testTextScan() throws Exception {
        String text = "毒贩正在吸食冰毒，旁边有人打架";
        System.out.println(greenTextScan.greeTextScan(text));
    }

    @Test
    public void testImage() throws Exception{
        byte[] bytes = fileStorage.downLoadFile("http://192.168.200.130:9000/leadnews/2021/04/23/ak47.jpg");
       Map map= greenImageScan.imageScan(Arrays.asList(bytes));
    }
}
