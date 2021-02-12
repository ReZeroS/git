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
    private String ref;



    @Override
    public void run() {
        checkout(this.ref);
    }


    /**
     * checkout command generate module by the commit describe
     * @param name could be head alias, hash and ref(branch, tags, HEAD...)
     */
    private void checkout(String name) {
        String id = ZitContext.getId(name);
        // get the name reference commit
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
