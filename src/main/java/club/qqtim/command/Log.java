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
    private String id;

    @Override
    public void run() {
        // if no args, set HEAD
        // else use tag or hash as object id
        String id = ConstantVal.NONE.equals(this.id)?
                club.qqtim.data.Data.getRef(ConstantVal.HEAD)
                : club.qqtim.data.Data.getId(this.id);
        while (id != null) {
            CommitObject commit = Commit.getCommit(id);
            log.info(String.format("%s %s\n", ConstantVal.COMMIT, id));
            log.info(String.format("%s\n", commit.getMessage()));
            id = commit.getParent();
        }
    }
}
