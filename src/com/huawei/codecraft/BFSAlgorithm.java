package com.huawei.codecraft;

import java.util.*;

public class BFSAlgorithm implements Algorithm {
    private char[][] map = null;
    private final Map<Pair, Path> regionPaths = new HashMap<>();
    private final Set<Position> regions = new HashSet<>();
    private Config config = null;

    public Dock[] docks = null;
    public Robot[] robots = null;
    public int frameId = 0;

    public BFSAlgorithm() {
    }

    public BFSAlgorithm(Config config) {
        this.config = config;
    }

    private boolean isValidPosition(char[][] map, Position position) {
        return isValidPosition(map, position.x, position.y);
    }

    private boolean isValidPosition(char[][] map, int x, int y) {
        if (x < 0 || y < 0 || x >= map.length || y >= map[0].length) {
            return false;
        }
        return isObstacle(map, x, y);
    }

    private boolean isObstacle(char[][] map, int x, int y) {
        return map[x][y] != '#' && map[x][y] != '*' && map[x][y] != 'W' && map[x][y] != '.';
    }

    /**
     * Label the map with robots as '#'.<br/>
     */
    private void lableMap() {
        for (int botId = 0; botId < config.N_ROBOT; botId++) {
            Position pos = robots[botId].getPos();
            map[pos.x][pos.y] = '#';
        }
    }

    /**
     * Restore the map after labeling the map.<br/>
     */
    private void restoreMap() {
        for (int botId = 0; botId < config.N_ROBOT; botId++) {
            Position pos = robots[botId].getPos();
            char c = 'A';
            for (Dock dock : docks) {
                if (dock.inDock(pos)) {
                    c = 'B';
                    break;
                }
            }
            map[pos.x][pos.y] = c;
        }
    }

    /**
     * Find the path from start to end by BFS.<br/>
     *
     * @param map
     * @param start
     * @param end
     * @return
     */
    private Path bfs(char[][] map, Position start, Position end) {

        lableMap();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Queue<Position> queue = new LinkedList<>();
        queue.offer(start);
        Map<Position, Position> parent = new HashMap<>();
        parent.put(start, null);
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(end)) {
                Path path = new Path(new ArrayDeque<>());
                while (current != null) {
                    path.offerFirst(current);
                    current = parent.get(current);
                }
                path.poll();
                restoreMap();
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
        restoreMap();
        return null;
    }

    @Override
    public Path findGoods(char[][] map, Robot bot, GoodsBucket goodsBucket) {
//        return findGoodsByStrategy(map, bot, goodsBucket);
        return findGoodsByBfs(map, bot, goodsBucket);
    }

    /**
     * Find the nearest goods by strategy.<br/>
     * The strategy is to find the goods with the lowest cost.<br/>
     *
     * @param map
     * @param bot
     * @param goodsBucket
     * @return
     */
    private Path findGoodsByStrategy(char[][] map, Robot bot, GoodsBucket goodsBucket) {
        Goods target = bot.chooseGoods(goodsBucket);
        if (target == null) return null;
        Path p = findPath(bot.getPos(), target.getPos());
        if (p == null) {
            goodsBucket.remove(target);
            return null;
        }
        return p;
    }

    /**
     * Find the nearest H_CANDIDATE_SIZE goods, then choose the one with the highest value.<br/>
     *
     * @param map
     * @param bot
     * @param goodsBucket
     * @return
     */
    private Path findGoodsByBfs(char[][] map, Robot bot, GoodsBucket goodsBucket) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Deque<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        int candidateSize = config.H_CANDIDATE_SIZE;
        List<Goods> candidates = new LinkedList<>();
        queue.offer(bot.getPos());
        visited.add(bot.getPos());
        Map<Position, Position> parent = new HashMap<>();
        parent.put(bot.getPos(), null);
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (goodsBucket.get(current.x, current.y) != null && !goodsBucket.get(current.x, current.y).assigned) {
                candidates.add(goodsBucket.get(current.x, current.y));
                if (candidates.size() == candidateSize) {
                    Goods bestCandidate = candidates.get(0);
                    for (Goods candidate : candidates) {
                        if (candidate.value > bestCandidate.value) {
                            bestCandidate = candidate;
                        }
                    }
                    bestCandidate.assigned = true;
                    Path p = new Path(new ArrayDeque<>());
                    while (current != null) {
                        p.offerFirst(current);
                        current = parent.get(current);
                    }
                    p.poll();
                    return p;
                }
            }
            for (int i = 0; i < 4; i++) {
                Position next = new Position(current.x + dx[i], current.y + dy[i]);
                if (isValidPosition(map, next) && !visited.contains(next)) {
                    queue.offer(next);
                    visited.add(next);
                    parent.put(next, current);
                }
            }
        }
        return null;
    }

    /**
     * Find the path to the dock.<br/>
     *
     * @param map
     * @param docks
     * @param bot
     * @return
     */
    @Override
    public Path findDock(char[][] map, Dock[] docks, Robot bot) {
        Random random = new Random();
        Dock target = bot.chooseDock();
        if (target == null) return null;
        Position targetPos = target.getPos();
        Path p = findPath(bot.getPos(), targetPos);
        if (p == null)
            bot.punishDock(target);
        return p;
    }


    /**
     * Preprocess the map.<br/>
     * 1. BFS from a point to randomly generate some regions.<br/>
     * 2. BFS from each region to each region to generate paths between regions.
     *
     * @param map the map to preprocess, the '.' robots could pass should be marked as 'A'.
     */
    @Override
    public void preprocess(char[][] map) {
        Random random = new Random();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        this.map = map;
        int m = map.length, n = map[0].length;
        int x = 50, y = 60;
        Deque<Position> queue = new LinkedList<>();
        queue.offer(new Position(x, y));
        Set<Position> visited = new HashSet<>();
        visited.add(new Position(x, y));
        // Generate regions
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            for (int i = 0; i < 4; i++) {
                Position next = new Position(current.x + dx[i], current.y + dy[i]);
                if (!isValidPosition(map, next)) continue;
                if (!visited.contains(next) && map[next.x][next.y] == 'A') {
                    queue.offer(next);
                    visited.add(next);
                    if (random.nextInt(1000) < config.H_INIT_REGION_RATE) {
                        regions.add(next);
                    }
                }
            }
        }
        for (Dock dock : docks) {
            regions.add(dock.getPos());
        }

        // Build Paths
        for (Position region : regions) {
            if (!(map[region.x][region.y] == 'A' || map[region.x][region.y] == 'B')) continue;
            queue = new ArrayDeque<>();
            Map<Position, Position> parents = new HashMap<>();
            queue.offer(region);
            parents.put(region, null);
            while (!queue.isEmpty()) {
                Position current = queue.poll();
                if ((regions.contains(current) && !current.equals(region)) || map[current.x][current.y] == 'B') {
                    // build path
                    Path path = new Path(new ArrayDeque<>());
                    Position pos = current;
                    while (pos != null) {
                        path.offerFirst(pos);
                        pos = parents.get(pos);
                    }
                    path.poll();
                    regionPaths.put(new Pair(region, current), path);

                }
                boolean flag = random.nextBoolean();
                if (flag) {
                    for (int i = 3; i >= 0; i--) {
                        Position next = new Position(current.x + dx[i], current.y + dy[i]);
                        if (isValidPosition(map, next) && !parents.containsKey(next)) {
                            queue.offer(next);
                            parents.put(next, current);
                        }
                    }
                } else {
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
    }

    /**
     * Find the nearest region of a position.<br/>
     * This method will be used in findPath method.
     *
     * @param position the position to find the nearest region
     * @return the nearest region of the position
     */
    private Position findNearestRegion(Position position) {
        if (regions.contains(position)) return position;
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        Deque<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        queue.offer(position);
        visited.add(position);
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (regions.contains(current)) {
                return current;
            }
            for (int i = 0; i < 4; i++) {
                Position next = new Position(current.x + dx[i], current.y + dy[i]);
                if (isValidPosition(map, next) && !visited.contains(next)) {
                    queue.offer(next);
                    visited.add(next);
                }
            }
        }
        return null;
    }

    /**
     * Find the path from start to end.<br/>
     * 1. Find the nearest region for start and end.<br/>
     * 2. Find the path from start to startRegion.<br/>
     * 3. Find the path from endRegion to end.<br/>
     * 4. Find the path from startRegion to endRegion.<br/>
     * 5. Merge the paths.
     *
     * @param start start position
     * @param end   end position
     * @return the path from start to end, not including start.
     */
    private Path findPath(Position start, Position end) {
        Position startRegion = findNearestRegion(start);
        Position endRegion = findNearestRegion(end);
        if (startRegion == null || endRegion == null) return null;
        if (startRegion.equals(endRegion)) {
            return bfs(map, start, end);
        }
        if (!regionPaths.containsKey(new Pair(startRegion, endRegion))) {
            return null;
        }
        Path startToStartRegion = bfs(map, start, startRegion);
        if (startToStartRegion == null) return null;
        Path endToEndRegion = bfs(map, endRegion, end);
        if (endToEndRegion == null) return null;
        Path startRegionToEndRegion = regionPaths.get(new Pair(startRegion, endRegion));
        if (startRegionToEndRegion == null) return null;
        Path path = new Path(new ArrayDeque<>());
        path.path.addAll(startToStartRegion.path);
        path.path.addAll(startRegionToEndRegion.path);
        path.path.addAll(endToEndRegion.path);
        return path;
    }
}