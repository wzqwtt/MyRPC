package com.wzq.rpc;

import java.util.List;

/**
 * @author wzq
 * @create 2022-12-05 21:04
 */
public interface StudentService {

    /**
     * 打印学生信息
     */
    void printInfo(Student student);

    /**
     * 创造n名学生
     *
     * @param n n名学生
     * @return 返回一个List
     */
    List<Student> makeNStudent(int n);

}
