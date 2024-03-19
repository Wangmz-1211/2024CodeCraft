package com.huawei.codecraft;

import java.util.ArrayDeque;
import java.util.Deque;

public class Path {

    Deque<Position> path = null;

    public Path(Deque<Position> path) {
        this.path = path;
    }

    public Path clone() {
        Deque<Position> newPath = new ArrayDeque<>();
        newPath.addAll(path);
        return new Path(newPath);
    }

    public Path cloneAndAppend(Position p) {
        Path newPath = clone();
        newPath.path.add(p);
        return newPath;
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public Position pollFirst() {
        return path.pollFirst();
    }

    public boolean offer(Position p) {
        return path.offer(p);
    }

    public boolean offerFirst(Position p) {
        return path.offerFirst(p);
    }


    public int size() {
        return path.size();
    }

    public Position poll() {
        return path.poll();
    }

    public Position peekTargetPos() {
        return path.peekLast();
    }
}
