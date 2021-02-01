package club.qqtim.diff;

import club.qqtim.diff.algorithm.MyersDiff;
import club.qqtim.diff.algorithm.PathNode;
import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @title: Diff
 * @Author rezeros.github.io
 * @Date: 2020/12/13
 * @Version 1.0.0
 */
@Slf4j
public class DiffUtil {


    public static List<SimplyChange> iteratorChangedFiles(Map<String, String> fromTree, Map<String, String> toTree) {
        final Map<String, List<String>> pathObjectIds = compareTrees(fromTree, toTree);
        return pathObjectIds.entrySet().stream().map(entry -> {
            final String path = entry.getKey();
            final List<String> objectIds = entry.getValue();
            final String fromObject = objectIds.get(0);
            final String toObject = objectIds.get(1);
            if (!Objects.equals(fromObject, toObject)) {
                final SimplyChange simplyChange = new SimplyChange();
                simplyChange.setPath(path);
                simplyChange.setAction(fromObject == null ? "new file" : toObject == null ? "delete file" : "modify file");
                return simplyChange;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static String diffTrees(Map<String, String> treeFrom, Map<String, String> treeTo) {
        StringBuilder output = new StringBuilder();
        final Map<String, List<String>> pathObjectIds = compareTrees(treeFrom, treeTo);
        pathObjectIds.forEach((path, objectIds) -> {
            final String from = objectIds.get(0);
            final String to = objectIds.get(1);
            if (!Objects.equals(from, to)) {
                output.append(String.format("\nchange file: %s\n", path));
                final List<LineObject> lineObjects = diffBlobs(from, to);
                final String diffResult = lineObjects.stream().map(lineObject -> String.format("%s %s %s",
                        ConstantVal.SYNC.equals(lineObject.getAction()) ? " "
                                : ConstantVal.PLUS.equals(lineObject.getAction()) ? "+" : "-",
                        lineObject.getIndex(), lineObject.getLineContent())).collect(Collectors.joining(System.lineSeparator()));
                output.append(diffResult);
            }
        });
        return output.toString();
    }

    /**
     * https://qqtim.club/2020/06/14/git-myers-diff/
     * https://blog.robertelder.org/diff-algorithm/
     *
     * @param from from blob id
     * @param to   to blob id
     * @return output
     */
    private static List<LineObject> diffBlobs(String from, String to) {
        final List<LineObject> fromLineObjects = convertObjectContentToLines(from);
        final List<LineObject> toLineObjects = convertObjectContentToLines(to);

        MyersDiff<LineObject> myersDiff = new MyersDiff<>();
        try {
            PathNode pathNode = myersDiff.buildPath(fromLineObjects, toLineObjects);
//            log.info(String.valueOf(pathNode));
            return myersDiff.buildDiff(pathNode, fromLineObjects, toLineObjects);
        } catch (Exception e) {
            log.error(e.toString());
        }

        return Collections.emptyList();
    }

    private static int findShortestEditCount(List<LineObject> fromLineObjects, List<LineObject> toLineObjects) {
        final int n = fromLineObjects.size();
        final int m = toLineObjects.size();
        int max = n + m;

//        final int[] v = new int[max * 2 + 2];
        final int[] v = new int[max + 2];
        v[1] = 0;
        for (int d = 0; d < max + 1; d++) {
            // for k in range(-D, D + 1, 2):
            // after liner space optimize
            for (int k = -(d - 2 * Integer.max(0, d - m)); k < (d - 2 * Integer.max(0, d - n)); ++k) {
                int x;
                final int kIndex = convertIndex(k, v.length);
                final int kPlusIndex = convertIndex(k + 1, v.length);
                final int kMinusIndex = convertIndex(k - 1, v.length);
                if (k == -d || k != d && v[kMinusIndex] < v[kPlusIndex]) {
                    x = v[kPlusIndex];
                } else {
                    x = v[kMinusIndex];
                }
                int y = x - k;
                while (x < n && y < m && Objects.equals(fromLineObjects.get(x), toLineObjects.get(y))) {
                    x++;
                    y++;
                }
                v[kIndex] = x;
                if (x >= n && y >= m) {
                    return d;
                }
            }
        }
        return -1;
    }

    private static int convertIndex(int k, int length) {
        if (k < 0) {
            return length - k;
        }
        return k;

    }

    private static List<LineObject> convertObjectContentToLines(String objectId) {
        if (Objects.isNull(objectId)) {
            return Collections.emptyList();
        }
        final byte[] objectContentBytes = ZitContext.getObject(objectId);
        final String objectContent = new String(objectContentBytes, Charsets.UTF_8);
        final List<String> objectLines = Arrays.stream(objectContent.split(System.lineSeparator())).collect(Collectors.toList());
        return IntStream.range(0, objectLines.size())
                .mapToObj(i -> new LineObject(i, objectLines.get(i))).collect(Collectors.toList());
    }

    /**
     * given trees, return path, object ids
     *
     * @param trees compare trees
     * @return key path, val objectIds
     */
    @SafeVarargs
    public static Map<String, List<String>> compareTrees(Map<String, String>... trees) {
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

    /**
     * key: path val: blob content
     *
     * @param headTree source tree
     * @param otherTree   target tree
     * @return merged tree
     */
    public static Map<String, String> mergeTrees(Map<String, String> baseTree, Map<String, String> headTree, Map<String, String> otherTree) {
        final Map<String, List<String>> comparedTrees = compareTrees(baseTree, headTree, otherTree);
        Map<String, String> mergedTrees = new HashMap<>();
        comparedTrees.forEach((path, trees) -> {
            final String mergeBlobs = mergeBlobs(trees.get(0), trees.get(1), trees.get(2));
            mergedTrees.put(path, mergeBlobs);
        });
        return mergedTrees;
    }

    /**
     * @param fromBlob from blob id
     * @param toBlob   to blob id
     * @return merged blob content
     */
    private static String mergeBlobs(String baseTree, String fromBlob, String toBlob) {

        //todo Three-way merge
        final List<LineObject> lineObjects = diffBlobs(fromBlob, toBlob);
        return lineObjects.stream().map(LineObject::getLineContent).collect(Collectors.joining(System.lineSeparator()));
    }
}
