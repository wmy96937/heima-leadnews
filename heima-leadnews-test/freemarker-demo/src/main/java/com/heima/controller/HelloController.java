package com.heima.controller;

import com.heima.pojos.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author ASUS
 * @date 2024-11-12 09:43
 **/
@Controller
public class HelloController {

    @GetMapping("/basic")
    public String testBasic(Model model){
        model.addAttribute("name","Hello World!");
        Student student = new Student();
        student.setName("小明");
        student.setAge(18);
        student.setBirthday(new Date());
        student.setMoney(1000.0f);

        model.addAttribute("stu",student);

        return "basic";
    }

    @GetMapping("/list")
    public String testList(Model model){

        Student s1 = new Student("小明",18,new Date(),168.0f);
        Student s2 = new Student("小红",19,new Date(),351.0f);
        Student s3 = new Student("小王",20,new Date(),140.0f);
        Student s4 = new Student("小张",21,new Date(),180.0f);
        Student s5= new Student("小李",22,new Date(),1600.0f);
        Student s6 = new Student("小白",23,new Date(),3200.0f);
        ArrayList<Object> stus = new ArrayList<>();
        stus.add(s1);
        stus.add(s2);
        stus.add(s3);
        stus.add(s4);
        stus.add(s5);
        stus.add(s6);
        model.addAttribute("stus",stus);


        HashMap<String, Student> map = new HashMap<>();
        map.put("stu1",s1);
        map.put("stu2",s2);
        model.addAttribute("stuMap",map);
        return "02-list";
    }
}
