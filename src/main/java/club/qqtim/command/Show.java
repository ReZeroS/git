package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.CommitObject;
import club.qqtim.diff.DiffUtil;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Objects;

/**
 * @title: Show
 * @Author rezeros.github.io
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
        if (Objects.nonNull(commit.getParents()) && !commit.getParents().isEmpty()) {
            parentTree = Commit.getCommit(commit.getParents().get(0)).getTree();
        }

        Log.printCommit(id, commit);

        final String result = DiffUtil.diffTrees(
                ReadTree.getTree(parentTree), ReadTree.getTree(commit.getTree())
        );

        log.info(result);
    }
}
