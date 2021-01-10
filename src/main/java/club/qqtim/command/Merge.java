package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.CommitObject;
import club.qqtim.data.RefValue;
import club.qqtim.diff.DiffUtil;
import club.qqtim.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @title: Merge
 * @Author rezeros.github.io
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

        final String mergeBase = MergeBase.getMergeBase(this.other, headRef);
        final CommitObject otherCommit = Commit.getCommit(this.other);

        if (headRef.equals(mergeBase)) {
            ReadTree.readTree(otherCommit.getTree());
            ZitContext.updateRef(ConstantVal.HEAD, new RefValue(false, this.other));
            log.info("Fast-forward merge, no need to commit");
        }

        ZitContext.updateRef(ConstantVal.MERGE_HEAD, new RefValue(false, this.other));

        final CommitObject mergeBaseCommit = Commit.getCommit(mergeBase);
        final CommitObject headCommit = Commit.getCommit(headRef);
        readTreeMerged(mergeBaseCommit.getTree(), headCommit.getTree(), otherCommit.getTree());
        log.info("merged in working tree\nPlease commit");
    }

    private void readTreeMerged(String baseTree, String headTree, String otherTree) {
        FileUtil.emptyCurrentDir();
        Map<String, String> pathBlobs = DiffUtil.mergeTrees(ReadTree.getTree(baseTree), ReadTree.getTree(headTree), ReadTree.getTree(otherTree));
        pathBlobs.forEach((path, blobContent) -> FileUtil.createFile(blobContent.getBytes(StandardCharsets.UTF_8), path));
    }
}
