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

    private final PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
    private final PriorityQueue<Node> closedSet = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));

    private Config config = null;

    public AStarAlgorithm(Config config) {
        this.config = config;
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
            if (System.currentTimeMillis() - startMs > config.H_ASTAR_MAX_TIME) {
                Logger.debug("[AStar]", "Time used: " + (System.currentTimeMillis() - startMs) + "ms");
                Logger.debug("[AStar]", "Exceed max depth, no path found.");
                return null;
            }
            Node current = openSet.poll();
            assert current != null;
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

    private int normOne(int startX, int startY, int endX, int endY) {
        return Math.abs(startX - endX) + Math.abs(startY - endY);
    }

    private int normTwo(int startX, int startY, int endX, int endY) {
        return (int) Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
    }

    private int baseCost(Pair start, Pair curr) {
        return normOne(start.x, start.y, curr.x, curr.y);
    }

    private int heuristicCost(Pair curr, Pair end) {
        return normOne(curr.x, curr.y, end.x, end.y);
    }

    private int totalCost(Pair start, Pair curr, Pair end) {
        return config.H_D_BASE_COST * baseCost(start, curr) + config.H_D_HEURISTIC_COST * heuristicCost(curr, end);
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
        return openSet.contains(node);
    }

    private boolean inClosedSet(Node node) {
        return closedSet.contains(node);
    }

    @Override
    public Path findGoods(char[][] map, Robot bot, GoodsBucket goodsBucket) {
        Goods target = bot.chooseGoods(goodsBucket);
        if (target == null) return null;
        Path path = aStar(map, bot.getPos(), target.getPos());
        if (path == null) {
            Logger.debug("[AStar]", "Exceed max depth, no path found. Remove goods info.");
            goodsBucket.remove(target);
            return null;
        }
        return path;
    }

    /**
     * Find the best dock by several factors
     *
     * @param map   the map
     * @param docks not used, for interface compatibility
     * @param bot   the robot
     * @return the path to the best dock, not including the current position.
     */

    @Override
    public Path findDock(char[][] map, Dock[] docks, Robot bot) {
        Dock target = bot.chooseDock();
        if (target == null) return null;
        Logger.debug("[FIND DOCK]", "Finding dock for robot at " + bot.getPos() + " to " + target.getPos() + ", " + " robot on " + map[bot.x][bot.y] + " dock on " + map[target.x][target.y]);
        Path path = aStar(map, bot.getPos(), target.getPos());
        if (path == null)
            bot.punishDock(target);
        return path;
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