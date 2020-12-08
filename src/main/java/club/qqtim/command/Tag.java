package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.data.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @title: Tag
 * @Author lijie78
 * @Date: 2020/11/7
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "tag")
public class Tag implements Runnable {

    @CommandLine.Parameters(index = "0", description = "name")
    private String name;

    @CommandLine.Parameters(index = "1", defaultValue = ConstantVal.HEAD_ALIAS, description = "commit id")
    private String id;



    @Override
    public void run() {
        String id = Data.getId(this.id);
        if (id != null) {
            club.qqtim.data.Data.updateRef(String.format("refs/tags/%s", name), id);
        }
    }
}
