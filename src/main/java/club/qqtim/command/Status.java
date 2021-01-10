package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.CommitObject;
import club.qqtim.diff.DiffUtil;
import club.qqtim.diff.SimplyChange;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * @title: Status
 * @Author rezeros.github.io
 * @Date: 2020/12/12
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "status")
public class Status implements Runnable {

    @Override
    public void run() {
        final String headId = ZitContext.getId(ConstantVal.HEAD_ALIAS);
        final String branchName = ZitContext.getBranchName();
        if (Objects.nonNull(branchName)) {
            log.info("On branch {}", branchName);
        } else {
            assert headId != null;
            log.info("HEAD detached at {}", headId.substring(0, 11));
        }

        final String mergeHeadId = ZitContext.getRef(ConstantVal.MERGE_HEAD).getValue();
        if (Objects.nonNull(mergeHeadId)) {
            log.info("Merging with {}", mergeHeadId.substring(0, 11));
        }

        log.info("\nChanges to be committed:\n");

        final String headTree = Commit.getCommit(headId).getTree();
        final List<SimplyChange> simplyChanges = DiffUtil.iteratorChangedFiles(ReadTree.getTree(headTree), Diff.getWorkingTree());
        simplyChanges.forEach(simplyChange -> log.info(simplyChange.toString()));

    }
}
