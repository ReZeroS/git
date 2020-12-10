package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.RefValue;
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
        String id = ZitContext.getId(this.id);
        if (id != null) {
            ZitContext.updateRef(String.format("refs/tags/%s", name), new RefValue(false, id));
        }
    }
}
