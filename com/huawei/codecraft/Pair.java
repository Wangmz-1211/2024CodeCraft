package com.huawei.codecraft;

public class Pair {
    public Integer x;
    public Integer y;

    public Pair(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return x.equals(pair.x) && y.equals(pair.y);
    }

    @Override
    public int hashCode() {
        return 31 * x.hashCode() + y.hashCode();
    }
}
