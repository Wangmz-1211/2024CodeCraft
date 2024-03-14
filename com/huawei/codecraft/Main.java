/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

import com.huawei.codecraft.algo.AStarAlgorithm;
import com.huawei.codecraft.algo.Algorithm;
import com.huawei.codecraft.algo.Path;
import com.huawei.codecraft.entities.*;
import com.huawei.codecraft.utils.Logger;
import com.huawei.codecraft.utils.Pair;
import com.huawei.codecraft.algo.BreadthFirstSearchAlgorithm;
import com.huawei.codecraft.utils.Utils;

import java.util.*;

/**
 * Main
 *
 * @since 2024-02-05
 */
public class Main {

    private int money, boat_capacity, id;
    private final String[] ch = new String[Config.S_MAP];
    private final char[][] map = new char[Config.S_MAP][Config.S_MAP];

    private final Robot[] robots = new Robot[Config.N_ROBOT];
    private final Dock[] docks = new Dock[Config.N_DOCK];
    private Dock[] docksBetter = new Dock[5];
    private Dock[] docksWorse = new Dock[5];
    private final Ship[] ships = new Ship[Config.N_SHIP];
    private final GoodsBucket goodsBucket = new GoodsBucket();

    private void init() throws Exception {
        Scanner scanf = new Scanner(System.in);
        for (int i = 0; i < Config.S_MAP; i++) {
            ch[i] = scanf.nextLine();
        }
        Logger.info("[INIT]", "Map loaded");
        for (int i = 0; i < Config.N_DOCK; i++) {
            Dock dock = new Dock();
            id = scanf.nextInt();
            docks[id] = dock;
            dock.id = id;
            dock.x = scanf.nextInt();
            dock.y = scanf.nextInt();
            dock.transport_time = scanf.nextInt();
            dock.loading_speed = scanf.nextInt();
            dock.score = (double) dock.loading_speed / dock.transport_time;
        }
        Logger.info("[INIT]", "Docks loaded");
        this.boat_capacity = scanf.nextInt();
        Logger.info("[INIT]", "Boat capacity " + boat_capacity);

        /*Other Logics*/
        // Initialize map information
        List<Pair> botPos = new ArrayList<>();
        for (int i = 0; i < Config.S_MAP; i++) {
            for (int j = 0; j < Config.S_MAP; j++) {
                char c = ch[i].charAt(j);
                if (c == 'A') {
                    botPos.add(new Pair(i, j));
                }
                map[i][j] = c;
            }
        }
        for (Pair p : botPos) {
            Utils.mapHandler(map, p);
        }
        for (char[] cs : map) {
            Logger.debug("[MAP]", new String(cs));
        }
        // Initialize Robots
        for (int i = 0; i < Config.N_ROBOT; i++) {
            Robot robot = new Robot(i);
            robot.docks = docks;
            robots[i] = robot;
        }
        Logger.info("[INIT]", "Robots initialized.");

        // Initialize Ships
        for (int i = 0; i < Config.N_SHIP; i++) {
            ships[i] = new Ship(i);
        }
        Logger.info("[INIT]", "Ships initialized.");

        // evaluate docks
        Dock[] docksSort = docks.clone();
        Arrays.sort(docksSort,
                (Dock a, Dock b) -> Double.compare(b.score, a.score
                ));
        docksBetter = Arrays.copyOfRange(docksSort, 0, 5);
        docksWorse = Arrays.copyOfRange(docksSort, 5, 10);
        Logger.info("[INIT]", "Docks evaluated.");

        // Finish
        String okk = scanf.nextLine();
        System.out.println("OK");
        System.out.flush();
    }

    private int input() {
        Scanner scanf = new Scanner(System.in);
        //  frameId
        this.id = scanf.nextInt();
        Logger.debug("[INPUT]", "Frame " + id);
        this.money = scanf.nextInt();
        Logger.debug("[INPUT]", "Money " + money);
        // Update goods
        int num = scanf.nextInt();
        Logger.debug("[INPUT]", "Goods " + num);
        for (int i = 1; i <= num; i++) {
            int x = scanf.nextInt();
            int y = scanf.nextInt();
            int val = scanf.nextInt();
            if (map[x][y] != 'A') continue;
            goodsBucket.add(new Goods(x, y, val, id));
        }
        Logger.info("[INPUT]", "Goods updated");
        // Update robots
        for (int i = 0; i < Config.N_ROBOT; i++) {
            int goods = scanf.nextInt();
            int x = scanf.nextInt();
            int y = scanf.nextInt();
            int status = scanf.nextInt();
            robots[i].update(x, y, goods, status);
        }
        Logger.info("[INPUT]", "Robots updated");
        for (int i = 0; i < 5; i++) {
            Ship ship = ships[i];
            ship.status = scanf.nextInt();
            ship.pos = scanf.nextInt();
        }
        Logger.info("[INPUT]", "Ships updated");
        String okk = scanf.nextLine();
        return id;
    }

    public static void main(String[] args) {

        new AStarAlgorithm().test();

        try {
            Logger.info("[INIT]", "Start");
            Main mainInstance = new Main();
            Algorithm algo = new AStarAlgorithm();
            mainInstance.init();
            for (int frame = 1; frame <= 15000; frame++) {
                Logger.info("[FRAME]", "==============================IN==============================");
                long startTime = System.currentTimeMillis();
                int frameId = mainInstance.input();
                Logger.info("[FRAME]", "==============================OP==============================");
                mainInstance.goodsBucket.clean(frameId);

                // Robot Logic
                for (int i = 0; i < Config.N_ROBOT; i++) {
                    Robot bot = mainInstance.robots[i];
                    if (bot.status == 0) { // bot is stuck
                        bot.path = null;
                        bot.targetDock = -1;
                    } else { // bot is running normally
                        if (bot.goods == 0) { // bot  is  empty
                            bot.targetDock = -1;
                            Goods goods = mainInstance.goodsBucket.get(bot.x, bot.y);
                            if (goods != null) { // bot is standing on a goods, take it.
                                bot.get(goods);
                                mainInstance.goodsBucket.remove(goods);
                                Logger.debug("[ROBOT]", "Robot " + bot.id + " got goods.");
                                continue;
                            }
                            if (bot.path == null && frameId % 10 == bot.id) { // find path
                                Path path = algo.findGoods(mainInstance.map, bot, mainInstance.goodsBucket);
                                if (path == null) {
                                    Logger.debug("[ROBOT]", "Robot " + bot.id + " found no path.");
                                    continue;
                                }
                                bot.path = path;
                            }
                            if (bot.path != null) {
                                bot.movePath();
                                Logger.debug("[ROBOT]", "Robot " + bot.id + " moved.");
                            }

                        } else { // bot has goods
                            if (mainInstance.map[bot.x][bot.y] == 'B') {
                                bot.pull();
                                continue;
                            }
                            if (bot.targetDock == -1 || bot.path == null) {
                                // find a dock
                                Path path = algo.findDock(mainInstance.map, mainInstance.docks, bot);
                                if (path == null) {
                                    Logger.debug("[ROBOT]", "Robot " + bot.id + " found no dock.");
                                    continue;
                                }
                                bot.targetDock = 1;
                                bot.path = path;
                            }
                            if (bot.targetDock > -1) {
                                bot.movePath();
                            }


                        }
                    }
                }

                // Ship Logic
                for (int i = 0; i < Config.N_SHIP; i++) {
                    Ship ship = mainInstance.ships[i];
                    if (ship.status == 0) { // Ship is moving
                        Logger.debug("[SHIP]", "Ship " + ship.id + " is moving.");
                    } else if (ship.status == 1) {
                        if (ship.pos == -1) { // Ship has sold goods, go dock
                            ship.ship(mainInstance.docksBetter[i].id);
                            Logger.info("[SHIP]", "Ship " + ship.id + " is going to dock " + mainInstance.docksBetter[i].id);
                            continue;
                        }
                        ship.load_time += 1;
                        if (ship.load_time >= Config.H_MAX_SHIP_LOAD_TIME) {
                            ship.go();
                            Logger.info("[SHIP]", "Ship " + ship.id + " is going to " + ship.pos);
                        }
                    } else {
                        ship.ship(mainInstance.docksWorse[i].id);
                        Logger.info("[SHIP]", "Ship " + ship.id + " is going to dock " + mainInstance.docksWorse[i].id);
                    }
                }


                long frameTime = System.currentTimeMillis() - startTime;
                Logger.info("[F TIME]", "Frame " + frameId + " finished in " + frameTime + "ms");
                if (frameTime > 15) {
                    Logger.error("[F TIME]", "Frame " + frameId + " finished in " + frameTime + "ms");
                }

                System.out.println("OK");
                System.out.flush();
            }
        } catch (Exception e) {
            Logger.error("[ERR]", e.toString());
        }
    }

}
