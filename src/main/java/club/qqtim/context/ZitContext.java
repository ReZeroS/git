package club.qqtim.context;

import club.qqtim.command.Commit;
import club.qqtim.command.ReadTree;
import club.qqtim.common.ConstantVal;
import club.qqtim.common.RegexConstantVal;
import club.qqtim.data.CommitObject;
import club.qqtim.data.RefObject;
import club.qqtim.data.RefValue;
import club.qqtim.data.ZitObject;
import club.qqtim.util.FileUtil;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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


    /**
     * behind commit id is the first parent while other parents will at the end before each commit found their first parent
     */
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


    //https://www.leshenko.net/p/ugit/#fetch-remote-refs-objects

    public static void iteratorObjectsInTree(String objectId, Set<String> visited, Collection<String> collections){
        visited.add(objectId);
        collections.add(objectId);
        final List<ZitObject> zitObjects = ReadTree.iteratorTreeEntries(objectId);
        for (ZitObject zitObject : zitObjects) {
            if (!visited.contains(zitObject.getObjectId())) {
                if (ConstantVal.TREE.equals(zitObject.getType())) {
                    iteratorObjectsInTree(zitObject.getObjectId(), visited, collections);
                } else {
                    visited.add(zitObject.getObjectId());
                    collections.add(zitObject.getObjectId());
                }
            }
        }
    }

    //https://www.leshenko.net/p/ugit/#fetch-remote-refs-objects
    public static List<String> iteratorObjectsInCommits(List<String> objectIds) {
        List<String> result = new ArrayList<>();
        final Set<String> visited = new HashSet<>();

        final List<String> commitIds = iteratorCommitsAndParents(objectIds);
        for (String commitId : commitIds) {
            result.add(commitId);
            final CommitObject commit = Commit.getCommit(commitId);
            if (!visited.contains(commit.getTree())) {
                List<String> traverseTree = new ArrayList<>();
                iteratorObjectsInTree(commit.getTree(), visited, traverseTree);
                result.addAll(traverseTree);
            }
        }
        return result;
    }




    /**
     * @param objectId  object id
     * @return true if the object exist
     */
    public static boolean objectExists(String objectId) {
        return FileUtil.isFile(Paths.get(ConstantVal.OBJECTS_DIR).resolve(objectId).toString());
    }

    public static void fetchObjectIfMissing(String objectId, String remoteDir) {
        if (objectExists(objectId)) {
            return;
        }
        final Path remoteFile = Paths.get(remoteDir).resolve(ConstantVal.OBJECTS_DIR).resolve(objectId);
        final Path localFile = Paths.get(ConstantVal.OBJECTS_DIR).resolve(objectId);
        FileUtil.copy(remoteFile, localFile);
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
        final Path refsPath = Paths.get(ConstantVal.REFS_DIR_REAL);
        final Path refsDir = Paths.get(ConstantVal.REFS_DIR);
        final List<String> pathList;
        try {
            pathList = FileUtil.walk(refsPath, Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .map(path -> refsDir.resolve(refsPath.relativize(path)).toString())
                    .map(FileUtil::convertUnixPath).collect(Collectors.toList());
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

    /**
     * 1. if ref not exist, return refObject: {refName:ref, refValue: {falseSymbolic, null}}
     *  it means if the ref not exist, refName will just return the param ref
     * 2. dereference is an action symbol to determine
     * whether deference the chain to return the final hash id or just return the ref directly
     **/
    private static RefObject getRefInternal(String ref, boolean dereference) {
        String value = null;
        File file = new File(String.format("%s/%s", ConstantVal.ZIT_DIR, ref));
        // read the file first line
        if (file.exists()) {
            value = FileUtil.readFileFirstLine(file);
            if (value == null) {
                return null;
            }
        }
        // determine whether it is an direct hash or reference symbolic
        boolean symbolic = (value != null && value.startsWith("ref:"));
        // if a reference
        if (symbolic){
            // get the reference key like ··ref: ref/heads/main··
            // pick up the `ref/heads/main`
            value = value.split(":", 2)[1].trim();
            if (dereference) {
                return getRefInternal(value);
            }
        }
        // if a direct hash
        return new RefObject(ref, new RefValue(symbolic, value));
    }

    public static void updateRef(String ref, RefValue refValue) {
        updateRef(ref, refValue, true);
    }

    public static void updateRef(String ref, RefValue refValue, boolean dereference) {
        // pick up the origin ref value
        final String refName = Objects.requireNonNull(getRefInternal(ref, dereference)).getRefName();
        String value;
        if (refValue.getSymbolic()) {
            value = String.format("ref: %s", refValue.getValue());
        } else {
            value = refValue.getValue();
        }
        // update the origin ref with the new refValue
        FileUtil.createFile(value.getBytes(Charsets.UTF_8), String.format("%s/%s", ConstantVal.ZIT_DIR, refName));
    }

    public static void deleteRef(String ref) {
        deleteRef(ref, true);
    }

    public static void deleteRef(String ref, boolean dereference) {
        final RefObject refInternal = getRefInternal(ref, dereference);
        if (Objects.nonNull(refInternal)) {
            final String refName = refInternal.getRefName();
            FileUtil.deleteDir(String.format("%s/%s", ConstantVal.ZIT_DIR, refName));
        }
    }

    /**
     * determine whether ref not exist use deference false
     * but get id finally will return the direct hash
     */
    public static String getId(String refOrId) {
        if (ConstantVal.HEAD_ALIAS.equals(refOrId)) {
            refOrId = ConstantVal.HEAD;
        }
        for (String path : ConstantVal.REF_REGISTRY_DIRECTORIES) {
            final String refValue = String.format(path, refOrId);

            // pay attention, check use dereference false
            final RefValue ref = ZitContext.getRef(refValue, false);
            if (Objects.nonNull(ref.getValue())) {
                // while pick up dereference  as true
                return ZitContext.getRef(refValue, true).getValue();
            }
        }
        if (RegexConstantVal.ALL_HEX.matcher(refOrId).find()) {
            return refOrId;
        }
        return null;
    }

    /**
     * get current branch name by read the head pointer ref
     */
    public static String getBranchName(){
        final RefValue head = getRef(ConstantVal.HEAD, false);
        if (!head.getSymbolic()) {
            return null;
        }
        final String headPath = head.getValue();
        if (headPath.startsWith(ConstantVal.HEADS_PATH)) {
            return Paths.get(ConstantVal.HEADS_PATH).relativize(Paths.get(headPath)).toString();
        }
        return null;
    }

    public static void init() {
        initRoot();
        initIndex();
        initObjects();
    }

    private static void initIndex() {
        FileUtil.createFile("{}".getBytes(StandardCharsets.UTF_8), ConstantVal.INDEX);
    }

    private static void initRoot() {
        FileUtil.mkdir(ConstantVal.ZIT_DIR);
    }

    private static void initObjects() {
        FileUtil.mkdir(ConstantVal.OBJECTS_DIR);
    }

    public static byte[] getObject(String hash) {
        return getObject(hash, ConstantVal.BLOB);
    }

    public static byte[] getObject(String hash, String type) {
        String path = ConstantVal.OBJECTS_DIR + "/" + hash;
        log.debug("get the content of {} file", path);
        try {
            return FileUtil.getFileByteSource(path, type).read();
        } catch (IOException e) {
            log.error(e.toString());
        }
        return null;
    }

    public static String getObjectAsString(String hash, String type) {
        String path = ConstantVal.OBJECTS_DIR + "/" + hash;
        log.debug("get the content of {} file", path);
        return FileUtil.getFileAsString(path, type);
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
                        path.startsWith(ConstantVal.ZIT_DIR)
                                || path.startsWith(".zit")
                                || path.startsWith("doc")
                                || path.startsWith("target")

                );
    }

    public static void pushObject(String objectId, String remotePath) {
        remotePath += "/.zit";
        FileUtil.copy(String.format(ConstantVal.OBJECTS_DIR + "/%s", objectId), String.format("%s/objects/%s", remotePath, objectId));
    }

}
