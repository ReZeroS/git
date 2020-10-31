package club.qqtim.data;

import club.qqtim.common.ConstantVal;
import club.qqtim.util.FileUtil;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


@Slf4j
public class Data {

    public static final String ZIT_DIR = ".zit";

    public static final String OBJECTS_DIR = ZIT_DIR + "/objects";

    public static final String HEAD_FILE = ZIT_DIR + "/HEAD";

    public static void setHead(String commitId) {
        FileUtil.createFile(commitId.getBytes(), HEAD_FILE);
    }

    public static String getHead() {
        File file = new File(HEAD_FILE);
        final CharSource charSource = Files.asCharSource(file, Charsets.UTF_8);
        try {
            return Objects.requireNonNull(charSource.readFirstLine()).trim();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public void init(){
        initRoot();
        initObjects();
    }

    private void initRoot() {
        FileUtil.mkdir(ZIT_DIR);
    }

    private void initObjects() {
        FileUtil.mkdir(OBJECTS_DIR);
    }

    public byte[] getObject(String hash) {
        return getObject(hash, ConstantVal.BLOB);
    }

    public byte[] getObject(String hash, String type) {
        String path = OBJECTS_DIR + "/" + hash;
        log.info("get the content of {} file", path);
        try {
            return FileUtil.getFileByteSource(path, type).read();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public String getObjectAsString(String hash, String type) {
        String path = OBJECTS_DIR + "/" + hash;
        log.info("get the content of {} file", path);
        try {
            return FileUtil.getFileAsString(path, type);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * @param path file path
     * @return whether it's zit meta file
     */
    public static boolean isNotIgnored(String path) {
        return !isIgnored(path);
    }
    /**
     * @param path file path
     * @return whether it's zit meta file
     */
    public static boolean isIgnored(String path) {
        return path != null &&
                (
                        path.startsWith(club.qqtim.data.Data.ZIT_DIR)
                                || path.startsWith(".zit")
                                || path.startsWith("doc")
                                || path.startsWith("target")

                );
    }

}
