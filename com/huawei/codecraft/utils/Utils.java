package com.huawei.codecraft.utils;

import com.huawei.codecraft.Config;

import java.awt.font.LineMetrics;
import java.util.ArrayDeque;
import java.util.Deque;

public class Utils {

    private static boolean inMap(Pair pos) {
        return pos.x >= 0 && pos.x < Config.S_MAP && pos.y >= 0 && pos.y < Config.S_MAP;
    }

    public static void mapHandler(char[][] map, Pair start) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Deque<Pair> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Pair pos = queue.poll();
            for (int i = 0; i < 4; i++) {
                Pair next = new Pair(pos.x + dx[i], pos.y + dy[i]);
                if ( inMap(next) && map[next.x][next.y] == '.') {
                    map[next.x][next.y] = 'A';
                    queue.add(next);
                }
            }
        }
    }
}
