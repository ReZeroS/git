package club.qqtim.diff;

import lombok.Data;

/**
 * @title: Simply
 * @Author rezeros.github.io
 * @Date: 2020/12/26
 * @Version 1.0.0
 */
@Data
public class SimplyChange {

    private String action;

    private String path;

    @Override
    public String toString() {
        return action + " : " + path;
    }
}
