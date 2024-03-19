package com.huawei.codecraft;

public class Position {
    public Integer x;
    public Integer y;

    public Position(Integer x, Integer y) {
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
        Position position = (Position) o;
        return x.equals(position.x) && y.equals(position.y);
    }

    @Override
    public int hashCode() {
        return 31 * x.hashCode() + y.hashCode();
    }
}
