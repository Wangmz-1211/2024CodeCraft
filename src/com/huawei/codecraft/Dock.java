package com.huawei.codecraft;

import com.huawei.codecraft.Pair;

public class Dock {

    public int id;
    public int x;
    public int y;
    public int transport_time;
    public int loading_speed;
    public int score;
    public int goods = 0;

    public boolean assigned = false;

    public Dock() {
    }

    public Dock(int id, int x, int y, int transport_time, int loading_speed) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.transport_time = transport_time;
        this.loading_speed = loading_speed;
        this.score = loading_speed * 100000 / transport_time;
    }

    public void updateScore() {
        this.score = loading_speed * 100000 / transport_time;
    }

    public Pair getPos() {
        return new Pair(x, y);
    }

    /**
     * Simulate the load action between a dock and a ship.
     * So we can track the goods number of the dock and the ship.
     * This could be used to ranking the dock by bot and ship.
     *
     * @param ship the ship in this dock.
     */
    public void load(Ship ship) {
        ship.num += loading_speed;
        goods -= loading_speed;
        ship.load_time += 1;
        Logger.debug("[DOCK]", "Left " + goods + " goods in dock " + id + ", loaded " + loading_speed * ship.load_time + " goods to ship.");
        Logger.debug("[DOCK]", "Load " + loading_speed + " goods from dock " + id + " to ship " + ship.id + ", already " + ship.load_time + " times");
    }

    public boolean inDock(Pair p) {
        return x <= p.x && p.x <= x + 3 && y <= p.y && p.y <= y + 3;
    }
}
