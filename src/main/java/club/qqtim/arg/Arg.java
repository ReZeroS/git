package club.qqtim.arg;

import lombok.Data;
import picocli.CommandLine;

@Data
public class Arg {

    @CommandLine.Option(names = "init", description = "git init")
    private boolean init;

}
