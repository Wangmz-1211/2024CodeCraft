/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

import sun.rmi.runtime.Log;

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
            dock.updateScore();
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
            ships[i].docks = docks;
        }
        Logger.info("[INIT]", "Ships initialized.");

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

        try {
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
                        if (bot.goods == 1) { // bot has goods
                            if (mainInstance.map[bot.x][bot.y] == 'B') {
                                bot.pull();
                                bot.path = null;
                                bot.targetDock = -1;
                                continue;
                            }
                            if (frameId % 10 == bot.id && bot.targetDock == -1 || bot.path == null) {
                                // find a dock
                                // label the position of the robots as obstacles, so the path
                                // won't go through the position of the robots.
                                for (int botId = 0; botId < Config.N_ROBOT; botId++) {
                                    Pair pos = mainInstance.robots[botId].getPos();
                                    mainInstance.map[pos.x][pos.y] = '#';
                                }
                                Path path = algo.findDock(mainInstance.map, mainInstance.docks, bot);
                                for (int botId = 0; botId < Config.N_ROBOT; botId++) {
                                    Pair pos = mainInstance.robots[botId].getPos();
                                    char c = 'A';
                                    for (Dock dock : mainInstance.docks) {
                                        if (dock.inDock(pos)) {
                                            c = 'B';
                                            break;
                                        }
                                    }
                                    mainInstance.map[pos.x][pos.y] = c;
                                }
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
                                for (int botId = 0; botId < Config.N_ROBOT; botId++) {
                                    Pair pos = mainInstance.robots[botId].getPos();
                                    mainInstance.map[pos.x][pos.y] = '#';
                                }

                                // Try to assign a goods to this robot
                                Path path = algo.findGoods(mainInstance.map, bot, mainInstance.goodsBucket);
                                for (int botId = 0; botId < Config.N_ROBOT; botId++) {
                                    Pair pos = mainInstance.robots[botId].getPos();
                                    char c = 'A';
                                    for (Dock dock : mainInstance.docks) {
                                        if (dock.inDock(pos)) {
                                            c = 'B';
                                            break;
                                        }
                                    }
                                    mainInstance.map[pos.x][pos.y] = c;
                                }
                                if (path == null) {
                                    Logger.debug("[ROBOT]", "Robot " + bot.id + " found no path.");
                                    continue;
                                }
                                Pair targetPos = path.peekTargetPos();
                                // Once assigned to a specific bot, the goods will be removed from the goodsBucket,
                                // so other bots will not be assigned to the same goods.
                                mainInstance.goodsBucket.assignAt(targetPos);
                                bot.path = path;
                            }
                            if (bot.path != null) {
                                bot.movePath();
                                Logger.debug("[ROBOT]", "Robot " + bot.id + " moved.");
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

                            Dock bestDock = ship.chooseDock();
                            bestDock.assigned = true;
                            ship.ship(bestDock.id);
                            Logger.info("[SHIP]", "Ship " + ship.id + " is going to dock " + bestDock.id);
                            continue;
                        }
                        // Ship is in a dock[ship.pos], loading goods
                        Dock dock = mainInstance.docks[ship.pos];
                        dock.load(ship);
                        if (
//                                ship.load_time >= Config.H_MIN_SHIP_LOAD_TIME
                                ship.load_time >= mainInstance.boat_capacity
                                        &&
                                        ship.num >= mainInstance.boat_capacity
                                        ||
                                        dock.goods <= dock.loading_speed
                                        ||
//                                        ship.load_time >= Config.H_MAX_SHIP_LOAD_TIME
                                        ship.load_time >= mainInstance.boat_capacity * 2
                        ) {
                            dock.assigned = false;
                            ship.go();
                            Logger.info("[SHIP]", "Ship " + ship.id + " is going to sell goods.");
                        }
                    } else { // Ship is waiting outside a dock
                        Logger.error("[SHIP]", "Ship " + ship.id + " is waiting outside dock " + ship.pos);
                        Dock bestDock = ship.chooseDock();
                        bestDock.assigned = true;
                        ship.ship(bestDock.id);
                        Logger.error("[SHIP]", "Ship " + ship.id + " is going to dock " + bestDock.id);

                    }
                }

                // Finish
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
