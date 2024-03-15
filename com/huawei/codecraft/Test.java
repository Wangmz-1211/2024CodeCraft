package com.huawei.codecraft;

import java.io.BufferedReader;
import java.io.FileReader;

public class Test {

    char[][] map = new char[Config.S_MAP][Config.S_MAP];

    public void readMap() {
        // read from map.txt by line and fill
        try {
            BufferedReader br = new BufferedReader(new FileReader("map1.txt"));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                map[i] = line.toCharArray();
                i++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test() {
        readMap();
        Utils.mapHandler(map, new Pair(60, 60));
        AStarAlgorithm algo = new AStarAlgorithm();
        Path path = algo.aStar(map, new Pair(181, 28), new Pair(44, 194));
        System.out.println(path.size());
        while (path.size() > 0) {
            Pair p = path.poll();
            map[p.x][p.y] = 'x';
        }
        for (int i = 0; i < Config.S_MAP; i++) {
            for (int j = 0; j < Config.S_MAP; j++) {
                if( map[i][j] == 'A') map[i][j] = ' ';
            }
        }
        for (int i = 0; i < Config.S_MAP; i++) {
            Logger.debug("", new String(map[i]));
        }
    }
}
