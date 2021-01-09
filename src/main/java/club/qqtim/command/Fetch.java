package club.qqtim.command;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @title: Fetch
 * @Author lijie78
 * @Date: 2021/1/1
 * @Version 1.0.0
 */
@Data
@Slf4j
@CommandLine.Command(name = "fetch")
public class Fetch implements Runnable{

    @CommandLine.Parameters(index = "0")
    private String remote;

    @Override
    public void run() {

    }

    public static void fetch(String remotePath) {
        log.debug("Will fetch the following refs:");

    }
}
