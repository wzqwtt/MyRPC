package com.wzq.rpc.service;

import com.wzq.rpc.Student;
import com.wzq.rpc.StudentService;
import com.wzq.rpc.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author wzq
 * @create 2022-12-05 21:06
 */
@Slf4j
@RpcService
public class StudentServiceImpl implements StudentService {

    static {
        System.out.println("StudentServiceImpl被注册了");
    }

    private static final String[] names = new String[]{
            "张三", "李四", "王五", "钱六", "wzq", "wtt", "wzqwtt"
    };

    @Override
    public void printInfo(Student student) {
        System.out.println("学生的姓名为:" + student.getName() + ", 今年" + student.getAge() + "岁了。");
    }

    @Override
    public List<Student> makeNStudent(int n) {
        ArrayList<Student> students = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            int random = r.nextInt(names.length);
            students.add(new Student(names[random], random));
        }

        return students;
    }
}
