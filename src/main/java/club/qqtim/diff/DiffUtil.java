package club.qqtim.diff;

import club.qqtim.command.HashObject;
import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.diff.algorithm.MyersDiff;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
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
                if (from == null) {
                    output.append(String.format("\ncreated file: %s\n", path));
                    final List<LineObject> lineObjects = convertObjectContentToLines(to);
                    lineObjects.forEach(lineObject -> lineObject.setAction(ConstantVal.PLUS));
                    final String diffResult = lineObjects.stream().map(LineObject::toString).collect(Collectors.joining(System.lineSeparator()));
                    output.append(diffResult);
                } else if (to == null) {
                    output.append(String.format("\ndeleted file: %s\n", path));
                    final List<LineObject> lineObjects = convertObjectContentToLines(from);
                    lineObjects.forEach(lineObject -> lineObject.setAction(ConstantVal.MINUS));
                    final String diffResult = lineObjects.stream().map(LineObject::toString).collect(Collectors.joining(System.lineSeparator()));
                    output.append(diffResult);
                } else {
                    output.append(String.format("\nchange file: %s\n", path));
                    final List<LineObject> lineObjects = diffBlobs(from, to);
                    final String diffResult = lineObjects.stream().map(LineObject::toString).collect(Collectors.joining(System.lineSeparator()));
                    output.append(diffResult);
                }
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
     */
    private static List<LineObject> diffBlobs(String from, String to) {
        final List<LineObject> fromLineObjects = convertObjectContentToLines(from);
        final List<LineObject> toLineObjects = convertObjectContentToLines(to);
        return diffBlobs(fromLineObjects, toLineObjects);
    }

    private static List<LineObject> diffBlobs(List<LineObject> fromLineObjects, List<LineObject> toLineObjects) {
        try {
            // strategy pattern, default is myers diff
            return new MyersDiff().diff(fromLineObjects, toLineObjects);
        } catch (Exception e) {
            e.printStackTrace();
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
                if (k == -d || (k != d && v[kMinusIndex] < v[kPlusIndex])) {
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
                .mapToObj(i -> new LineObject(i + 1, objectLines.get(i))).collect(Collectors.toList());
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
     * Three-way merge
     * @param headTree source tree
     * @param otherTree   target tree
     * @return merged tree
     */
    public static Map<String, String> mergeTrees(Map<String, String> baseTree, Map<String, String> headTree, Map<String, String> otherTree) {
        final Map<String, List<String>> comparedTrees = compareTrees(baseTree, headTree, otherTree);
        Map<String, String> mergedTrees = new HashMap<>();
        comparedTrees.forEach((path, trees) -> {
            String mergeBlobs = mergeBlobs(trees.get(0), trees.get(1), trees.get(2));
            final String objectId = HashObject.hashObject(mergeBlobs.getBytes(StandardCharsets.UTF_8));
            mergedTrees.put(path, objectId);
        });
        return mergedTrees;
    }

    /**
     * diff3
     * @param fromBlob from blob id
     * @param toBlob   to blob id
     * @return merged blob content
     * https://blog.jcoglan.com/2017/05/08/merging-with-diff3/
     */
    public static String mergeBlobs(String baseBlob, String fromBlob, String toBlob) {

        final List<LineObject> originalLines = convertObjectContentToLines(baseBlob);
        final List<LineObject> headLines = convertObjectContentToLines(fromBlob);
        final List<LineObject> otherLines = convertObjectContentToLines(toBlob);
        return mergeBlobs(originalLines, headLines, otherLines);
    }

    public static String mergeBlobs(List<LineObject> originalLines, List<LineObject> headLines, List<LineObject> otherLines) {

        List<LineObject> result = new ArrayList<>();

        // generate two match sets
        Map<Integer, Integer> matchBaseHead = diffBlobs(new ArrayList<>(originalLines), new ArrayList<>(headLines))
                .stream().filter(e -> ConstantVal.SYNC.equals(e.getAction()))
                .collect(Collectors.toMap(LineObject::getIndex, LineObject::getAnotherIndex));
        Map<Integer, Integer> matchBaseOther = diffBlobs(new ArrayList<>(originalLines), new ArrayList<>(otherLines))
                .stream().filter(e -> ConstantVal.SYNC.equals(e.getAction()))
                .collect(Collectors.toMap(LineObject::getIndex, LineObject::getAnotherIndex));

        // all start from zero
        int originLineNumber = 0, headLineNumber = 0, otherLineNumber = 0;

        for (;;) {
            int i = nextMisMatch(originLineNumber, headLineNumber,
                    otherLineNumber, headLines, otherLines, matchBaseHead, matchBaseOther);

            int origin = 0, head = 0, other = 0;

            if (i == 1) {
                final MergePoint mergePoint = nextMatch(originLineNumber, originalLines, matchBaseHead, matchBaseOther);
                origin = mergePoint.getOrigin();
                head = mergePoint.getHead();
                other = mergePoint.getOther();
            } else if (i > 1) {
                origin = originLineNumber + i;
                head = headLineNumber + i;
                other = otherLineNumber + i;
            }

            if (origin == 0 || head == 0 || other == 0) {
                break;
            }

            // chunk
            buildChunk(originLineNumber, headLineNumber, otherLineNumber,
                    origin - 1, head - 1, other - 1, originalLines, headLines, otherLines, result);

            originLineNumber = origin - 1;
            headLineNumber = head - 1;
            otherLineNumber = other - 1;
        }

        // build final chunk
        buildChunk(originLineNumber, headLineNumber, otherLineNumber,
                originalLines.size(), headLines.size(), otherLines.size(),
                originalLines, headLines, otherLines, result);

        return result.stream().map(LineObject::getLineContent).collect(Collectors.joining(System.lineSeparator()));
    }

    private static void buildChunk(int originLineNumber, int headLineNumber,
                                   int otherLineNumber, int origin, int head, int other,
                                   List<LineObject> originalLines, List<LineObject> headLines,
                                   List<LineObject> otherLines, List<LineObject> result) {
        final List<LineObject> originChunk = originalLines.subList(originLineNumber, origin);
        final List<LineObject> headChunk = headLines.subList(headLineNumber, head);
        final List<LineObject> otherChunk = otherLines.subList(otherLineNumber, other);

        if (headChunk.equals(otherChunk)) {
            result.addAll(originChunk);
        } else if (originChunk.equals(headChunk)) {
            result.addAll(originChunk);
        } else if (originChunk.equals(otherChunk)) {
            result.addAll(originChunk);
        } else {
            result.add(ConstantVal.MERGE_CONFLICT.get(ConstantVal.HEAD_CONFLICT));

            result.addAll(headChunk);

            result.add(ConstantVal.MERGE_CONFLICT.get(ConstantVal.ORIGIN_CONFLICT));

            result.addAll(otherChunk);

            result.add(ConstantVal.MERGE_CONFLICT.get(ConstantVal.OTHER_CONFLICT));
        }
    }


    private static int nextMisMatch(int indexOrigin, int indexHead, int indexOther,
                          List<LineObject> headLines, List<LineObject> otherLines,
                          Map<Integer, Integer> matchBaseHead, Map<Integer, Integer> matchBaseOther){
        for (int i = 1; i <= headLines.size() && i <= otherLines.size(); i++) {
            final Integer head = matchBaseHead.get(indexOrigin + i);
            final Integer other = matchBaseOther.get(indexOrigin + i);

            if (Objects.isNull(head) || !head.equals(indexHead + i)
                    || Objects.isNull(other) || !other.equals(indexOther + i)) {
                return i;
            }

        }
        return 0;
    }

    private static MergePoint nextMatch(int indexOrigin, List<LineObject> originLines,
                                        Map<Integer, Integer> matchBaseHead, Map<Integer, Integer> matchBaseOther) {
        for (int i = indexOrigin + 1; i <= originLines.size(); i++) {
            final Integer head = matchBaseHead.get(i);
            final Integer other = matchBaseOther.get(i);

            if (Objects.nonNull(head) && Objects.nonNull(other)) {
                return new MergePoint(i, head, other);
            }
        }
        return new MergePoint(0, 0, 0);

    }



    public static void main(String[] args) {
        String head = "4d2bdcbc2da4dbec446864512dc7903071b89f3d";
        String base = "4f90761edb43368f13e7691838bf1be1033ee579";
        String other = "5a1a58f54065a36616127a65516a53964c8630d0";
        mergeBlobs(base, head, other);
    }


    private static int findNextSyncLine(int index, List<LineObject> fromDiffBase) {
        for (int i = index; i < fromDiffBase.size(); i++) {
            if (ConstantVal.SYNC.equals(fromDiffBase.get(i).getAction())) {
                return i;
            }
        }
        // not found
        return -1;
    }


//
//    // compare and get two diffs
//    final List<LineObject> fromDiffBase = diffBlobs(baseBlob, fromBlob);
//    final List<LineObject> toDiffBase = diffBlobs(baseBlob, toBlob);
//
//    // split into different block
//        for (int i = 0, j = 0; i < fromDiffBase.size() && j < toDiffBase.size(); i++) {
//        int blockStartFrom = i, blockStartTo = j;
//        int blockEndFrom, blockEndTo;
//        // if not common sync
//        boolean commonSync = fromDiffBase.get(i).getIndex().equals(toDiffBase.get(j).getIndex())
//                && ConstantVal.SYNC.equals(fromDiffBase.get(i).getAction())
//                && ConstantVal.SYNC.equals(toDiffBase.get(j).getAction());
//
//        // diff block
//        if (!commonSync) {
//            // find their first common sync
//            // 1. find from first sync
//            i = findNextSyncLine(i, fromDiffBase);
//            // 2. find to first sync
//            j = findNextSyncLine(j, fromDiffBase);
//
//            // 3. sync from and to
//            while(i < fromDiffBase.size()
//                    && fromDiffBase.get(i).getIndex() < toDiffBase.get(j).getIndex()) {
//                i++;
//            }
//            while(j < toDiffBase.size()
//                    && toDiffBase.get(j).getIndex() < fromDiffBase.get(i).getIndex()) {
//                j++;
//            }
//
//            blockEndFrom = i;
//            blockEndTo = j;
//        } else {
//            // common block
//            while(i < fromDiffBase.size() && j < toDiffBase.size()
//                    && ConstantVal.SYNC.equals(fromDiffBase.get(i).getAction())
//                    && ConstantVal.SYNC.equals(toDiffBase.get(j).getAction())
//                    && toDiffBase.get(j).getIndex().equals(fromDiffBase.get(i).getIndex())) {
//                i ++; j++;
//            }
//
//            blockEndFrom = i;
//            blockEndTo = j;
//        }
//
//        // insert blocks: origin head other
//
//
//
//
//
//    }

}
