package com.huawei.codecraft;

public interface Algorithm {
    Path findGoods(char[][] map, Robot bot, GoodsBucket goodsBucket);

    public void preprocess(char[][] map);

    Path findDock(char[][] map, Dock[] docks, Robot bot);

}
