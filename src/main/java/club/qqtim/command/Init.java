package club.qqtim.command;

import club.qqtim.context.ZitContext;
import lombok.Data;
import picocli.CommandLine;

/**
 * @author lijie78
 * 初始化 .zit 目录
 */
@Data
@CommandLine.Command(name = "init")
public class Init implements Runnable {
    @CommandLine.Option(names = "init", description = "zit init")
    private boolean init;

    @Override
    public void run() {
        new ZitContext().init();
    }
}
