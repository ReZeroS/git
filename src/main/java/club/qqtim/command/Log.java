package club.qqtim.command;

import lombok.Data;
import picocli.CommandLine;

/**
 * @title: Log
 * @Author lijie78
 * @Date: 2020/10/31
 * @Version 1.0.0
 */
@Data
@CommandLine.Command(name = "log")
public class Log implements Runnable{

    @Override
    public void run() {

    }
}
