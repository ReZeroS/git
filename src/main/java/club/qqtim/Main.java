package club.qqtim;

import club.qqtim.parser.Parser;
import club.qqtim.parser.support.CommandLineParser;
import club.qqtim.util.FileUtil;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("hello, zit.");
        Parser parser = new CommandLineParser();
        parser.execute(args);
    }



}
