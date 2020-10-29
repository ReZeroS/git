package club.qqtim.arg;


import club.qqtim.util.FileUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author lijie78
 */
@Data
@Slf4j
@CommandLine.Command(name = "cat-file")
public class CatFile implements Callable<String> {

    @CommandLine.Parameters(index = "0")
    private String hash;

    public String call() throws Exception {
        String path = club.qqtim.data.Data.OBJECTS + "/" + hash;
        log.info("Printed the content of {} file", path);
        String fileContent = FileUtil.getFileAsString(path);
        log.info(fileContent);
        return fileContent;
    }
}
