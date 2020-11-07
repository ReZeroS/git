package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.data.CommitObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @title: Log
 * @Author lijie78
 * @Date: 2020/10/31
 * @Version 1.0.0
 */
@Data
@Slf4j
@CommandLine.Command(name = "log")
public class Log implements Runnable{

    @CommandLine.Parameters(index = "0", defaultValue = ConstantVal.NONE)
    private String commitId;

    @Override
    public void run() {
        String id = ConstantVal.NONE.equals(commitId)? club.qqtim.data.Data.getHead(): commitId;
        while (id != null) {
            CommitObject commit = Commit.getCommit(id);
            log.info(String.format("%s %s\n", ConstantVal.COMMIT, id));
            log.info(String.format("%s\n", commit.getMessage()));
            id = commit.getParent();
        }
    }
}
