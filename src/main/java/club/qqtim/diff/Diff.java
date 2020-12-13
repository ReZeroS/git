package club.qqtim.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @title: Diff
 * @Author lijie78
 * @Date: 2020/12/13
 * @Version 1.0.0
 */
public class Diff {


    public static String diffTrees(Map<String, String> treeFrom, Map<String, String> treeTo) {
        StringBuilder output = new StringBuilder();
        final Map<String, List<String>> pathObjectIds = compareTrees(treeFrom, treeTo);
        pathObjectIds.forEach((path, objectIds) -> {
            final String from = objectIds.get(0);
            final String to = objectIds.get(1);
            if (!Objects.equals(from, to)) {
                output.append(String.format("changed: %s\n", path));
            }
        });
        return output.toString();
    }

    /**
     * given trees, return object ids
     * @param trees compare trees
     * @return key path, val objectIds
     */
    public static Map<String, List<String>> compareTrees(Map<String, String> ...trees) {
        Map<String, List<String>> entries = new HashMap<>(1);
        for (int i = 0; i < trees.length; i++) {
            for (Map.Entry<String, String> entry : trees[i].entrySet()) {
                String path = entry.getKey();
                String objectId = entry.getValue();
                entries.putIfAbsent(path, new ArrayList<>(Collections.nCopies(trees.length, null)));
                entries.get(path).set(i, objectId);
            }
        }
        return entries;
    }
}
