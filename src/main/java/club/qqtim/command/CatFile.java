package club.qqtim.command;


import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author rezeros.github.io
 */
@Data
@Slf4j
@CommandLine.Command(name = "cat-file")
public class CatFile implements Callable<String> {

    @CommandLine.Parameters(index = "0", converter = IdConverter.class)
    private String id;

    @CommandLine.Parameters(index = "1", defaultValue = "blob")
    private String type;

    @Override
    public String call() {
        final String fileContent = ZitContext.getObjectAsString(id, type);
        log.info(fileContent);
        return fileContent;
    }
}
