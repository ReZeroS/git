package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.diff.DiffUtil;
import club.qqtim.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

    @CommandLine.Option(names = {"--cached"})
    private boolean cached = false;


    @CommandLine.Option(names = {"--commit"})
    private String commit;


    @Override
    public String call() {
        String objectId;
        Map<String, String> treeFrom = new HashMap<>(16), treeTo;
        if (this.commit != null) {
            objectId = ZitContext.getId(this.commit);
            treeFrom = ReadTree.getTree(Commit.getCommit(objectId).getTree());
        }
        final String indexContent = FileUtil.getFileAsString(ConstantVal.INDEX, ConstantVal.NONE);

        if (cached) {
            treeTo = new Gson().fromJson(indexContent, new TypeToken<Map<String, String>>(){}.getType());
            if (this.commit == null) {
                objectId = ZitContext.getId(ConstantVal.HEAD_ALIAS);
                treeFrom = ReadTree.getTree(Commit.getCommit(objectId).getTree());
            }
        } else {
            treeTo = Diff.getWorkingTree();
            if (this.commit == null) {
                treeFrom = new Gson().fromJson(indexContent, new TypeToken<Map<String, String>>(){}.getType());
            }
        }

        final String diffChanges = DiffUtil.diffTrees(treeFrom, treeTo);
        log.info(diffChanges);
        return diffChanges;
    }

    public static Map<String, String> getWorkingTree() {
        final Path basePath = Paths.get(ConstantVal.BASE_PATH);
        try {
            return FileUtil.walk(basePath, Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .map(path -> basePath.relativize(path).toString())
                    .filter(ZitContext::isNotIgnored)
                    .collect(Collectors.toMap(key -> basePath.resolve(key).toString(), path -> {
                        final File file = new File(basePath.resolve(path).toString());
                        final byte[] fileContent = FileUtil.getFileAsBytes(file);
                        return HashObject.hashObject(fileContent, ConstantVal.BLOB);
                    }));
        } catch (IOException e) {
            log.error(e.toString());
        }
        return Collections.emptyMap();
    }


}
