package club.qqtim;

import club.qqtim.arg.Arg;
import club.qqtim.data.Data;
import club.qqtim.parser.Parser;
import club.qqtim.parser.support.CommandLineParser;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("hello, zit.");
        Parser parser = new CommandLineParser();
        Arg parsedArg = parser.parse(args);
        if (parsedArg.isInit()) {
            new Data().init();
        } else {
            log.info("2");
        }

    }
}
