package club.qqtim.util;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public final class FileUtil {

    private FileUtil(){}


    public static boolean mkdir(String dirName) {
        return new File(dirName).mkdir();
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
}
