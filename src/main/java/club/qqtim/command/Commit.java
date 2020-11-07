package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.data.CommitObject;
import com.google.common.base.Charsets;
import lombok.Data;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * @title: Commit
 * @Author lijie78
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
        final byte[] commit = new club.qqtim.data.Data().getObject(id, ConstantVal.COMMIT);
        final String commitContent = new String(commit, Charsets.UTF_8);
        final String[] lines = commitContent.split(ConstantVal.NEW_LINE);

        CommitObject commitObject = new CommitObject();
        Arrays.stream(lines).forEach(line -> {
            final String[] fields = line.split(ConstantVal.SINGLE_SPACE);
            if (ConstantVal.TREE.equals(fields[0])) {
                commitObject.setTree(fields[1]);
            }
            if (ConstantVal.PARENT.equals(fields[0])) {
                commitObject.setParent(fields[1]);
            }
        });
        commitObject.setMessage(commitContent);
        return commitObject;
    }

    @Override
    public String call() {
        // calc the commit message
        WriteTree writeTree = new WriteTree();
        writeTree.setPath(ConstantVal.BASE_PATH);
        String commitMessage = String.format("%s %s\n", ConstantVal.TREE, writeTree.call());

        String headId = club.qqtim.data.Data.getHead();
        if (headId != null) {
            commitMessage += String.format("%s %s\n", ConstantVal.PARENT, headId);
        }

        commitMessage += String.format("\n%s\n", message);

        HashObject hashObject = new HashObject();
        final String commitId = hashObject.hashObject(commitMessage.getBytes(), ConstantVal.COMMIT);
        club.qqtim.data.Data.setHead(commitId);
        return commitId;
    }
}
