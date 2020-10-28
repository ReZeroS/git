package club.qqtim.parser.support;

import club.qqtim.arg.Arg;
import club.qqtim.parser.Parser;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Arrays;

@Slf4j
public class CommandLineParser implements Parser {

    public Arg parse(String[] args) {
        log.debug(Arrays.toString(args));
        Arg arg = new Arg();
        new CommandLine(arg).parseArgs(args);
        return arg;
    }
}
