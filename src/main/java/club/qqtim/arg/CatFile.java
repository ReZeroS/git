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

    @CommandLine.Parameters(index = "1", defaultValue = "blob")
    private String type;

    @Override
    public String call() throws Exception {
        final String fileContent = new club.qqtim.data.Data().getObject(hash, type);
        log.info(fileContent);
        return fileContent;
    }
}
