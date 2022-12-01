package com.wzq.rpc;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wzq
 * @create 2022-12-01 21:16
 */
public class Hello implements Serializable {

    private String message;
    private String description;

    public Hello(String message, String description) {
        this.message = message;
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
