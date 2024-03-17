package com.huawei.codecraft;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Ship {

    public int id = 0;
    /**
     * The number of the goods ship loaded.
     */
    public int num = 0;

    /**
     * The position of the ship, refresh every frame from input.
     */
    public int pos = 0;

    /**
     * The status of the ship, refresh every frame from input.
     */
    public int status = 0;

    /**
     * The value of the ship
     */
    public int value = 0;
    public int load_time = 0;

    public Dock[] docks = null;

    public Ship(int id) {
        this.id = id;
    }

    private int getDockCost(Dock dock) {
        return -Math.abs(dock.goods) * dock.score;
    }

    public Dock chooseDock() {
        class Node {
            final Dock dock;
            final int cost;

            public Node(Dock dock, int cost) {
                this.dock = dock;
                this.cost = cost;
            }
        }

        PriorityQueue<Node> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (Dock dock : docks) {
            if (dock.assigned) continue;
            int cost = getDockCost(dock);
            rank.offer(new Node(dock, cost));
        }
        return rank.poll().dock;
    }

    public void ship(int dockId) {
        System.out.println("ship " + this.id + " " + dockId);
    }

    public void go() {
        this.load_time = 0;
        System.out.println("go " + this.id);
    }
}
