package club.qqtim;

import club.qqtim.parser.Parser;
import club.qqtim.parser.support.CommandLineParser;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("hello, zit.");
        Parser parser = new CommandLineParser();
        parser.execute(args);
    }
}
