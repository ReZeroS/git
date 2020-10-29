package club.qqtim.util;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public final class FileUtil {

    private FileUtil(){}


    public static void mkdir(String dirName) {
        boolean mkdir = new File(dirName).mkdir();
        if (mkdir) {
            log.info("Init empty .zit repository in {}{}{}", FileUtil.getCurrentDir(), "\\", dirName);
        } else {
            log.info("Create directory failed, please check your access right.");
        }
    }

    public static String getCurrentDir() {
        File file = new File(".");
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            log.error("can not get the current dir, please check your access right");
        }
        return null;
    }

    public static void createFile(byte[] fileContents, String fileName) throws IOException {
        File hashObject = new File(fileName);
        Files.write(fileContents, hashObject);
    }


    public static String getFileAsString(String path) throws IOException {
        CharSource charSource = Files.asCharSource(new File(path), Charsets.UTF_8);
        return charSource.read();
    }
}
