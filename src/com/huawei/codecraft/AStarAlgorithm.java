package com.huawei.codecraft;

import sun.rmi.runtime.Log;

import java.util.*;

public class AStarAlgorithm implements Algorithm {

    static class Node {
        Position position;
        int cost;

        Node parent;

        public Node(Position position, int cost, Node parent) {
            this.position = position;
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
            return position.equals(node.position);
        }

        @Override
        public int hashCode() {
            return position.hashCode();
        }

    }

    private final PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(o -> o.cost));
    private final Set<Node> closedSet = new HashSet<>();

    private Config config = null;

    public AStarAlgorithm(Config config) {
        this.config = config;
    }

    public Path aStar(char[][] map, Position start, Position end, int timeLimit) {
        long startMs = System.currentTimeMillis();
        Logger.debug("[AStar]", "Finding path from " + start + " to " + end);

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        openSet.clear();
        closedSet.clear();
        openSet.offer(new Node(start, 100000, null));

        while (!openSet.isEmpty()) {

            if (System.currentTimeMillis() - startMs > timeLimit) {
                Logger.debug("[AStar]", "Time used: " + (System.currentTimeMillis() - startMs) + "ms");
                Logger.debug("[AStar]", "Exceed max time, no path found.");
                return null;
            }
            Node current = openSet.poll();
            assert current != null;
            if (current.position.equals(end)) {
                Path p = buildPath(current);
                Logger.debug("[AStar]", "Time used: " + (System.currentTimeMillis() - startMs) + "ms");
                Logger.debug("[AStar]", "Path found, length: " + p.path.size());
                p.poll();
                return p;
            }

            closedSet.add(current);

            for (int i = 0; i < 4; i++) {
                Position next = new Position(current.position.x + dx[i], current.position.y + dy[i]);
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


    private int baseCost(Position start, Position curr) {
        return normOne(start.x, start.y, curr.x, curr.y);
    }

    private int heuristicCost(Position curr, Position end) {
        int dx = curr.x - end.x;
        int dy = curr.y - end.y;
        return dx * dx + 2 * dy * dy;
    }

    private int totalCost(Position start, Position curr, Position end) {
        return config.H_D_BASE_COST * baseCost(start, curr) + config.H_D_HEURISTIC_COST * heuristicCost(curr, end);
    }

    private boolean isValidPosition(char[][] map, int x, int y) {
        if (x < 0 || y < 0 || x >= map.length || y >= map[0].length) {
            return false;
        }
        return isObstacle(map, x, y);
    }

    private boolean isValidPosition(char[][] map, Position position) {
        return isValidPosition(map, position.x, position.y);
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
//        Path path = aStar(map, bot.getPos(), target.getPos(), config.H_ASTAR_MAX_TIME_GOODS);
        Path path = aStarWithPreprocessing(map, bot.getPos(), target.getPos());
        if (path == null) {
            Logger.debug("[FIND GOOD]", "Exceed max depth, no path found. Remove goods info.");
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
        Logger.debug("[FIND DOCK]", "Finding dock for robot " + bot.id + " to dock " + target.id);

//        Path path = aStar(map, bot.getPos(), target.getPos(), config.H_ASTAR_MAX_TIME_DOCK);
        Path path = aStarWithPreprocessing(map, bot.getPos(), target.getPos());

        if (path == null)
            bot.punishDock(target);
        return path;
    }

    private Path buildPath(Node end) {
        Path path = new Path(new ArrayDeque<>());
        Node current = end;
        while (current != null) {
            path.offerFirst(current.position);
            current = current.parent;
        }
        return path;
    }

    class Pair {
        public Position first;
        public Position second;

        public Pair(Position first, Position second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            Pair pair = (Pair) obj;
            return first.equals(pair.first) && second.equals(pair.second);
        }

        @Override
        public int hashCode() {
            return first.hashCode() * 31 + second.hashCode();
        }
    }

    private final Map<Pair, Path> regionPaths = new HashMap<>();
    private final int regionSize = 10;

    private Path bfs(char[][] map, Position start, Position end) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Queue<Position> queue = new LinkedList<>();
        queue.offer(start);
        Map<Position, Position> parent = new HashMap<>();
        parent.put(start, null);
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(end) || map[current.x][current.y] == 'B') {
                Path path = new Path(new ArrayDeque<>());
                while (current != null) {
                    path.offerFirst(current);
                    current = parent.get(current);
                }
                path.poll();
                return path;
            }
            for (int i = 0; i < 4; i++) {
                Position next = new Position(current.x + dx[i], current.y + dy[i]);
                if (isValidPosition(map, next) && !parent.containsKey(next)) {
                    parent.put(next, current);
                    queue.offer(next);
                }
            }
        }
        return null;
    }

    /**
     * First, generate a map of all the regions, then use bfs to try to from a region to another region.
     * Try as many as possible to generate the map of region to region path.
     *
     * @param map
     */

    public void preprocess(char[][] map) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        int width = map[0].length;
        int height = map.length;
        HashSet<Position> regions = new HashSet<>();
        for (int i = 0; i < width; i += regionSize) {
            for (int j = 0; j < height; j += regionSize) {
                regions.add(new Position(i, j));
            }
        }
        for (Position region : regions) {
            if (map[region.x][region.y] != 'A') continue;
            Logger.debug("[Preprocess]", "Region: " + region);
            Deque<Position> queue = new ArrayDeque<>();
            Map<Position, Position> parents = new HashMap<>();
            queue.offer(region);
            parents.put(region, null);
            while (!queue.isEmpty()) {
                Position current = queue.poll();
                if (regions.contains(current) && !current.equals(region)) {
                    // build path
                    Path path = new Path(new ArrayDeque<>());
                    Position pos = current;
                    while (pos != null) {
                        path.offerFirst(pos);
                        pos = parents.get(pos);
                    }
                    path.poll();
                    regionPaths.put(new Pair(region, current), path);
                    Logger.debug("[Preprocess]", "From " + region + " to " + current + ", Found.");
                }
                for (int i = 0; i < 4; i++) {
                    Position next = new Position(current.x + dx[i], current.y + dy[i]);
                    if (isValidPosition(map, next) && !parents.containsKey(next)) {
                        queue.offer(next);
                        parents.put(next, current);
                    }
                }
            }
        }
    }

    public Path aStarWithPreprocessing(char[][] map, Position start, Position end) {
        Logger.debug("[AStarGrid]", "start: " + start + ", end: " + end);
        Position startRegion = new Position(start.x / regionSize * regionSize, start.y / regionSize * regionSize);
        Position endRegion;
        if (map[end.x][end.y] == 'B')
            endRegion = end;
        else
            endRegion = new Position(end.x / regionSize * regionSize, end.y / regionSize * regionSize);
        // If in a same region, directly use aStar
        if (startRegion.equals(endRegion)) {
            return aStar(map, start, end, 3);
        }
        // From start to start region
        Path prePath;
        if (startRegion.equals(start)) prePath = new Path(new ArrayDeque<>()); // start on a region
        else {
            prePath = aStar(map, start, startRegion, 3);
            if (prePath == null) { // Cannot find a path from start to start region, directly search
                Logger.debug("[AStarGrid Warning]", "Can't optimize path, directly search.");
                return aStar(map, start, end, 3);
            }
        }
        // Try to find a path from start region to end region
        Logger.debug("[AStarGrid]", "Trying to find a path from " + startRegion + " to " + endRegion);
        Path regionPath = regionPaths.get(new Pair(startRegion, endRegion));
        if (regionPath != null) { // Lucky, found a path
            Logger.debug("[AStarGrid]", "From " + startRegion + " to " + endRegion + ", Found.");
            // Now try to find a path from end region to end
            Path pathToEnd = aStar(map, endRegion, end, 3);
            if (pathToEnd != null) {
                prePath.path.addAll(regionPath.path);
                prePath.path.addAll(pathToEnd.path);
                Logger.debug("[Perfect]", "HIT");
                return prePath;
            } else {
                // cannot find a path from end region to end, give up. This may not happen.
                return null;
            }
        } else {
            // TODO: That's fine, try to find by A*. This may not happen.
            Logger.debug("[AStarGrid Warning]", "Can't optimize path, directly search. ");
            return aStar(map, start, end, 3);
        }
    }

}