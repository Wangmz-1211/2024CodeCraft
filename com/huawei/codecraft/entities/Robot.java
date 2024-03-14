package com.huawei.codecraft.entities;

import com.huawei.codecraft.Config;
import com.huawei.codecraft.utils.Pair;

import java.util.Deque;
import java.util.List;

public class Robot {
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
    public Deque<Pair> path = null;

    public Robot(int id) {
        this.id = id;
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
        }
        else if (dx == -1 && dy == 0) {
            move(2);
            this.x = next.x;
            this.y = next.y;
        }
        else if (dx == 0 && dy == 1) {
            move(0);
            this.x = next.x;
            this.y = next.y;
        }
        else if (dx == 0 && dy == -1) {
            move(1);
            this.x = next.x;
            this.y = next.y;
        }
        else {
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

    public void pull() {
        if (this.goods == 0) return;
        boolean inDock = false;
        for (Dock dock : docks) {
            if (
                    dock.x <= this.x && dock.x + 3 >= this.x &&
                            dock.y <= this.y && dock.y + 3 >= this.y) {
                inDock = true;
                break;
            }
        }
        if (inDock) {
            this.goods = 0;
            this.value = 0;
            System.out.println("put " + id);
        }
    }

}
