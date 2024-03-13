package com.huawei.codecraft.entities;

public class Ship {

    public int id = 0;
    public int num = 0;
    public int pos = 0;
    public int status = 0;
    public int value = 0;
    public int load_time = 0;

    public Ship(int id) {
        this.id = id;
    }

    public void go() {
        this.load_time = 0;
        System.out.println("go " + this.id);
    }
}
