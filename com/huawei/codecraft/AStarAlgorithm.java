package com.huawei.codecraft;

import java.util.ArrayDeque;
import java.util.Comparator;
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

    public Path aStar(char[][] map, Pair start, Pair end) {
        long startMs = System.currentTimeMillis();
        Logger.debug("[AStar]", "Finding path from " + start + " to " + end);


        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        openSet.clear();
        closedSet.clear();
        openSet.offer(new Node(start, 0, null));
        while (!openSet.isEmpty()) {
            //TODO limit by time
            if (System.currentTimeMillis() - startMs > Config.H_ASTAR_MAX_TIME) {
                Logger.debug("[AStar]", "Time used: " + (System.currentTimeMillis() - startMs) + "ms");
                Logger.debug("[AStar]", "Exceed max depth, no path found.");
                return null;
            }
            Node current = openSet.poll();
            if (current.pair.equals(end)) {
                Path p = buildPath(current);
                Logger.debug("[AStar]", "Time used: " + (System.currentTimeMillis() - startMs) + "ms");
                Logger.debug("[AStar]", "Path found, length: " + p.path.size());
                p.poll();
                return p;
            }

            closedSet.offer(current);
            for (int i = 0; i < 4; i++) {
                Pair next = new Pair(current.pair.x + dx[i], current.pair.y + dy[i]);
                if (isValidPosition(map, next)) {
                    Node nextNode = new Node(next, totalCost(start, next, end), current);
                    if (inClosedSet(nextNode)) {
                        continue;
                    }
                    if (!inOpenSet(nextNode)) {
                        openSet.offer(nextNode);
                    }
                }
            }
        }
        Logger.debug("[AStar]", "Time used: " + (System.currentTimeMillis() - startMs) + "ms");
        Logger.debug("[AStar]", "No path found");
        return null;
    }

    /**
     * This cost function controls the priority of the goods for a robot.
     *
     * @param goods the goods to be evaluated
     * @param bot   the robot
     * @return cost value, smaller better
     */
    private int getGoodsCost(Goods goods, Robot bot) {
        int d = Math.abs(goods.x - bot.x) + Math.abs(goods.y - bot.y);
        return d - goods.value;
    }

    /**
     * This cost function controls the priority of the dock for a robot.
     *
     * @param dock the dock to be evaluated
     * @param bot  the robot
     * @return cost value, smaller better
     */
    private int getDockCost(Dock dock, Robot bot) {
        Pair p1 = dock.getPos();
        Pair p2 = bot.getPos();
        return normOne(p1.x, p1.y, p2.x, p2.y) - dock.score;
    }

    private int normOne(int startX, int startY, int endX, int endY) {
        return Math.abs(startX - endX) + Math.abs(startY - endY);
    }

    private int baseCost(Pair start, Pair curr) {
        return normOne(start.x, start.y, curr.x, curr.y);
    }

    private int heuristicCost(Pair curr, Pair end) {
        return normOne(curr.x, curr.y, end.x, end.y);
    }

    private int totalCost(Pair start, Pair curr, Pair end) {
        return Config.H_D_BASE_COST * baseCost(start, curr) + Config.H_D_HEURISTIC_COST * heuristicCost(curr, end);
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

    private boolean isObstacle(char[][] map, int x, int y) {
        return map[x][y] != '#' && map[x][y] != '*' && map[x][y] != 'W' && map[x][y] != '.';
    }

    private boolean inOpenSet(Node node) {
        if (openSet == null) {
            throw new IllegalStateException("openSet is null");
        }
        return openSet.contains(node);
    }

    private boolean inClosedSet(Node node) {
        if (closedSet == null) {
            throw new IllegalStateException("closedSet is null");
        }
        return closedSet.contains(node);
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
            if (goods.assigned) continue;
            int cost = getGoodsCost(goods, bot);
            rank.offer(new Node(goods, cost));
        }
        Goods target = rank.isEmpty() ? null : rank.poll().goods;
        if (target == null) return null;
        Path path = aStar(map, bot.getPos(), target.getPos());
        if (path == null) {
            Logger.debug("[AStar]", "Exceed max depth, no path found.");
            goodsBucket.remove(target);
            return null;
        }
        return path;
    }


    @Override
    public Path findDock(char[][] map, Dock[] docks, Robot bot) {
        class Node {
            final Dock dock;
            final int cost;

            public Node(Dock dock, int cost) {
                this.dock = dock;
                this.cost = cost;
            }
        }
        Logger.debug("[AStar]", "Finding dock for robot " + bot.id + " from " + bot.docks.size() + " docks");
        PriorityQueue<Node> rank = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
        for (Dock dock : bot.docks) {
            int cost = getDockCost(dock, bot);
            rank.offer(new Node(dock, cost));
        }
        Dock target = rank.isEmpty() ? null : rank.poll().dock;
        if (target == null) return null;
        Logger.debug("[FIND DOCK]", "Finding dock for robot at " + bot.getPos() + " to " + target.getPos() + ", " + " robot on " + map[bot.x][bot.y] + " dock on " + map[target.x][target.y]);
        Path path = aStar(map, bot.getPos(), target.getPos());
        if (path == null) {
            bot.docks.remove(target);
            return null;
        }
        return path;
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

}