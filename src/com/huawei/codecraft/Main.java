/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.huawei.codecraft;

import java.util.*;

/**
 * Main
 *
 * @since 2024-02-05
 */
public class Main {

    public Config config = new Config();
    public Utils utils = new Utils(config);
    private int money, boat_capacity, id;
    private final String[] ch = new String[config.S_MAP];
    private final char[][] map = new char[config.S_MAP][config.S_MAP];

    private final Robot[] robots = new Robot[config.N_ROBOT];
    private final Dock[] docks = new Dock[config.N_DOCK];
    private final Ship[] ships = new Ship[config.N_SHIP];
    private final GoodsBucket goodsBucket = new GoodsBucket();


    private void init(AStarAlgorithm algo) {
        long startTime = System.currentTimeMillis();
        Scanner scanf = new Scanner(System.in);
        for (int i = 0; i < config.S_MAP; i++) {
            ch[i] = scanf.nextLine();
        }
        Logger.info("[INIT]", "Map loaded");
        for (int i = 0; i < config.N_DOCK; i++) {
            Dock dock = new Dock();
            id = scanf.nextInt();
            docks[id] = dock;
            dock.id = id;
            dock.x = scanf.nextInt();
            dock.y = scanf.nextInt();
            dock.transport_time = scanf.nextInt();
            dock.loading_speed = scanf.nextInt();
            dock.updateScore();
            Logger.debug("[DOCK" + id + "]", "Transport time: " + dock.transport_time + " Loading speed: " + dock.loading_speed + " Score: " + dock.score);
        }
        Logger.info("[INIT]", "Docks loaded");
        this.boat_capacity = scanf.nextInt();
        Logger.info("[INIT]", "Boat capacity " + boat_capacity);

        /*Other Logics*/
        // Initialize map information
        List<Position> botPos = new ArrayList<>();
        for (int i = 0; i < config.S_MAP; i++) {
            for (int j = 0; j < config.S_MAP; j++) {
                char c = ch[i].charAt(j);
                if (c == 'A') {
                    botPos.add(new Position(i, j));
                }
                map[i][j] = c;
            }
        }
        for (Position p : botPos) {
            utils.mapHandler(map, p);
        }
        algo.preprocess(map);
        // Initialize Robots
        for (int i = 0; i < config.N_ROBOT; i++) {
            Robot robot = new Robot(i, config);
            robot.docks = docks;
            robots[i] = robot;
        }
        Logger.info("[INIT]", "Robots initialized.");


        // Initialize Ships
        for (int i = 0; i < config.N_SHIP; i++) {
            ships[i] = new Ship(i);
            ships[i].docks = docks;
            ships[i].capacity = this.boat_capacity;
        }
        Logger.info("[INIT]", "Ships initialized.");
        Logger.info("[INIT]", "Initialization finished in " + (System.currentTimeMillis() - startTime) + "ms");
        // Finish
        String okk = scanf.nextLine();
        System.out.println("OK");
        System.out.flush();
    }

    private int input() {
        Scanner scanf = new Scanner(System.in);
        //  frameId
        this.id = scanf.nextInt();
        Logger.info("[INPUT]", "Frame " + id);
        this.money = scanf.nextInt();
        Logger.info("[INPUT]", "Money " + money);
        // Update goods
        int num = scanf.nextInt();
        Logger.info("[INPUT]", "Goods " + num);
        for (int i = 1; i <= num; i++) {
            int x = scanf.nextInt();
            int y = scanf.nextInt();
            int val = scanf.nextInt();
            if (map[x][y] != 'A') continue;
            goodsBucket.add(new Goods(x, y, val, id));
        }
        Logger.debug("[INPUT]", "Goods updated");
        // Update robots
        for (int i = 0; i < config.N_ROBOT; i++) {
            int goods = scanf.nextInt();
            int x = scanf.nextInt();
            int y = scanf.nextInt();
            int status = scanf.nextInt();
            robots[i].update(x, y, goods, status);
        }
        Logger.debug("[INPUT]", "Robots updated");
        for (int i = 0; i < 5; i++) {
            Ship ship = ships[i];
            ship.status = scanf.nextInt();
            ship.pos = scanf.nextInt();
        }
        Logger.debug("[INPUT]", "Ships updated");
        String okk = scanf.nextLine();
        return id;
    }

    private void labelMap() {
        for (int botId = 0; botId < config.N_ROBOT; botId++) {
            Position pos = robots[botId].getPos();
            map[pos.x][pos.y] = '#';
        }
    }

    private void restoreMap() {
        for (int botId = 0; botId < config.N_ROBOT; botId++) {
            Position pos = robots[botId].getPos();
            char c = 'A';
            for (Dock dock : docks) {
                if (dock.inDock(pos)) {
                    c = 'B';
                    break;
                }
            }
            map[pos.x][pos.y] = c;
        }
    }

    public static void main(String[] args) {

        try {
            Main mainInstance = new Main();

            Config config = mainInstance.config;

            // Configuration by args
            if (args.length > 0) {
                // Ship logic
                config.H_SHIP_CAPACITY_THRESHOLD = Integer.parseInt(args[0]);
                config.H_MAX_SHIP_LOAD_TIME = Double.parseDouble(args[1]);
                config.H_SHIP_REDIRECT_THRESHOLD = Integer.parseInt(args[2]);
                // A* algorithm
                config.H_ASTAR_MAX_TIME_GOODS = Integer.parseInt(args[3]);
                config.H_ASTAR_MAX_TIME_DOCK = Integer.parseInt(args[4]);
                // Robot logic
                config.H_DOCK_PUNISH = Integer.parseInt(args[5]);
                config.H_BOT_FIND_GOOD_ITERATOR = Boolean.parseBoolean(args[6]);
                config.H_BOT_FIND_DOCK_ITERATOR = Boolean.parseBoolean(args[7]);
            }

            AStarAlgorithm algo = new AStarAlgorithm(config);
            mainInstance.init(algo);
            for (int frame = 1; frame <= 15000; frame++) {
                Logger.debug("[FRAME]", "==============================IN==============================");
                long startTime = System.currentTimeMillis();
                int frameId = mainInstance.input();
                Logger.debug("[FRAME]", "==============================OP==============================");
                mainInstance.goodsBucket.clean(frameId);

                // Robot Logic
                for (int i = 0; i < config.N_ROBOT; i++) {
                    Robot bot = mainInstance.robots[i];
                    if (bot.status == 0) { // bot is stuck
                        bot.path = null;
                        bot.targetDock = -1;
                    } else { // bot is running normally
                        if (bot.goods == 1) { // bot has goods
                            // Pull the goods if bot can.
                            if (mainInstance.map[bot.x][bot.y] == 'B') {
                                bot.pull();
                                bot.path = null;
                                bot.targetDock = -1;
                                continue;
                            }
                            if (bot.targetDock == -1 || bot.path == null) {
                                if (!config.H_BOT_FIND_DOCK_ITERATOR || frameId % 10 == bot.id) {
                                    // find a dock
                                    // label the position of the robots as obstacles, so the path
                                    // won't go through the position of the robots.
                                    mainInstance.labelMap();
                                    Path path = algo.findDock(mainInstance.map, mainInstance.docks, bot);
                                    mainInstance.restoreMap();
                                    if (path == null) {
                                        Logger.debug("[ROBOT]", "Robot " + bot.id + " found no dock.");
                                        continue;
                                    }
                                    bot.targetDock = 1;
                                    bot.path = path;
                                }
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
                            if (bot.path == null) { // find path
                                if (!config.H_BOT_FIND_GOOD_ITERATOR || frameId % 10 == bot.id) {
                                    mainInstance.labelMap();
                                    // Try to assign a goods to this robot
                                    Path path = algo.findGoods(mainInstance.map, bot, mainInstance.goodsBucket);
                                    mainInstance.restoreMap();
                                    // Can't find goods
                                    if (path == null) continue;
                                    // Once assigned to a specific bot, the goods will be removed from the goodsBucket,
                                    // so other bots will not be assigned to the same goods.
                                    Position targetPos = path.peekTargetPos();
                                    mainInstance.goodsBucket.assignAt(targetPos);
                                    bot.path = path;

                                }
                            }
                            if (bot.path != null) {
                                bot.movePath();
                                Logger.debug("[ROBOT]", "Robot " + bot.id + " moved.");
                            }
                        }
                    }
                }

                // Ship Logic
                for (int i = 0; i < config.N_SHIP; i++) {
                    Ship ship = mainInstance.ships[i];
                    if (ship.status == 1) {
                        if (ship.pos == -1) { // Ship has sold goods, go dock
                            Dock bestDock = ship.chooseDock();
                            bestDock.assigned = true;
                            ship.ship(bestDock.id);
                            Logger.info("[SHIP" + ship.id + "]", "Ship " + ship.id + " is going to dock " + bestDock.id);
                            continue;
                        }
                        // Ship is in a dock[ship.pos], loading goods
                        Dock dock = mainInstance.docks[ship.pos];
                        dock.load(ship);
                        Logger.debug("[SHIP" + ship.id + "]", "Loading goods at " + ship.pos + " dock, Goods on ship: " + ship.num);
                        Logger.debug("[SHIP" + ship.id + "]", "Dock left goods: " + dock.goods + "  Ship load time: " + ship.load_time);
                        if (ship.load_time >= mainInstance.boat_capacity * config.H_MAX_SHIP_LOAD_TIME) {
                            // efficiency is too low, go to sell goods.
                            ship.go();
                            dock.assigned = false;
                            Logger.debug("[SHIP" + ship.id + "]", "Going to sell goods.");
                        } else if (
                                dock.goods == 0 // dock is out of goods
                                        ||
                                        ship.num >= mainInstance.boat_capacity // Ship is full
                        ) {
                            if (mainInstance.boat_capacity - ship.num > config.H_SHIP_CAPACITY_THRESHOLD
                                    && ship.redirect <= config.H_SHIP_REDIRECT_THRESHOLD) {
                                // Ship is far from full, redirect to another dock
                                Dock bestDock = ship.chooseDock();
                                bestDock.assigned = true;
                                ship.ship(bestDock.id);
                                ship.redirect += 1;
                                Logger.debug("[SHIP" + ship.id + "]", "Ship " + ship.id + " is going to dock " + bestDock.id + ". Redirected " + ship.redirect + " times.");
                            } else {
                                // When ship is almost full, go to sell goods.
                                ship.go();
                                Logger.debug("[SHIP" + ship.id + "]", "Going to sell goods.");
                            }
                            dock.assigned = false;
                        }
                    } else if (ship.status == 2) { // Ship is waiting outside a dock
                        Dock bestDock = ship.chooseDock();
                        bestDock.assigned = true;
                        ship.ship(bestDock.id);
                    }
                }

                // Finish
                long frameTime = System.currentTimeMillis() - startTime;
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
