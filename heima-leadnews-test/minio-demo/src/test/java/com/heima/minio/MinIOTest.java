package com.heima.minio;

import com.heima.file.service.impl.MinIOFileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
@SpringBootTest
public class MinIOTest {

    @Autowired
    private MinIOFileStorageService minIOFileStorageService;

    @Test
    public void testUpload() throws Exception{
        FileInputStream fileInputStream = new FileInputStream("G:\\test.html");

        String path = minIOFileStorageService.uploadHtmlFile("","list.html",fileInputStream);
        System.out.println(path);
    }



}