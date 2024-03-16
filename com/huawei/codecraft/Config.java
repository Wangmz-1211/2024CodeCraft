package com.huawei.codecraft;

import java.lang.annotation.Native;

public interface Config {
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


    // Factors to control ship logic.
    // The factors work base on ship capacity.
    double H_MIN_SHIP_LOAD_TIME = 1;
    double H_MAX_SHIP_LOAD_TIME = 2;

    // used in BFS algorithm
    @Deprecated
    int H_FIND_GOODS_MAX_DISTANCE = 3000;

    // used in A* algorithm
    int H_D_BASE_COST = 1;
    int H_D_HEURISTIC_COST = 2;
    int H_ASTAR_MAX_TIME = 6; // ms
    int H_DOCK_PUNISH = 6;

    // used in robot logic
    boolean H_BOT_FIND_GOOD_ITERATOR = true;
    boolean H_BOT_FIND_DOCK_ITERATOR = false;


}
