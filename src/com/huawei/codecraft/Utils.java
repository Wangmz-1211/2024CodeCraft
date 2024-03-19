package com.huawei.codecraft;

import java.util.ArrayDeque;
import java.util.Deque;

public class Utils {

    private Config config = null;

    public Utils(Config config) {
        this.config = config;
    }

    private boolean inMap(Position pos) {
        return pos.x >= 0 && pos.x < config.S_MAP && pos.y >= 0 && pos.y < config.S_MAP;
    }

    public void mapHandler(char[][] map, Position start) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Deque<Position> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Position pos = queue.poll();
            for (int i = 0; i < 4; i++) {
                Position next = new Position(pos.x + dx[i], pos.y + dy[i]);
                if (inMap(next) && map[next.x][next.y] == '.') {
                    map[next.x][next.y] = 'A';
                    queue.add(next);
                }
            }
        }
    }
}
