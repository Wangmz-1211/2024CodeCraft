package com.huawei.codecraft;

import java.lang.annotation.Native;

public class Config {
    int S_MAP = 200;
    int N_ROBOT = 10;
    int N_DOCK = 10;
    int N_SHIP = 5;

    int N_GOOD = 210;
    int N_FRAME = 15000;
    int T_GOODS = 1000;
    // -1 for prod, 0 for error, 1 for info, 2 for debug
    int DEBUG_LEVEL = 2;
    int N_GOODS_BUCKET = 10;

    /**
     * The following factors are all hyper-parameters.
     * They are used to control the logic of the program.
     */
    //Ship logic

    // When ship.capacity - ship.num <= H_SHIP_CAPACITY_THRESHOLD, the ship will go to the dock.
    int H_SHIP_CAPACITY_THRESHOLD = 100;
    // When ship.load_time >= H_MAX_SHIP_LOAD_TIME, the efficiency is low, go sell.
    double H_MAX_SHIP_LOAD_TIME = 1;
    // When ship.redirect >= H_SHIP_REDIRECT_THRESHOLD, the ship won't go to another dock.
    int H_SHIP_REDIRECT_THRESHOLD = 1;

    // Robot logic
    int H_DOCK_PUNISH = 1;

    int H_S_REGION = 16;

    int GRID_SHIFT_X = 4;
    int GRID_SHIFT_Y = 3;
    public Config() {
    }
}
