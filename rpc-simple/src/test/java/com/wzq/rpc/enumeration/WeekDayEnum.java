package com.wzq.rpc.enumeration;

/**
 * @author wzq
 * @create 2022-12-02 16:15
 */
public class WeekDayEnum {

    public static void main(String[] args) {
        for (WeekDay value : WeekDay.values()) {
            System.out.println(value.getDay());
        }
    }
}

enum WeekDay {
    // 枚举成员
    Mon("Monday"),Tue("Tuesday"),Wed("Wednesday"),Thu("Thursday"),Fri("Friday"),Sat("Saturday"),Sun("Sunday");

    private final String day;

    private WeekDay(String day) {
        this.day = day;
    }

    public String getDay() {
        return day;
    }
}