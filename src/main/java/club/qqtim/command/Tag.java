package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @title: Tag
 * @Author lijie78
 * @Date: 2020/11/7
 * @Version 1.0.0
 */
@Data
@Slf4j
@CommandLine.Command(name = "tag")
public class Tag implements Runnable {

    @CommandLine.Parameters(index = "0", description = "name")
    private String name;

    @CommandLine.Parameters(index = "1", defaultValue = ConstantVal.NONE, description = "commit id")
    private String id;



    @Override
    public void run() {
        String id = club.qqtim.data.Data.getId(this.id);
        club.qqtim.data.Data.updateRef(String.format("refs/tags/%s", name), id);
    }
}