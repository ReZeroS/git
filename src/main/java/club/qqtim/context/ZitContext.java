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
            if (Objects.nonNull(commit.getParents()) && !commit.getParents().isEmpty()) {
                // todo java ugly slice
                idsDeque.offerFirst(commit.getParents().get(0));
                if (commit.getParents().size() > 1) {
                    final List<String> lastCommits = commit.getParents().subList(1, commit.getParents().size());
                    lastCommits.forEach(idsDeque::offerLast);
                }
            }
        }
        return new ArrayList<>(resultSet);
    }



    public static List<RefObject> iteratorRefs() {
        return iteratorRefs(ConstantVal.EMPTY, true);
    }

    public static List<RefObject> iteratorRefs(String prefix) {
        return iteratorRefs(prefix, true);
    }

    public static List<RefObject> iteratorRefs(boolean deference) {
        return iteratorRefs(ConstantVal.EMPTY, deference);
    }
    /**
     * return all ref objects in the context
     *
     * @return HEAD and all ref objects in refs directory
     */
    public static List<RefObject> iteratorRefs(String prefix, boolean dereference) {
        List<String> refs = new ArrayList<>(1);
        refs.add(ConstantVal.HEAD);
        refs.add(ConstantVal.MERGE_HEAD);

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

        return refs.stream()
                .filter(refName -> refName.startsWith(prefix))
                .map(refName -> {
                    final RefValue ref = getRef(refName, dereference);
                    if (Objects.isNull(ref.getValue())) {
                        return null;
                    }
                    final RefObject refObject = new RefObject();
                    refObject.setRefName(refName);
                    refObject.setRefValue(ref);
                    return refObject;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    public static RefValue getRef(String ref) {
        return getRef(ref, true);
    }
/**
     * dereference it recursively for content   ref: <refname>
     *
     * @param ref ref
     * @return real ref
     */
    public static RefValue getRef(String ref, boolean dereference) {
        return getRefInternal(ref, dereference).getRefValue();
    }

    private static RefObject getRefInternal(String ref) {
        return getRefInternal(ref, true);
    }

    private static RefObject getRefInternal(String ref, boolean dereference) {
        String value = null;
        File file = new File(String.format("%s/%s", ZIT_DIR, ref));
        if (file.exists()) {
            try {
                final CharSource charSource = com.google.common.io.Files.asCharSource(file, Charsets.UTF_8);
                value = Objects.requireNonNull(charSource.readFirstLine()).trim();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        boolean symbolic = (value != null && value.startsWith("ref:"));
        if (symbolic){
            value = value.split(":", 2)[1].trim();
            if (dereference) {
                return getRefInternal(value);
            }
        }
        return new RefObject(ref, new RefValue(symbolic, value));
    }

    public static void updateRef(String ref, RefValue refValue) {
        updateRef(ref, refValue, true);
    }

    public static void updateRef(String ref, RefValue refValue, boolean dereference) {
        final String refName = getRefInternal(ref, dereference).getRefName();
        String value;
        if (refValue.getSymbolic()) {
            value = String.format("ref: %s", refValue.getValue());
        } else {
            value = refValue.getValue();
        }
        FileUtil.createFile(value.getBytes(Charsets.UTF_8), String.format("%s/%s", ZIT_DIR, refName));
    }

    public static void deleteRef(String ref) {
        deleteRef(ref, true);
    }

    public static void deleteRef(String ref, boolean dereference) {
        final RefObject refInternal = getRefInternal(ref, dereference);
        if (Objects.nonNull(refInternal)) {
            final String refName = refInternal.getRefName();
            FileUtil.deleteDir(String.format("%s/%s", ZIT_DIR, refName));
        }
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
            final String refValue = String.format(path, refOrId);
            final RefValue ref = ZitContext.getRef(refValue, false);
            if (Objects.nonNull(ref.getValue())) {
                // pay attention, check use dereference false but get use true
                return ZitContext.getRef(refValue).getValue();
            }
        }
        if (RegexConstantVal.ALL_HEX.matcher(refOrId).find()) {
            return refOrId;
        }
        return null;
    }

    public static String getBranchName(){
        final RefValue head = getRef(ConstantVal.HEAD, false);
        if (!head.getSymbolic()) {
            return null;
        }
        final String headPath = head.getValue();
        if (headPath.startsWith("refs/heads")) {
            return Paths.get("refs/heads").relativize(Paths.get(headPath)).toString();
        }
        return null;
    }

    public static void init() {
        initRoot();
        initObjects();
    }

    private static void initRoot() {
        FileUtil.mkdir(ZIT_DIR);
    }

    private static void initObjects() {
        FileUtil.mkdir(OBJECTS_DIR);
    }

    public static byte[] getObject(String hash) {
        return getObject(hash, ConstantVal.BLOB);
    }

    public static byte[] getObject(String hash, String type) {
        String path = OBJECTS_DIR + "/" + hash;
        log.debug("get the content of {} file", path);
        try {
            return FileUtil.getFileByteSource(path, type).read();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static String getObjectAsString(String hash, String type) {
        String path = OBJECTS_DIR + "/" + hash;
        log.debug("get the content of {} file", path);
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
