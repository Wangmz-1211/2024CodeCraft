package com.huawei.codecraft;


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
    public Path path = null;

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
        boolean inDock = false;
        for (Dock dock : docks) {
            if (dock.inDock(pos)) {
                inDock = true;
                this.goods = 0;
                this.value = 0;
                dock.goods++;
                System.out.println("pull " + id);
                return;
            }
        }
    }

}