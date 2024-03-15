package com.huawei.codecraft;


import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Robot {

    static class Node<T> {
        T obj;
        int cost;

        public Node(T obj, int cost) {
            this.obj = obj;
            this.cost = cost;
        }
    }

    int xLimit = Config.S_MAP;
    int yLimit = Config.S_MAP;

    public int id = -1;
    public int x = -1;
    public int y = -1;
    public int goods = 0;
    public int status = 0;
    public int targetDock = -1;
    public int value = 0;

    public Dock[] docks = null;
    public int[] dockPunish = new int[Config.N_DOCK];
    public Path path = null;

    public Robot(int id) {
        this.id = id;
    }

    private int normOne(int x, int y) {
        return Math.abs(x - this.x) + Math.abs(y - this.y);
    }

    public void punishDock(Dock dock) {
        for( int dockId = 0; dockId < Config.N_DOCK; dockId++) {
            if (docks[dockId] == dock) {
                dockPunish[dockId] += Config.H_DOCK_PUNISH;
            }
        }
    }
    private int getGoodsCost(Goods goods) {
        return normOne(goods.x, goods.y) - goods.value;
    }

    private int getDockCost(Dock dock) {
        return normOne(dock.x, dock.y) - dock.score;
    }

    public int getDockCost(int dockId) {
        return getDockCost(docks[dockId]) + dockPunish[dockId];
    }

    public Goods chooseGoods(GoodsBucket goodsBucket) {
        PriorityQueue<Node<Goods>> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (Goods goods : goodsBucket.goodsSet) {
            if (goods.assigned) continue;
            int cost = getGoodsCost(goods);
            rank.offer(new Node<>(goods, cost));
        }
        return rank.isEmpty() ? null : rank.poll().obj;
    }

    public Dock chooseDock() {
        PriorityQueue<Node<Dock>> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (int dockId = 0; dockId < Config.N_DOCK; dockId++) {
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

    public void movePath() {
        if (path == null) return;
        if (path.isEmpty()) {
            path = null;
            return;
        }
        Pair next = path.pollFirst();
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
        this.goods = 1;
        this.value = goods.value;
        this.path = null;
        this.targetDock = -1;
        System.out.println("get " + id);
    }

    public Pair getPos() {
        return new Pair(x, y);
    }

    public void pull() {
        if (this.goods == 0) return;
        Pair pos = getPos();
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
