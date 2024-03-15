# Cost Functions

在进行多种行为的目标选择时，均遵循以下流程：

1.   对所有的可访问的目标进行遍历，计算cost，并放入优先队列中
2.   结束遍历后从优先队列中选择cost最低的一个目标
3.   涉及路径时对目标进行A*

## Robot choose Dock

$$
f(robot, dock) = \frac{distance(robot, dock)} {dock.score}
$$

-   `dock.score = 100000 * dock.loading_speed / transport_time`作为评价dock本身质量的标准
    -   越高的score表示dock能更快的装货并以更短的距离卖出货物
-   distance为一范数/曼哈顿距离，越短越好

##  Robot choose Goods

$$
f(robot, goods) = \frac{distance(robot, goods)^3}{goods.value}
$$

-   `0 < goods.value <= 200`是货物的价值

## Ship choose Dock

$$
f(ship, dock) = \frac{10000}{dock.score \times (\abs{dock.goods}+1)}
$$

-   `dock.goods`为dock中剩余货物量，由于多种因素只能获得近似值，防止除零错误进行处理





# Hyper Params

| H_MIN_SHIP_LOAD_TIME | H_MAX_SHIP_LOAD_TIME | H_FIND_GOODS_MAX_DISTANCE | score |
| -------------------- | -------------------- | ------------------------- | ----- |
| 10                   | 100                  | 4000                      | 9w+   |
| 10                   | 100                  | 8000                      | 9w+   |
| 10                   | 100                  | 10000                     | 8-10w |
| 10                   | 100                  | 15000                     | 9w+   |
| 30                   | 100                  | 15000                     | 11w+  |
| 50                   | 100                  | 15000                     | 11w+  |
| 70                   | 100                  | 15000                     | 12w+  |
| 100                  | 100                  | 15000                     | 12w+  |



# Score on maps

| Map          | Score  | Timeout |
| ------------ | ------ | ------- |
| map1.txt     | 128310 | no      |
| map2.txt     | 119332 | yes     |
| map-3.7.txt  | 163023 | no      |
| map-3.8.txt  | 171658 | no      |
| map-3.9.txt  | x      | yes     |
| map-3.10.txt | 110266 | no      |
| map-3.11.txt | 166395 | no      |
| map-3.12.txt | x      | yes     |
| map-3.13.txt | x      | yes     |

