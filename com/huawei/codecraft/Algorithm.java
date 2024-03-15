package com.huawei.codecraft;

public interface Algorithm {
    Path findGoods(char[][] map, Robot bot, GoodsBucket goodsBucket);


    Path findDock(char[][] map, Dock[] docks, Robot bot);

}
