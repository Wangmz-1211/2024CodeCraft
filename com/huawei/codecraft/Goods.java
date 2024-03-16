package com.huawei.codecraft;

import com.huawei.codecraft.Config;
import com.huawei.codecraft.Pair;

public class Goods {

    public static final Config config = new Config();
    public int x;
    public int y;
    public int value;
    public int expire_time;

    public boolean assigned = false;

    public Goods(int x, int y, int value, int generate_time) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.expire_time = generate_time + config.T_GOODS;
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

    public Pair getPos() {
        return new Pair(x, y);
    }
}
