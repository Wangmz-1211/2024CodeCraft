package com.huawei.codecraft;

import java.util.ArrayDeque;
import java.util.Deque;

public class Path {

    Deque<Pair> path = null;

    public Path(Deque<Pair> path) {
        this.path = path;
    }

    public Path clone() {
        Deque<Pair> newPath = new ArrayDeque<>();
        newPath.addAll(path);
        return new Path(newPath);
    }

    public Path cloneAndAppend(Pair p) {
        Path newPath = clone();
        newPath.path.add(p);
        return newPath;
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public Pair pollFirst() {
        return path.pollFirst();
    }

    public boolean offer(Pair p) {
        return path.offer(p);
    }

    public boolean offerFirst(Pair p) {
        return path.offerFirst(p);
    }


    public int size() {
        return path.size();
    }

    public Pair poll() {
        return path.poll();
    }

    public Pair peekTargetPos() {
        return path.peekLast();
    }
}
