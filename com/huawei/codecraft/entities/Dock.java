package com.huawei.codecraft.entities;

public class Dock {

    public int id;
    public int x;
    public int y;
    public int transport_time;
    public int loading_speed;
    public double score;

    public Dock() {
    }

    public Dock(int id, int x, int y, int transport_time, int loading_speed) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.transport_time = transport_time;
        this.loading_speed = loading_speed;
        this.score = (double) loading_speed / transport_time;
    }
}
