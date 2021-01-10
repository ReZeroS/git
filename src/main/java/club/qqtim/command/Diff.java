package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.CommitObject;
import club.qqtim.diff.DiffUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * show what changes in working directory since last commit
 *
 * @title: Diff
 * @Author rezeros.github.io
 * @Date: 2020/12/26
 * @Version 1.0.0
 */
@Data
@Slf4j
@CommandLine.Command(name = "diff")
public class Diff implements Callable<String> {

    @CommandLine.Parameters(index = "0", defaultValue = ConstantVal.HEAD_ALIAS, converter = IdConverter.class)
    private String id;


    @Override
    public String call() {
        final CommitObject commit = Commit.getCommit(id);
        final String diffChanges = DiffUtil.diffTrees(
                ReadTree.getTree(commit.getTree()), getWorkingTree());
        log.info(diffChanges);
        return diffChanges;
    }

    public static Map<String, String> getWorkingTree() {
        final Path basePath = Paths.get(ConstantVal.BASE_PATH);
        try {
            return Files.walk(basePath, Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .map(path -> basePath.relativize(path).toString())
                    .filter(ZitContext::isNotIgnored)
                    .collect(Collectors.toMap(Function.identity(), path -> {
                        HashObject hashObject = new HashObject();
                        hashObject.setFile(new File(path));
                        hashObject.setType(ConstantVal.BLOB);
                        return hashObject.call();
                    }));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return Collections.emptyMap();
    }


}
