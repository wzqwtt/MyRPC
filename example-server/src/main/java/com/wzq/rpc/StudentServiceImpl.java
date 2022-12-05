package com.wzq.rpc;

import java.util.*;

/**
 * @author wzq
 * @create 2022-12-05 21:06
 */
public class StudentServiceImpl implements StudentService {

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
