package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import lombok.Data;
import picocli.CommandLine;

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

    @Override
    public String call() {
        // calc the commit message
        WriteTree writeTree = new WriteTree();
        writeTree.setPath(ConstantVal.BASE_PATH);
        String commitMessage = String.format("tree %s\n", writeTree.call());

        String headId = club.qqtim.data.Data.getHead();
        if (headId != null) {
            commitMessage += String.format("parent %s\n", headId);
        }

        commitMessage += String.format("\n%s\n", message);

        HashObject hashObject = new HashObject();
        final String commitId = hashObject.hashObject(commitMessage.getBytes(), "commit");
        club.qqtim.data.Data.setHead(commitId);
        return commitId;
    }
}
