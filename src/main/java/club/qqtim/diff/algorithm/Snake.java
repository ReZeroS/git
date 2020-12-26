package club.qqtim.diff.algorithm;

/**
 * @title: Snake
 * @Author lijie78
 * @Date: 2020/12/25
 * @Version 1.0.0
 */
public final class Snake extends PathNode {
    public Snake(int i, int j, PathNode prev) {
        super(i, j, prev);
    }

    @Override
    public Boolean isSnake() {
        return true;
    }
}
