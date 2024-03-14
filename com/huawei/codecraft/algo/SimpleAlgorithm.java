package com.huawei.codecraft.algo;

import com.huawei.codecraft.Config;
import com.huawei.codecraft.entities.Dock;
import com.huawei.codecraft.entities.Goods;
import com.huawei.codecraft.entities.Robot;
import com.huawei.codecraft.utils.Logger;
import com.huawei.codecraft.utils.Pair;

import java.util.*;

public class SimpleAlgorithm {


    /**
     * Find the 1nn goods for the bot.
     *
     * @param map
     * @param bot
     * @param goodsArr
     * @return
     */
    public static Deque<Pair> findGoods(char[][] map, Robot bot, Goods[][] goodsArr) {

        long startTime = System.currentTimeMillis();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Map<Pair, Character> visited = new HashMap<>();
        Deque<Pair> posQueue = new ArrayDeque<>();
        posQueue.add(new Pair(bot.x, bot.y));
        Deque<Deque<Pair>> pathQueue = new ArrayDeque<>();
        pathQueue.add(new ArrayDeque<>());
        Deque<Pair> res = null;

        int goodsX = -1, goodsY = -1;
        int maxDistance = Config.H_FIND_GOODS_MAX_DISTANCE;
        int d = 0, l = 1;
        while (l > 0) {
            d += 1;
            if (d > maxDistance) break;
            for (int i = 0; i < l; i++) {
                Pair pos = posQueue.poll();
                Deque<Pair> path = pathQueue.poll();
                assert pos != null;
                assert path != null;
                if (map[pos.x][pos.y] == 'W') continue;
                if ( goodsArr[pos.x][pos.y] != null) {
                    goodsX = pos.x;
                    goodsY = pos.y;
                    path.offer(pos);
                    res = path;
                    break;
                }
                visited.put(pos, map[pos.x][pos.y]);
                map[pos.x][pos.y] = 'W';
                for (int j = 0; j < 4; j++) {
                    int newX = pos.x + dx[j], newY = pos.y + dy[j];
                    if (
                            0 <= newX && newX < Config.S_MAP &&
                                    0 <= newY && newY < Config.S_MAP &&
                                    (
                                            map[newX][newY] == '.' ||
                                                    map[newX][newY] == 'A' ||
                                                    map[newX][newY] == 'B'
                                    )
                    ) {
                        Deque<Pair> clone = clone(path);
                        clone.offer(pos);
                        posQueue.offer(new Pair(newX, newY));
                        pathQueue.offer(clone);
                    }
                }
            }
            if (goodsX > -1) break;
            l = posQueue.size();
        }
        for (Pair p : visited.keySet()) {
            map[p.x][p.y] = visited.get(p);
        }

        long endTime = System.currentTimeMillis();
        Logger.debug("[ALGO]", "Find goods time: " + (endTime - startTime));
        if (goodsX == -1) {
            return null;
        }
        res.poll();
        return res;

    }

    public static Deque<Pair> findDock(char[][] map, Dock[] docks, Robot bot) {
        Dock dock = docks[bot.id];
        Logger.info("[FIND DOCK]", "Bot " + bot.id + "at " + bot.x + " " + bot.y +
                " bfs start, try to find dock at " + dock.x + " " + dock.y + "."
        );
        Deque<Pair> path = bfs(map, bot.x, bot.y, dock.x, dock.y);
        Logger.info("[FIND DOCK]", "Bot " + bot.id + " finish bfs.");
        return path;
    }

    private static Deque<Pair> clone(Deque<Pair> path) {
        Deque<Pair> res = new ArrayDeque<>();
        for (Pair p : path) {
            res.offer(p);
        }
        return res;
    }

    public static Deque<Pair> bfs(char[][] map, int x, int y, int targetX, int targetY) {
        long startTime = System.currentTimeMillis();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Map<Pair, Character> visited = new HashMap<>();
        Deque<Pair> posQueue = new ArrayDeque<>();
        posQueue.add(new Pair(x, y));
        Deque<Deque<Pair>> pathQueue = new ArrayDeque<>();
        pathQueue.add(new ArrayDeque<>());
        Deque<Pair> res = null;
        while (!posQueue.isEmpty()) {
            Pair pos = posQueue.poll();
            Deque<Pair> path = pathQueue.poll();
            assert pos != null;
            assert path != null;
            if (map[pos.x][pos.y] == 'W') continue;
            visited.put(pos, map[pos.x][pos.y]);
            map[pos.x][pos.y] = 'W';
            if (pos.x == targetX && pos.y == targetY) {
                path.add(pos);
                res = path;
                break;
            }
            for (int i = 0; i < 4; i++) {
                int newX = pos.x + dx[i], newY = pos.y + dy[i];
                if (
                        0 <= newX && newX < Config.S_MAP &&
                                0 <= newY && newY < Config.S_MAP &&
                                (
                                        map[newX][newY] == '.' ||
                                                map[newX][newY] == 'A' ||
                                                map[newX][newY] == 'B'
                                )
                ) {
                    Deque<Pair> clone = clone(path);
                    clone.offer(pos);
                    posQueue.offer(new Pair(newX, newY));
                    pathQueue.offer(clone);
                }
            }
        }

        for (Pair p : visited.keySet()) {
            map[p.x][p.y] = visited.get(p);
        }


        long endTime = System.currentTimeMillis();
        Logger.debug("[ALGO]", "BFS time: " + (endTime - startTime) + "ms");
        if (res == null) {
            return null;
        }
        Logger.debug("[ALGO]", "BFS Depth = " + res.size());
        res.poll();
        return res;
    }
}
