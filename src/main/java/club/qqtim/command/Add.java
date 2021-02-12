package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @title: Add
 * @Author ReZeroS
 * @Date: 2021/2/3
 * @Version 1.0.0
 */

@Slf4j
@lombok.Data
@CommandLine.Command(name = "add")
public class Add implements Runnable{


    @CommandLine.Parameters(paramLabel = "FILE", description = "one ore more files to archive", defaultValue = ".")
    List<String> files;



    @Override
    public void run() {
        addFiles(files);
    }

    /**
     * update the index file content: if new file then add while old file will be updated
     * content is a big easy (key val directly) json like this:
     * {
     *    ".\\doc\\OptionsAndParameters2.png": "f4a36c21ce890b0fa10067c10dab416e9b71a13e"
     * }
     */
    private void addFiles(List<String> files) {
        final String indexContent = FileUtil.getFileAsString(ConstantVal.INDEX, ConstantVal.NONE);
        final JsonObject asJsonObject = new Gson().fromJson(indexContent, JsonObject.class);

        files.forEach(file  -> {
            final boolean isFile = FileUtil.isFile(file);
            if (isFile) {
                addFile(asJsonObject, file);
            } else { // is directory
                addDirectory(asJsonObject, file);
            }
        });
        FileUtil.createFile(asJsonObject.toString(), ConstantVal.INDEX);
    }

    private void addFile(JsonObject asJsonObject, String file) {
        HashObject hashObject = new HashObject();
        hashObject.setFile(new File(file));
        hashObject.setType(ConstantVal.BLOB);
        final String objectId = hashObject.call();
        asJsonObject.addProperty(file, objectId);
    }


    private void addDirectory(JsonObject asJsonObject, String file) {
        try {
            Files.walk(Paths.get(file), Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .forEach(regularFile -> addFile(asJsonObject, regularFile.toString()));
        } catch (IOException e) {
            log.error(e.toString());
        }
    }





}
