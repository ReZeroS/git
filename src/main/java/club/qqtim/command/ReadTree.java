package club.qqtim.command;


import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.ZitObject;
import club.qqtim.util.FileUtil;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static club.qqtim.util.FileUtil.emptyCurrentDir;

/**
 * @author rezeros.github.io
 * extract the tree， opposite to write-tree
 */
@Data
@Slf4j
@CommandLine.Command(name = "read-tree")
public class ReadTree implements Runnable {

    /**
     * tree id
     */
    @CommandLine.Parameters(index = "0")
    private String hash;

    @Override
    public void run() {
        readTree(hash);
    }


    public static void readTree(String id){
        readTree(id, false);
    }


    /**
     * updateWorking means whether reset the working area to the index description
     */
    public static void readTree(String id, boolean updateWorking){

        Map<String, String> tree = getTree(id);

        String fileContent = new Gson().toJson(tree);
        FileUtil.createFile(fileContent, ConstantVal.INDEX);

        if (updateWorking) {
            checkoutIndex(tree);
        }

    }



    public static List<ZitObject> iteratorTreeEntries(String treeId) {
        final String tree = ZitContext.getObjectAsString(treeId, "tree");
        assert tree != null;
        return Arrays.stream(tree.split(ConstantVal.NEW_LINE))
                .map(object -> object.split(ConstantVal.SINGLE_SPACE))
                .map(objectFields -> new ZitObject(objectFields[0], objectFields[1], objectFields[2]))
                .collect(Collectors.toList());
    }

    /**
     * only one layer
     * [
     *    {key: path, val: object id},
     *    {...}
     * ]
     */
    public static Map<String, String> getTree(String treeId) {
        return getTree(treeId, ConstantVal.EMPTY);
    }

    private static Map<String, String> getTree(String treeId, String basePath) {
        if (Objects.isNull(treeId)) {
            return Collections.emptyMap();
        }
        final List<ZitObject> zitObjects = iteratorTreeEntries(treeId);
        Map<String, String> map = new HashMap<>(16);
        zitObjects.forEach(zitObject -> {
            String path = basePath + zitObject.getName();
            if (ConstantVal.BLOB.equals(zitObject.getType())) {
                map.put(path, zitObject.getObjectId());
            } else if (ConstantVal.TREE.equals(zitObject.getType())) {
                final Map<String, String> tree = getTree(zitObject.getObjectId(), String.format("%s/", path));
                map.putAll(tree);
            }
        });
        return map;
    }




    /**
     *  index must only have one layer
     */
    public static void checkoutIndex(Map<String, String> index) {
        // clean current dir
        emptyCurrentDir();

        for (Map.Entry<String, String> pathObjectId : index.entrySet()) {
            String path = pathObjectId.getKey();
            String objectId = pathObjectId.getValue();
            FileUtil.createParentDirs(path);
            final byte[] objectBytes = ZitContext.getObject(objectId);
            FileUtil.createFile(objectBytes, path);
        }

    }


}
