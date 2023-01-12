package com.wzq.rpc.proxy.common;

/**
 * 水果代理商、经销商
 *
 * @author wzq
 * @create 2023-01-12 21:15
 */
public class Dealer implements Sales {

    public FruitGrower fruitGrower;

    public Dealer(FruitGrower fruitGrower) {
        this.fruitGrower = fruitGrower;
    }

    @Override
    public void sellFruit() {
        if (fruitGrower == null) {
            this.fruitGrower = fruitGrower;
        }
        // 售卖前涨价
        fruitGrower.sellFruit();
        // 售卖后处理
    }
}
