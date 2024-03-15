## History

| Cost Function | W1   | W2   | Timeout Times   | Score |
| ------------- | ---- | ---- | --------------- | ----- |
| 2-norm        | 1.0  | 0.5  | 697             | 78753 |
| 2-norm        | 1.0  | 1.0  | 434             | 77859 |
| 1-norm        | 1.0  | 1.0  | 7 (up to 410ms) | 61013 |
| 1-norm        | 1.0  | 0.5  | 6 (up to 408ms) | 61013 |
| diag          | 1.0  | 0.5  | 7               | 33058 |
| diag          | 1.0  | 1.0  | 7               | 33058 |

## Cost Functions

1-norm
$$
f(x_s, y_s, x_t, y_t) = \abs{x_t-x_s} + \abs{y_t - y_s}
$$
2-norm
$$
f(x_s, y_s, x_t, y_t) = \sqrt{(x_t-x_s)^2 + (y_t - y_s)^2}
$$
diag
$$
f(x_s, y_s, x_t, y_t) = \sqrt{2}\min(\Delta x, \Delta y) + \abs{\Delta y - \Delta x}
$$
