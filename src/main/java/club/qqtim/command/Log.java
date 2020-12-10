package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.data.CommitObject;
import club.qqtim.context.ZitContext;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

/**
 * @title: Log
 * @Author lijie78
 * @Date: 2020/10/31
 * @Version 1.0.0
 */

@lombok.Data
@Slf4j
@CommandLine.Command(name = "log")
public class Log implements Runnable{

    @CommandLine.Parameters(index = "0", defaultValue = ConstantVal.HEAD_ALIAS)
    private String id;

    @Override
    public void run() {
        // if no args, set HEAD
        // else use tag or hash as object id
        final String id = ZitContext.getId(this.id);
        final List<String> idList = ZitContext.iteratorCommitsAndParents(Collections.singletonList(id));
        idList.forEach(objectId -> {
            CommitObject commit = Commit.getCommit(objectId);
            log.info(String.format("%s %s\n", ConstantVal.COMMIT, objectId));
            log.info(String.format("%s\n", commit.getMessage()));
        });
    }
}
