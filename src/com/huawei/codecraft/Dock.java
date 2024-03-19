package com.huawei.codecraft;

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
        updateScore();
    }

    public void updateScore() {
        this.score = loading_speed * 100000 / transport_time;
    }

    public Position getPos() {
        return new Position(x, y);
    }

    /**
     * Simulate the load action between a dock and a ship.
     * So we can track the goods number of the dock and the ship.
     * This could be used to ranking the dock by bot and ship.
     *
     * @param ship the ship in this dock.
     */
    public void load(Ship ship) {
        int loadNum = Math.min(loading_speed, goods);
        loadNum = Math.min(loadNum, ship.capacity - ship.num);
        ship.num += loadNum;
        goods -= loadNum;
        ship.load_time += 1;
        Logger.debug("[DOCK" + id + "]", "Loaded " + loadNum + " goods from dock " + id + " to ship " + ship.id);
        Logger.debug("[DOCK" + id + "]", "Left " + goods + " goods in dock " + id);
    }

    public boolean inDock(Position p) {
        return x <= p.x && p.x <= x + 3 && y <= p.y && p.y <= y + 3;
    }
}
