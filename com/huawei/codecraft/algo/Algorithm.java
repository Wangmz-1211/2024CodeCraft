package com.huawei.codecraft.algo;

import com.huawei.codecraft.entities.Dock;
import com.huawei.codecraft.entities.Goods;
import com.huawei.codecraft.entities.GoodsBucket;
import com.huawei.codecraft.entities.Robot;
import com.huawei.codecraft.utils.Pair;

import java.util.Deque;

public interface Algorithm {
    Path findGoods(char[][] map, Robot bot, GoodsBucket goodsBucket);


    Path findDock(char[][] map, Dock[] docks, Robot bot);

}
