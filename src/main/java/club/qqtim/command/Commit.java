package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.CommitObject;
import club.qqtim.data.RefValue;
import com.google.common.base.Charsets;
import lombok.Data;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @title: Commit
 * @Author rezeros.github.io
 * @Date: 2020/10/31
 * @Version 1.0.0
 */
@Data
@CommandLine.Command(name = "commit")
public class Commit implements Callable<String> {

    @CommandLine.Option(names = {"-m", "--message"}, description = "do commit", required = true)
    private boolean commit;

    @CommandLine.Parameters(index = "0")
    private String message;

    public static CommitObject getCommit(String id) {
        final byte[] commit = ZitContext.getObject(id, ConstantVal.COMMIT);
        assert commit != null;
        final String commitContent = new String(commit, Charsets.UTF_8);
        final String[] lines = commitContent.split(ConstantVal.NEW_LINE);

        List<String> parents = new ArrayList<>();
        CommitObject commitObject = new CommitObject();
        Arrays.stream(lines).forEach(line -> {
            final String[] fields = line.split(ConstantVal.SINGLE_SPACE);
            if (ConstantVal.TREE.equals(fields[0])) {
                commitObject.setTree(fields[1]);
            }
            if (ConstantVal.PARENT.equals(fields[0])) {
                parents.add(fields[1]);
            }
        });
        commitObject.setParents(parents);
        commitObject.setMessage(commitContent);
        return commitObject;
    }

    @Override
    public String call() {
        // calc the commit message
        WriteTree writeTree = new WriteTree();
        String commitMessage = String.format("%s %s\n", ConstantVal.TREE, writeTree.call());

        String headId = ZitContext.getRef(ConstantVal.HEAD).getValue();
        if (headId != null) {
            commitMessage += String.format("%s %s\n", ConstantVal.PARENT, headId);
        }
        String mergedHead = ZitContext.getRef(ConstantVal.MERGE_HEAD).getValue();
        if (mergedHead != null) {
            commitMessage += String.format("%s %s\n", ConstantVal.PARENT, mergedHead);
            ZitContext.deleteRef(ConstantVal.MERGE_HEAD, false);
        }

        commitMessage += String.format("\n%s\n", message);

        final String commitId = HashObject.hashObject(commitMessage.getBytes(Charsets.UTF_8), ConstantVal.COMMIT);
        ZitContext.updateRef(ConstantVal.HEAD, new RefValue(false, commitId));
        return commitId;
    }
}
