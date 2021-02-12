package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.RefValue;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import static club.qqtim.common.ConstantVal.BASE_REFS_TAGS_PATH;

/**
 * @title: Tag
 * @Author rezeros.github.io
 * @Date: 2020/11/7
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "tag")
public class Tag implements Runnable {

    @CommandLine.Parameters(index = "0", description = "name")
    private String name;

    @CommandLine.Parameters(index = "1", defaultValue = ConstantVal.HEAD_ALIAS, converter = IdConverter.class, description = "commit id")
    private String id;



    @Override
    public void run() {
        if (id != null) {
            String tag = String.format(BASE_REFS_TAGS_PATH, name);
            ZitContext.updateRef(tag, new RefValue(false, id));
        }
    }
}
