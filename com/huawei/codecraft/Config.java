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
    int DEBUG_LEVEL = 0;
    int N_GOODS_BUCKET = 10;
    int H_D_BASE_COST = 1;
    int H_D_HEURISTIC_COST = 2;

    /**
     * The following factors are all hyper-parameters.
     * They are used to control the logic of the program.
     */
    //Ship logic
    double H_MIN_SHIP_LOAD_TIME = 1;
    double H_MAX_SHIP_LOAD_TIME = 2;

    // A* algorithm
    int H_ASTAR_MAX_TIME = 6; // ms

    // Robot logic
    int H_DOCK_PUNISH = 6;
    boolean H_BOT_FIND_GOOD_ITERATOR = true;
    boolean H_BOT_FIND_DOCK_ITERATOR = false;

    public Config() {
    }
}
