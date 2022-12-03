package com.wzq.rpc;

import lombok.*;

import java.io.Serializable;

/**
 * @author wzq
 * @create 2022-12-01 21:16
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {

    private String message;
    private String description;

}
