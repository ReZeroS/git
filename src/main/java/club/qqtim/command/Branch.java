package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @title: Branch
 * @Author lijie78
 * @Date: 2020/12/9
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "branch")
public class Branch implements Runnable{


    @CommandLine.Parameters(index = "0")
    private String name;



    @CommandLine.Parameters(index = "1", defaultValue = ConstantVal.HEAD_ALIAS)
    private String startPoint;



    @Override
    public void run() {
        createBranch(name, startPoint);
    }

    private static void createBranch(String name, String startPoint) {
        Data.updateRef(String.format("refs/heads/%s", name), startPoint);
    }
}
