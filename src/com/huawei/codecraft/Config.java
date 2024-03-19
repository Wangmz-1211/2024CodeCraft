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
    int H_D_BASE_COST = 1;
    int H_D_HEURISTIC_COST = 1;

    /**
     * The following factors are all hyper-parameters.
     * They are used to control the logic of the program.
     */
    //Ship logic

    // When ship.capacity - ship.num <= H_SHIP_CAPACITY_THRESHOLD, the ship will go to the dock.
    int H_SHIP_CAPACITY_THRESHOLD = 80;
    // When ship.load_time >= H_MAX_SHIP_LOAD_TIME, the efficiency is low, go sell.
    double H_MAX_SHIP_LOAD_TIME = .3;
    // When ship.redirect >= H_SHIP_REDIRECT_THRESHOLD, the ship won't go to another dock.
    int H_SHIP_REDIRECT_THRESHOLD = 1;

    // A* algorithm
    int H_ASTAR_MAX_TIME_GOODS = 2; // ms
    int H_ASTAR_MAX_TIME_DOCK = 2; // ms

    // Robot logic
    int H_DOCK_PUNISH = 1;
    boolean H_BOT_FIND_GOOD_ITERATOR = false;
    boolean H_BOT_FIND_DOCK_ITERATOR = false;


    public Config() {
    }
}
