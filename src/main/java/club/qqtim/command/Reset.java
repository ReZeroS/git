package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.RefValue;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @title: Reset
 * @Author lijie78
 * @Date: 2020/12/13
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "reset")
public class Reset implements Runnable {

    @CommandLine.Parameters(index = "0", converter = IdConverter.class)
    private String commit;

    @Override
    public void run() {
        ZitContext.updateRef(ConstantVal.HEAD, new RefValue(false, commit));
    }
}
