package com.wzq.rpc;

import lombok.*;

import java.io.Serializable;

/**
 * @author wzq
 * @create 2022-12-05 21:03
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Student implements Serializable {

    private String name;
    private int age;

}
