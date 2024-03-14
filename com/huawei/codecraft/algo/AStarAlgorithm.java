package com.huawei.codecraft.algo;

import com.huawei.codecraft.Config;
import com.huawei.codecraft.entities.Dock;
import com.huawei.codecraft.entities.Goods;
import com.huawei.codecraft.entities.GoodsBucket;
import com.huawei.codecraft.entities.Robot;
import com.huawei.codecraft.utils.Logger;
import com.huawei.codecraft.utils.Pair;
import sun.rmi.runtime.Log;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;

public class AStarAlgorithm implements Algorithm {

    static class Node {
        Pair pair;
        int cost;

        Node parent;

        public Node(Pair pair, int cost, Node parent) {
            this.pair = pair;
            this.cost = cost;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            Node node = (Node) obj;
            return pair.equals(node.pair);
        }

        @Override
        public int hashCode() {
            return pair.hashCode();
        }

    }

    private PriorityQueue<Node> openSet = null;
    private PriorityQueue<Node> closedSet = null;

    public AStarAlgorithm() {
        openSet = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        closedSet = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
    }

    private double baseCost(int startX, int startY, int currX, int currY) {
        return Config.H_D_BASE_COST * (Math.abs(startX - currX) + Math.abs(startY - currY));
    }

    private double baseCost(Pair start, Pair curr) {
        return baseCost(start.x, start.y, curr.x, curr.y);
    }

    private double heuristicCost(int currX, int currY, int endX, int endY) {
        return Config.H_D_HEURISTIC_COST * (Math.abs(currX - endX) + Math.abs(currY - endY));
    }

    private double heuristicCost(Pair curr, Pair end) {
        return heuristicCost(curr.x, curr.y, end.x, end.y);
    }

    private double totalCost(Pair start, Pair curr, Pair end) {
        return baseCost(start, curr) + heuristicCost(curr, end);
    }

    private boolean isValidPosition(char[][] map, int x, int y) {
        if (x < 0 || y < 0 || x >= map.length || y >= map[0].length) {
            return false;
        }
        return isObstacle(map, x, y);
    }

    private boolean isValidPosition(char[][] map, Pair pair) {
        return isValidPosition(map, pair.x, pair.y);
    }

    private boolean isValidPosition(char[][] map, Node node) {
        return isValidPosition(map, node.pair);
    }

    private boolean isObstacle(char[][] map, int x, int y) {
        return map[x][y] != '#' && map[x][y] != '*' && map[x][y] != 'W' && map[x][y] != '.';
    }

    private boolean isObstacle(char[][] map, Pair pair) {
        return isObstacle(map, pair.x, pair.y);
    }

    private boolean isObstacle(char[][] map, Node node) {
        return isObstacle(map, node.pair);
    }

    private boolean inOpenSet(Node node) {
        if (openSet == null) {
            throw new IllegalStateException("openSet is null");
        }
        return openSet.contains(node);
    }

    private boolean inOpenSet(Pair pair) {
        if (openSet == null) {
            throw new IllegalStateException("openSet is null");
        }
        return openSet.contains(new Node(pair, 0, null));
    }

    private boolean inClosedSet(Node node) {
        if (closedSet == null) {
            throw new IllegalStateException("closedSet is null");
        }
        return closedSet.contains(node);
    }

    private boolean inClosedSet(Pair pair) {
        if (closedSet == null) {
            throw new IllegalStateException("closedSet is null");
        }
        return closedSet.contains(new Node(pair, 0, null));
    }

    private int getGoodsCost(Goods goods, Robot bot) {
        int d = Math.abs(goods.x - bot.x) + Math.abs(goods.y - bot.y);
        return 1000 * d / goods.value;
    }


    @Override
    public Path findGoods(char[][] map, Robot bot, GoodsBucket goodsBucket) {
        class Node {
            final Goods goods;
            final int cost;

            public Node(Goods goods, int cost) {
                this.goods = goods;
                this.cost = cost;
            }
        }
        PriorityQueue<Node> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (Goods goods : goodsBucket.goodsSet) {
            int cost = getGoodsCost(goods, bot);
            rank.offer(new Node(goods, cost));
        }
        Goods target = rank.isEmpty() ? null : rank.poll().goods;
        if (target == null) return null;
        return aStar(map, bot.getPos(), target.getPos());
    }

    @Override
    public Path findDock(char[][] map, Dock[] docks, Robot bot) {
        return findDockById(map, docks, bot);
    }

    private Path findDockById(char[][] map, Dock[] docks, Robot bot) {
        return aStar(map, new Pair(bot.x, bot.y), new Pair(docks[bot.id].x, docks[bot.id].y));
    }

    private Path buildPath(Node end) {
        Path path = new Path(new ArrayDeque<>());
        Node current = end;
        while (current != null) {
            path.offerFirst(current.pair);
            current = current.parent;
        }
        return path;
    }


    private Path aStar(char[][] map, Pair start, Pair end) {
        Logger.debug("[AStar]", "Finding path from " + start + " to " + end);

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        openSet.clear();
        closedSet.clear();
        openSet.offer(new Node(start, 0, null));
        while (!openSet.isEmpty()) {

            Node current = openSet.poll();
            if (current.pair.equals(end)) {
                Path p =  buildPath(current);
                Logger.debug("[AStar]", "Path found, length: " + p.path.size());
                p.poll();
                return p;
            }

            closedSet.offer(current);
            for (int i = 0; i < 4; i++) {
                Pair next = new Pair(current.pair.x + dx[i], current.pair.y + dy[i]);
                if (isValidPosition(map, next)) {
                    Node nextNode = new Node(next, (int) totalCost(start, next, end), current);
                    if (inClosedSet(nextNode)) {
                        continue;
                    }
                    if (!inOpenSet(nextNode)) {
                        openSet.offer(nextNode);
                    }
                }
            }
        }
        Logger.debug("[AStar]", "No path found");
        return null;
    }


    public void test() {

        System.out.println("AStarAlgorithm test");
        char[][] map = new char[200][200];
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                map[i][j] = 'A';
            }
        }
        System.out.println("Map initialized");
        Pair start = new Pair(0, 0);
        Pair end = new Pair(5, 5);
        System.out.println("Start and end initialized");
        long startTime = System.currentTimeMillis();
        Path path = aStar(map, start, end);
        long endTime = System.currentTimeMillis();
        if (path == null) {
            System.out.println("No path found");
        } else {
            System.out.println("Path found");
            for (Pair p : path.path) {
                System.out.println(p.x + " " + p.y);
            }
        }
        System.out.println("Time: " + (endTime - startTime));


    }
}
