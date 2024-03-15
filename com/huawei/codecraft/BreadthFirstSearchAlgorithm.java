package com.huawei.codecraft;

import java.util.*;

public class BreadthFirstSearchAlgorithm implements Algorithm {


    /**
     * Find the 1nn goods for the bot.
     *
     * @param map
     * @param bot
     * @param goodsArr
     * @return
     */
    public Path findGoods(char[][] map, Robot bot, GoodsBucket goodsBucket) {
        Goods[][] goodsArr = goodsBucket.goodsArr;
        long startTime = System.currentTimeMillis();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Map<Pair, Character> visited = new HashMap<>();
        Deque<Pair> posQueue = new ArrayDeque<>();
        posQueue.add(new Pair(bot.x, bot.y));
        Deque<Path> pathQueue = new ArrayDeque<>();
        pathQueue.add(new Path(new ArrayDeque<>()));
        Path res = null;

        int goodsX = -1, goodsY = -1;
        int maxDistance = Config.H_FIND_GOODS_MAX_DISTANCE;
        int d = 0, l = 1;
        while (l > 0) {
            d += 1;
            if (d > maxDistance) break;
            for (int i = 0; i < l; i++) {
                Pair pos = posQueue.poll();
                Path path = pathQueue.poll();
                assert pos != null;
                assert path != null;
                if (map[pos.x][pos.y] == 'W') continue;
                if (goodsArr[pos.x][pos.y] != null) {
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
                    if (isValidPosition(map, newX, newY)) {
                        Path clone = path.cloneAndAppend(pos);
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


    private boolean isValidPosition(char[][] map, int x, int y) {
        if (x < 0 || y < 0 || x >= map.length || y >= map[0].length) {
            return false;
        }
        return isObstacle(map, x, y);
    }

    private boolean isValidPosition(char[][] map, Pair pair) {
        return isValidPosition(map, pair.x, pair.y);
    }

    private boolean isObstacle(char[][] map, int x, int y) {
        return map[x][y] != '#' && map[x][y] != '*' && map[x][y] != 'W' && map[x][y] != '.';
    }

    private boolean isObstacle(char[][] map, Pair pair) {
        return isObstacle(map, pair.x, pair.y);
    }


    public Path findDock(char[][] map, Dock[] docks, Robot bot) {
        Dock dock = docks[bot.id];
        Logger.info("[FIND DOCK]", "Bot " + bot.id + "at " + bot.x + " " + bot.y +
                " bfs start, try to find dock at " + dock.x + " " + dock.y + "."
        );
        Path path = bfs(map, bot.x, bot.y, dock.x, dock.y);
        Logger.info("[FIND DOCK]", "Bot " + bot.id + " finish bfs.");
        return path;
    }


    /**
     * This is a simple bfs algorithm, finding a path from (x, y) to (targetX, targetY).
     *
     * @param map
     * @param x
     * @param y
     * @param targetX
     * @param targetY
     * @return
     */
    public Path bfs(char[][] map, int x, int y, int targetX, int targetY) {
        long startTime = System.currentTimeMillis();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Map<Pair, Character> visited = new HashMap<>();
        Deque<Pair> posQueue = new ArrayDeque<>();
        posQueue.add(new Pair(x, y));
        Deque<Path> pathQueue = new ArrayDeque<>();
        pathQueue.add(new Path(new ArrayDeque<>()));
        Path res = null;
        while (!posQueue.isEmpty()) {
            Pair pos = posQueue.poll();
            Path path = pathQueue.poll();
            assert pos != null;
            assert path != null;
            if (map[pos.x][pos.y] == 'W') continue;
            visited.put(pos, map[pos.x][pos.y]);
            map[pos.x][pos.y] = 'W';
            if (pos.x == targetX && pos.y == targetY) {
                // todo
                path.offer(pos);
                res = path;
                break;
            }
            for (int i = 0; i < 4; i++) {
                int newX = pos.x + dx[i], newY = pos.y + dy[i];
                if (isValidPosition(map, newX, newY)) {
                    Path clone = path.cloneAndAppend(pos);
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
