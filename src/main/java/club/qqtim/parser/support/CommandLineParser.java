package club.qqtim.parser.support;

import club.qqtim.arg.Zit;
import club.qqtim.parser.Parser;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Arrays;

@Slf4j
public class CommandLineParser implements Parser {

    @Override
    public void execute(String[] args) {
        log.debug(Arrays.toString(args));
        new CommandLine(new Zit()).execute(args);
    }
}
