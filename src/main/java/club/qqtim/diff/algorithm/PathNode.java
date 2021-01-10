package club.qqtim.diff.algorithm;

/**
 * @title: PathNode
 * @Author rezeros.github.io
 * @Date: 2020/12/25
 * @Version 1.0.0
 */

// https://chenshinan.github.io/2019/05/02/git%E7%94%9F%E6%88%90diff%E5%8E%9F%E7%90%86%EF%BC%9AMyers%E5%B7%AE%E5%88%86%E7%AE%97%E6%B3%95/
public abstract class PathNode {
    public final int i;
    public final int j;
    public final PathNode prev;

    public PathNode(int i, int j, PathNode prev) {
        this.i = i;
        this.j = j;
        this.prev = prev;
    }

    public abstract Boolean isSnake();

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("[");
        PathNode node = this;
        while (node != null) {
            buf.append("(");
            buf.append(node.i);
            buf.append(",");
            buf.append(node.j);
            buf.append(")");
            node = node.prev;
        }
        buf.append("]");
        return buf.toString();
    }
}