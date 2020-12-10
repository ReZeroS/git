package club.qqtim.context;

import club.qqtim.command.Commit;
import club.qqtim.common.ConstantVal;
import club.qqtim.common.RegexConstantVal;
import club.qqtim.data.CommitObject;
import club.qqtim.data.RefObject;
import club.qqtim.data.RefValue;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class ZitContext {

    public static final String ZIT_DIR = ".zit";

    public static final String OBJECTS_DIR = ZIT_DIR + "/objects";

    public static final String REFS_DIR = "refs";
    public static final String REFS_DIR_REAL = ZIT_DIR + "/" + REFS_DIR;





    public static List<String> iteratorCommitsAndParents(Collection<String> ids) {
        Deque<String> idsDeque = new LinkedList<>(ids);
        Set<String> visitedIds = new HashSet<>();
        Set<String> resultSet = new HashSet<>(ids);

        while (!idsDeque.isEmpty()) {
            // pop an element
            final String id = idsDeque.pollFirst();
            // check if visited this element
            if (visitedIds.contains(id)) {
                continue;
            }
            // if not visited, then add it to result and check it's parent
            visitedIds.add(id);
            resultSet.add(id);
            final CommitObject commit = Commit.getCommit(id);
            if (Objects.nonNull(commit.getParent())) {
                idsDeque.offerFirst(commit.getParent());
            }
        }
        return new ArrayList<>(resultSet);
    }


    /**
     * return all ref objects in the context
     *
     * @return HEAD and all ref objects in refs directory
     */
    public static List<RefObject> iteratorRefs() {
        List<String> refs = new ArrayList<>(1);
        refs.add(ConstantVal.HEAD);

        //get all file relative path from refs/ , like refs/* format
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
            final RefValue ref = getRef(refName);
            final RefObject refObject = new RefObject();
            refObject.setRefName(refName);
            refObject.setRefValue(ref);
            return refObject;
        }).collect(Collectors.toList());
    }

    /**
     * dereference it recursively for content   ref: <refname>
     *
     * @param ref ref
     * @return real ref
     */
    public static RefValue getRef(String ref) {
        return getRefInternal(ref).getRefValue();
    }

    private static RefObject getRefInternal(String ref) {
        String value = null;
        File file = new File(String.format("%s/%s", ZIT_DIR, ref));
        try {
            final CharSource charSource = com.google.common.io.Files.asCharSource(file, Charsets.UTF_8);
            value = Objects.requireNonNull(charSource.readFirstLine()).trim();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        boolean symbolic = (value != null && value.startsWith("ref:"));
        if (symbolic){
            value = value.split(":", 1)[1].trim();
            return getRefInternal(value);
        }
        return new RefObject(ref, new RefValue(false, value));
    }

    public static void updateRef(String ref, RefValue refValue) {
        FileUtil.createFile(refValue.getValue().getBytes(), String.format("%s/%s", ZIT_DIR, ref));
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
            final RefValue ref = ZitContext.getRef(String.format(path, refOrId));
            return ref.getValue();
        }
        if (RegexConstantVal.ALL_HEX.matcher(refOrId).find()) {
            return refOrId;
        }
        return null;
    }

    public void init() {
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
                        path.startsWith(ZitContext.ZIT_DIR)
                                || path.startsWith(".zit")
                                || path.startsWith("doc")
                                || path.startsWith("target")

                );
    }

}
