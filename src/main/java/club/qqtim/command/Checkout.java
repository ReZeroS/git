package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.data.CommitObject;
import club.qqtim.context.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @title: Checkout
 * @Author lijie78
 * @Date: 2020/11/7
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "checkout")
public class Checkout implements Runnable {


    @CommandLine.Parameters(index = "0")
    private String id;



    @Override
    public void run() {
        String id = Data.getId(this.id);
        checkout(id);
    }

    private void checkout(String id) {
        final CommitObject commit = Commit.getCommit(id);
        final ReadTree readTree = new ReadTree();
        readTree.readTree(commit.getTree());
        Data.updateRef(ConstantVal.HEAD, id);
    }

}
