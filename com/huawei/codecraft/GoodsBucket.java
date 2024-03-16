package com.huawei.codecraft;

import java.util.*;

import static com.huawei.codecraft.Config.*;

public class GoodsBucket {

    public Goods[][] goodsArr = new Goods[Config.S_MAP][Config.S_MAP];
    public Set<Goods> goodsSet = new HashSet<>();
    private Set<Goods>[] buckets = new HashSet[N_GOODS_BUCKET];

    public int add_iterator = 0;
    public int clean_iterator = 0;

    public GoodsBucket() {
        for(int i = 0; i < N_GOODS_BUCKET; i++) {
            buckets[i] = new HashSet<>();
        }
    }

    public void add(Goods goods) {
        int x = goods.x;
        int y = goods.y;

        // Get the target bucket
        Set<Goods> bucket = this.buckets[add_iterator];
        add_iterator = (add_iterator + 1) % N_GOODS_BUCKET;

        // Add the goods
        bucket.add(goods);
        goodsSet.add(goods);
        goodsArr[x][y] = goods;
    }

    public void remove(Goods  goods) {
        this.goodsArr[goods.x][goods.y] = null;
        this.goodsSet.remove(goods);
    }

    public void assignAt(Pair pos) {
        Goods goods = this.goodsArr[pos.x][pos.y];
        if (goods != null) {
            goods.assigned = true;
        }
    }

    public void clean(int frameId) {
        Set<Goods> bucket = this.buckets[clean_iterator];
        clean_iterator = (clean_iterator + 1) % N_GOODS_BUCKET;
        ArrayList<Goods> toRemove = new ArrayList<>();
        for (Goods goods : bucket) {
            if (!goodsSet.contains(goods)) {
                toRemove.add(goods);
                continue;
            }
            if (goods.expire_time < frameId) {
                toRemove.add(goods);
            }
            // log here
        }
        for (Goods goods : toRemove) {
            bucket.remove(goods);
            goodsSet.remove(goods);
            goodsArr[goods.x][goods.y] = null;
        }
    }

    public Goods get(int x, int y) {
        return goodsArr[x][y];
    }


}
