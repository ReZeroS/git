package club.qqtim.data;

import lombok.Data;

import java.util.List;

/**
 * @title: Commit
 * @Author rezeros.github.io
 * @Date: 2020/10/31
 * @Version 1.0.0
 */
@Data
public class CommitObject {

    /**
     * write tree id
     */
    private String tree;

    /**
     * parents commit id
     */
    private List<String> parents;

    /**
     * commit message
     */
    private String message;

}
