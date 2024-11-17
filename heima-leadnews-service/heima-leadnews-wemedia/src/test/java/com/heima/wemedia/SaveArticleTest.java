package com.heima.wemedia;

import com.heima.wemedia.service.WmNewsAutoScanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ASUS
 * @date 2024-11-15 17:22
 **/
@SpringBootTest
public class SaveArticleTest {
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Test
    public void testAutoScan(){
        wmNewsAutoScanService.autoScanWmNews(6241);
    }
}
