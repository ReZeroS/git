package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.RefValue;
import lombok.Data;
import picocli.CommandLine;

/**
 * @author rezeros.github.io
 * 初始化 .zit 目录
 */
@Data
@CommandLine.Command(name = "init")
public class Init implements Runnable {
    @CommandLine.Option(names = "init", description = "zit init")
    private boolean init;

    @Override
    public void run() {
        ZitContext.init();
        final RefValue refValue = new RefValue(true, String.format(ConstantVal.BASE_REFS_HEADS_PATH, ConstantVal.DEFAULT_BRANCH));
        ZitContext.updateRef(ConstantVal.HEAD, refValue);
    }
}
