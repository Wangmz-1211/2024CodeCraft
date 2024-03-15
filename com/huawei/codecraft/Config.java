package com.huawei.codecraft;

public interface Config {
    int S_MAP = 200;
    int N_ROBOT = 10;
    int N_DOCK = 10;
    int N_SHIP = 5;
    int N_GOOD = 210;
    int N_FRAME = 15000;
    int T_GOODS = 1000;
    int DEBUG_LEVEL = 2;
    int H_MIN_SHIP_LOAD_TIME = 90;
    int H_MAX_SHIP_LOAD_TIME = 120;
    int H_FIND_DOCK_K1 = 5;
    int H_FIND_DOCK_K2 = 5;
    int H_FIND_GOODS_K1 = 2;
    int H_FIND_GOODS_K2 = 1;

    // used in BFS algorithm
    int H_FIND_GOODS_MAX_DISTANCE = 3000;
    int H_N_GOODS_BUCKET = 10;

    // used in A* algorithm
    int H_D_BASE_COST = 10;
    int H_D_HEURISTIC_COST = 20;
    int H_ASTAR_MAX_TIME = 12; // ms
}
