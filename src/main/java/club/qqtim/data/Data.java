package club.qqtim.data;

import club.qqtim.common.ConstantVal;
import club.qqtim.common.RegexConstantVal;
import club.qqtim.util.FileUtil;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
public class Data {

    public static final String ZIT_DIR = ".zit";

    public static final String OBJECTS_DIR = ZIT_DIR + "/objects";

    public static final String REFS_DIR = "refs";
    public static final String REFS_DIR_REAL = ZIT_DIR + "/" + REFS_DIR;


    public static void updateRef(String ref, String commitId) {
        FileUtil.createFile(commitId.getBytes(), String.format("%s/%s", ZIT_DIR, ref));
    }

    /**
     * 返回当前环境下的所有 ref 对象
     * @return HEAD 以及所有 refs 目录下的 refObject
     */
    public static List<RefObject> iteratorRefs() {
        List<String> refs = new ArrayList<>(1);
        refs.add(ConstantVal.HEAD);

        //获取 refs/ 目录下所有文件的相对路径，生成 refs/* 的格式
        final Path refsPath = Paths.get(REFS_DIR_REAL);
        final Path refsDir = Paths.get(REFS_DIR);
        final List<String> pathList;
        try {
            pathList = Files.walk(refsPath, Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .map(path -> refsDir.resolve(refsPath.relativize(path)).toString()).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }

        refs.addAll(pathList);

        return refs.stream().map(refName -> {
            final String ref = getRef(refName);
            final RefObject refObject = new RefObject();
            refObject.setRefName(refName);
            refObject.setRef(ref);
            return refObject;
        }).collect(Collectors.toList());
    }

    public static String getRef(String ref) {
        File file = new File(String.format("%s/%s", ZIT_DIR, ref));
        try {
            final CharSource charSource = com.google.common.io.Files.asCharSource(file, Charsets.UTF_8);
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
