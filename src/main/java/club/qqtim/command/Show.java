package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.CommitObject;
import club.qqtim.diff.Diff;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Objects;

/**
 * @title: Show
 * @Author lijie78
 * @Date: 2020/12/13
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "show")
public class Show implements Runnable {

    @CommandLine.Parameters(index = "0", defaultValue = ConstantVal.HEAD_ALIAS, converter = IdConverter.class)
    private String id;

    @Override
    public void run() {
        final CommitObject commit = Commit.getCommit(id);

        String parentTree = null;
        if (Objects.nonNull(commit.getParent())) {
            parentTree = Commit.getCommit(commit.getParent()).getTree();
        }

        Log.printCommit(id, commit);

        final String result = Diff.diffTrees(
                ReadTree.getTree(parentTree), ReadTree.getTree(commit.getTree())
        );

        log.info(result);
    }
}
