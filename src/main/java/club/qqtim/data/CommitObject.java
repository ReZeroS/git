package club.qqtim.data;

import lombok.Data;

/**
 * @title: Commit
 * @Author lijie78
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
     * parent commit id
     */
    private String parent;

    /**
     * commit message
     */
    private String message;

}
