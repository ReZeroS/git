package club.qqtim.diff.algorithm;

/**
 * @title: DiffNode
 * @Author rezeros.github.io
 * @Date: 2020/12/25
 * @Version 1.0.0
 */
public final class DiffNode extends PathNode {
    public DiffNode(int i, int j, PathNode prev) {
        super(i, j, prev);
    }

    @Override
    public Boolean isSnake() {
        return false;
    }
}
