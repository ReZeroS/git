package club.qqtim.diff;

import club.qqtim.algorithm.MyersDiff;
import club.qqtim.algorithm.PathNode;
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
 * @Author lijie78
 * @Date: 2020/12/13
 * @Version 1.0.0
 */
@Slf4j
public class Diff {


    public static String diffTrees(Map<String, String> treeFrom, Map<String, String> treeTo) {
        StringBuilder output = new StringBuilder();
        final Map<String, List<String>> pathObjectIds = compareTrees(treeFrom, treeTo);
        pathObjectIds.forEach((path, objectIds) -> {
            final String from = objectIds.get(0);
            final String to = objectIds.get(1);
            if (!Objects.equals(from, to)) {
                output.append(String.format("\nchange file: %s\n", path));
                output.append(diffBlobs(from, to));
            }
        });
        return output.toString();
    }

    /**
     * https://qqtim.club/2020/06/14/git-myers-diff/
     * https://blog.robertelder.org/diff-algorithm/
     * @param from from blob id
     * @param to     to blob id
     * @return output
     */
    private static String diffBlobs(String from, String to) {
        final List<LineObject> fromLineObjects = convertObjectContentToLines(from);
        final List<LineObject> toLineObjects = convertObjectContentToLines(to);

        MyersDiff<LineObject> myersDiff = new MyersDiff<>();
        try {
            PathNode pathNode = myersDiff.buildPath(fromLineObjects, toLineObjects);
//            log.info(String.valueOf(pathNode));
            final List<String> diffResult = myersDiff.buildDiff(pathNode, fromLineObjects, toLineObjects);
            return String.join(System.lineSeparator(), diffResult);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return ConstantVal.EMPTY;
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
            for (int k = -(d - 2*Integer.max(0, d - m)); k < (d - 2*Integer.max(0, d - n)); ++k) {
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
