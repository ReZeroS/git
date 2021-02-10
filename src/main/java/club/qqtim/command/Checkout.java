package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.CommitObject;
import club.qqtim.data.RefValue;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Path;

/**
 * @title: Checkout
 * @Author rezeros.github.io
 * @Date: 2020/11/7
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "checkout")
public class Checkout implements Runnable {


    @CommandLine.Parameters(index = "0")
    private String commit;



    @Override
    public void run() {
        checkout(this.commit);
    }


    /**
     * checkout command generate module by the commit describe
     * @param name any thing can checkout
     */
    private void checkout(String name) {
        String id = ZitContext.getId(name);
        final CommitObject commit = Commit.getCommit(id);
        ReadTree.readTree(commit.getTree(), true);

        RefValue refValue;
        if (Branch.existBranch(name)) {
            refValue = new RefValue(true, String.format(ConstantVal.BASE_REFS_HEADS_PATH, name));
        } else {
            refValue = new RefValue(false, id);
        }
        ZitContext.updateRef(ConstantVal.HEAD, refValue,false);
    }

}
