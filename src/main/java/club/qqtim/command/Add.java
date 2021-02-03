package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @title: Add
 * @Author lijie78
 * @Date: 2021/2/3
 * @Version 1.0.0
 */

@Slf4j
@lombok.Data
@CommandLine.Command(name = "add")
public class Add implements Runnable{


    @CommandLine.Parameters(paramLabel = "FILE", description = "one ore more files to archive")
    List<String> files;



    @Override
    public void run() {
        addFiles(files);
    }

    private void addFiles(List<String> files) {
        final String indexContent = FileUtil.getFileAsString(ConstantVal.INDEX, ConstantVal.NONE);
        final JsonElement jsonElement = new Gson().fromJson(indexContent, JsonElement.class);
        final JsonObject asJsonObject = jsonElement.getAsJsonObject();

        files.forEach(file  -> {
            final boolean isFile = !FileUtil.isFile(file);
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
