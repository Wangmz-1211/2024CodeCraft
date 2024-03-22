package com.huawei.codecraft;


import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class Robot {

    static class Node<T> {
        T obj;
        int cost;

        public Node(T obj, int cost) {
            this.obj = obj;
            this.cost = cost;
        }
    }

    int xLimit = 0;
    int yLimit = 0;

    private Config config = null;

    public int id = -1;
    public int x = -1;
    public int y = -1;
    public int goods = 0;
    public int status = 0;
    public int targetDock = -1;
    public int value = 0;

    /**
     * The goods history of the robot, for debugging the goods choosing strategy.
     */
    public int history = 0;
    public Dock[] docks = null;
    public int[] dockPunish = null;
    public Path path = null;
    public Robot[] robots = null;
    public char[][] map = null;

    public Robot(int id, Config config) {
        this.id = id;
        this.config = config;
        xLimit = config.S_MAP;
        yLimit = config.S_MAP;
        dockPunish = new int[config.N_DOCK];

    }

    private int normOne(int x, int y) {
        return Math.abs(x - this.x) + Math.abs(y - this.y);
    }

    public void punishDock(Dock dock) {
        for (int dockId = 0; dockId < config.N_DOCK; dockId++) {
            if (docks[dockId] == dock) {
                dockPunish[dockId] += config.H_DOCK_PUNISH;
            }
        }
    }

    private int getGoodsCost(Goods goods, AStarAlgorithm algo) {
        Position startRegion = algo.getRegion(this.getPos());
        Position endRegion = algo.getRegion(goods.getPos());
        if (startRegion.equals(endRegion)) {
            return normOne(goods.x, goods.y) - goods.value / 2;
        }
        int d = algo.getRegionDistance(startRegion, endRegion);
        if (d == 100000) return normOne(goods.x, goods.y) - goods.value;
        return d - goods.value;
    }

    private int getGoodsCost(Goods goods) {
        return normOne(goods.x, goods.y);// - goods.value;
    }

    private int getDockCost(Dock dock, AStarAlgorithm algo) {
        Position startRegion = new Position(x, y);
        Position endRegion = new Position(dock.x, dock.y);
        int d = algo.getRegionDistance(startRegion, endRegion);
        if (d == 100000) return normOne(dock.x, dock.y) - dock.score / 3 - (dock.assigned ? 30 : 0);
        return d - dock.score / 5 - (dock.assigned ? 100 : 0);

    }

    private int getDockCost(Dock dock) {
        return normOne(dock.x, dock.y) - dock.score - (dock.assigned ? 20 : 0) + 50 * Math.abs(id - dock.id);
    }

    private int getDockCost(int dockId) {
        return getDockCost(docks[dockId]) + dockPunish[dockId];
    }

    public int getDockCost(int dockId, AStarAlgorithm algo) {
        return getDockCost(docks[dockId], algo) + dockPunish[dockId];
    }

    /**
     * This is a choose goods strategy for A* algorithm.
     *
     * @param goodsBucket
     * @param algo
     * @return
     */
    public Goods chooseGoods(GoodsBucket goodsBucket, AStarAlgorithm algo) {
        PriorityQueue<Node<Goods>> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (Goods goods : goodsBucket.goodsSet) {
            if (goods.assigned) continue;
            int cost = getGoodsCost(goods, algo);
            rank.offer(new Node<>(goods, cost));
        }
        return rank.isEmpty() ? null : rank.poll().obj;
    }

    /**
     * This is a generic goods choosing strategy.
     *
     * @param goodsBucket
     * @return
     */
    public Goods chooseGoods(GoodsBucket goodsBucket) {
        PriorityQueue<Node<Goods>> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (Goods goods : goodsBucket.goodsSet) {
            if (goods.assigned) continue;
            int cost = getGoodsCost(goods);
            rank.offer(new Node<>(goods, cost));
        }
        return rank.isEmpty() ? null : rank.poll().obj;
    }

    /**
     * This is a choose dock strategy for A* algorithm.
     *
     * @param algo
     * @return
     */
    public Dock chooseDock(AStarAlgorithm algo) {
        PriorityQueue<Node<Dock>> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (int dockId = 0; dockId < config.N_DOCK; dockId++) {
            int cost = getDockCost(dockId, algo);
            rank.offer(new Node<>(docks[dockId], cost));
        }
        return rank.isEmpty() ? null : rank.poll().obj;
    }

    /**
     * This is a choose dock strategy for bfs algorithm.
     *
     * @return
     */
    public Dock chooseDock() {
        PriorityQueue<Node<Dock>> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (int dockId = 0; dockId < config.N_DOCK; dockId++) {
            int cost = getDockCost(dockId);
            rank.offer(new Node<>(docks[dockId], cost));
        }
        return rank.isEmpty() ? null : rank.poll().obj;
    }

    public void update(int x, int y, int goods, int status) {
        if (this.x == x && this.y == y && this.goods == goods && this.status == status) return;
        else {
            this.x = x;
            this.y = y;
            this.goods = goods;
            this.status = status;
            this.targetDock = -1;
            this.path = null;
        }
    }

    private int distance(Robot robot) {
        return Math.abs(x - robot.x) + Math.abs(y - robot.y);
    }

    private boolean validPlace(int x, int y) {
        return x >= 0 && x < xLimit && y >= 0 && y < yLimit && map[x][y] != '#' && map[x][y] != '*';
    }

    public void movePath() {
        if (path == null) return;
        if (path.isEmpty()) {
            path = null;
            return;
        }
        Position next = path.path.peekFirst();

        // check if the next position is occupied by other robots
        for (Robot robot : robots) {
            if (robot != this && (robot.getPos().equals(next) || (robot.path != null && robot.path.path.peekFirst() != null && robot.path.path.peekFirst().equals(next)))) {
                this.path = null;
                this.targetDock = -1;
                return;
            }

        }
        next = path.pollFirst();
        int dx = next.x - x;
        int dy = next.y - y;
        if (dx == 1 && dy == 0) {
            move(3);
            this.x = next.x;
            this.y = next.y;
        } else if (dx == -1 && dy == 0) {
            move(2);
            this.x = next.x;
            this.y = next.y;
        } else if (dx == 0 && dy == 1) {
            move(0);
            this.x = next.x;
            this.y = next.y;
        } else if (dx == 0 && dy == -1) {
            move(1);
            this.x = next.x;
            this.y = next.y;
        } else {
            path = null;
            targetDock = -1;
        }
    }

    public void move(int direction) {
        switch (direction) {
            case 0:
                if (y < yLimit - 1) y++;
                break;
            case 1:
                if (y > 0) y--;
                break;
            case 2:
                if (x > 0) x--;
                break;
            case 3:
                if (x < xLimit - 1) x++;
                break;
        }
        System.out.println("move " + id + " " + direction);
    }

    public void get(Goods goods) {
        if (this.goods != 0) return;
        this.history++;
        this.goods = 1;
        this.value = goods.value;
        this.path = null;
        this.targetDock = -1;
        System.out.println("get " + id);
    }

    public Position getPos() {
        return new Position(x, y);
    }

    public void pull() {
        if (this.goods == 0) return;
        Position pos = getPos();
        this.goods = 0;
        this.value = 0;
        for (Dock dock : docks) {
            if (dock.inDock(pos)) {
                dock.goods++;
                break;
            }
        }
        System.out.println("pull " + id);
    }

}
