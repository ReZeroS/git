package club.qqtim.arg;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * @author lijie78
 */
@Data
@Slf4j
@CommandLine.Command(name = "read-tree")
public class ReadTree implements Runnable {

    @CommandLine.Parameters(index = "0")
    private String hash;

    @Override
    public void run() {

    }
}
