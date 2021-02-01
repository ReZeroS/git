package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.RefValue;
import club.qqtim.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

/**
 * @title: Push
 * @Author lijie78
 * @Date: 2021/2/1
 * @Version 1.0.0
 */
@Slf4j
@lombok.Data
@CommandLine.Command(name = "push")
public class Push implements Runnable{

    @CommandLine.Parameters(index = "0")
    private String remote;

    @CommandLine.Parameters(index = "1")
    private String branch;




    @Override
    public void run() {
        push(remote, String.format(ConstantVal.BASE_REFS_HEADS_PATH, branch));
    }

    private void push(String remotePath, String refName) {
        final String localRef = ZitContext.getRef(refName).getValue();
        final List<String> objectsToPush = ZitContext.iteratorObjectsInCommits(Collections.singletonList(localRef));

        for (String objectId : objectsToPush) {
            ZitContext.pushObject(objectId, remotePath);
        }

        FileUtil.setRootPathContext(remotePath);
        {
            ZitContext.updateRef(refName, new RefValue(false, localRef));
        }
        FileUtil.removeRootPathContext();

    }
}
