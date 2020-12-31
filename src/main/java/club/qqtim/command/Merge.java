package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.CommitObject;
import club.qqtim.diff.DiffUtil;
import club.qqtim.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @title: Merge
 * @Author lijie78
 * @Date: 2020/12/26
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "merge")
public class Merge implements Runnable {

    @CommandLine.Parameters(index = "0", converter = IdConverter.class)
    private String other;

    @Override
    public void run() {
        final String headRef = ZitContext.getRef(ConstantVal.HEAD).getValue();
        final CommitObject head = Commit.getCommit(headRef);
        final CommitObject other = Commit.getCommit(this.other);

        readTreeMerged(head.getTree(), other.getTree());
        log.info("merged in working tree");
    }

    private void readTreeMerged(String fromTree, String toTree) {
        FileUtil.emptyCurrentDir();
        Map<String, String> pathBlobs = DiffUtil.mergeTrees(ReadTree.getTree(fromTree), ReadTree.getTree(toTree));
        pathBlobs.forEach((path, blobContent) -> FileUtil.createFile(blobContent.getBytes(StandardCharsets.UTF_8), path));
    }
}
