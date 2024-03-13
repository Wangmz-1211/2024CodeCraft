package com.huawei.codecraft.entities;

import com.huawei.codecraft.Config;

public class Goods {
    int x;
    int y;
    int value;
    int expire_time;

    public Goods(int x, int y, int value, int generate_time) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.expire_time = generate_time + Config.T_GOODS;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "x=" + x +
                ", y=" + y +
                ", value=" + value +
                ", expire_time=" + expire_time +
                '}';
    }
}
