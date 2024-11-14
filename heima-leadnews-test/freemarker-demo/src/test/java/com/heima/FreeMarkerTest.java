package com.heima;

import com.heima.pojos.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author ASUS
 * @date 2024-11-12 11:22
 **/
@SpringBootTest
public class FreeMarkerTest {
    @Autowired
    private Configuration configuration;

    @Test
    public void testFreeMarker() throws Exception {
        Template template = configuration.getTemplate("02-list.ftl");
        Map map = getHashMap();
        template.process(map,new FileWriter("G:\\test.html"));
    }

    private Map getHashMap() {
        HashMap<String, Object> map = new HashMap<>();
        //小强对象模型数据
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        //向map中存放List集合数据
        map.put("stus", stus);

        HashMap<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);

        map.put("stuMap",stuMap);
        return map;
    }
}
