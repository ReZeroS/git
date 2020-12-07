package club.qqtim.data;

import club.qqtim.common.ConstantVal;
import club.qqtim.common.RegexConstantVal;
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


    public static void updateRef(String ref, String commitId) {
        FileUtil.createFile(commitId.getBytes(), String.format("%s/%s", ZIT_DIR, ref));
    }



    public static String getRef(String ref) {
        File file = new File(String.format("%s/%s", ZIT_DIR, ref));
        try {
            final CharSource charSource = Files.asCharSource(file, Charsets.UTF_8);
            return Objects.requireNonNull(charSource.readFirstLine()).trim();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * @param refOrId ref or id
     * @return ref
     */
    public static String getId(String refOrId) {
        if (ConstantVal.HEAD_ALIAS.equals(refOrId)) {
            refOrId = ConstantVal.HEAD;
        }
        for (String path : ConstantVal.REF_REGISTRY_DIRECTORIES) {
            final String ref = Data.getRef(String.format(path, refOrId));
            if (Objects.nonNull(ref)) {
                return ref;
            }
        }
        if (RegexConstantVal.ALL_HEX.matcher(refOrId).find()) {
            return refOrId;
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
