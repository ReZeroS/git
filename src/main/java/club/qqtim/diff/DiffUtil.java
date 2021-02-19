package club.qqtim.diff;

import club.qqtim.command.HashObject;
import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.diff.algorithm.Snake;
import club.qqtim.diff.algorithm.SnakePoint;
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
import java.util.Stack;
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
        try {
            return diff(fromLineObjects, toLineObjects);
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
     *
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
     * @param fromBlob from blob id
     * @param toBlob   to blob id
     * @return merged blob content
     */
    private static String mergeBlobs(String baseTree, String fromBlob, String toBlob) {

        //todo Three-way merge
        final List<LineObject> lineObjects = diffBlobs(fromBlob, toBlob);
        return lineObjects.stream().map(LineObject::getLineContent).collect(Collectors.joining(System.lineSeparator()));
    }




    /**
     * complexity of both time and space : O ((N + M)D)
     * k = x - y
     * 0 - 1 - 2 - 3 // x
     * |
     * 1
     * |
     * 2  // y
     */
    public static List<LineObject> diff(List<LineObject> fromLineObjects, List<LineObject> targetLineObjects) {

        // init step
        int finalStep = 0;
        // we set from as x anxious
        final int fromLineCount = fromLineObjects.size();

        // we set target as y anxious
        final int targetLineCount = targetLineObjects.size();

        // sum of from and target lines count
        final int totalLineCount = targetLineCount + fromLineCount;

        int vSize = Math.max(fromLineCount, targetLineCount) * 2 + 1;

        // do snapshot for v while iterate step
        int [][] vList = new int[totalLineCount + 1][vSize];

        // k can be zero, so plus one
        //todo optimize for minimize v.length
        final int[] v = new int[vSize];

        // set the previous start point
        v[v.length / 2 + 1] = 0;

        boolean foundShortest = false;
        for (int step = 0; step <= totalLineCount; step++) {

            // little trick, java can not use negative number as array index
            int negativeStep = v.length / 2 - step;
            int positiveStep = v.length / 2 + step;
            for (int k = negativeStep; k >= 0 && k <= positiveStep; k += 2) {
                int kAimD = k - v.length / 2;
                boolean down = (kAimD == -step || (kAimD != step && v[k - 1] < v[k + 1]));

                int xStart = down? v[k + 1]: v[k - 1];

                int xEnd = down? xStart: xStart + 1;
                int yEnd = xEnd - kAimD;
                // diagonal
                while ((0 <= xEnd && xEnd < fromLineCount) && (0 <= yEnd && yEnd < targetLineCount)
                        && (fromLineObjects.get(xEnd).getLineContent().equals(targetLineObjects.get(yEnd).getLineContent()))){
                    xEnd++; yEnd++;
                }
                v[k] = xEnd;
                if (xEnd >= fromLineCount && yEnd >= targetLineCount) {
                    foundShortest = true;
                }
            }
            // do snapshot for v
            vList[step] = Arrays.copyOf(v, v.length);
            if (foundShortest) {
                finalStep = step;
                break;
            }
        }
        List<LineObject> result = new ArrayList<>();

        if (foundShortest) {
            Stack<Snake> snakeStack = generateSnakes(fromLineCount, targetLineCount, vList, finalStep);

            // the final step, let's rock
            SnakePoint realStartPoint = new SnakePoint(0, 0);
            while(!snakeStack.empty()) {
                final Snake snake = snakeStack.pop();
                final SnakePoint start = snake.getStart();
                final SnakePoint middle = snake.getMiddle();
                final SnakePoint end = snake.getEnd();

                result.addAll(compareSnakePoint(realStartPoint, start, fromLineObjects, targetLineObjects));
                result.addAll(compareSnakePoint(start, middle, fromLineObjects, targetLineObjects));
                result.addAll(compareSnakePoint(middle, end, fromLineObjects, targetLineObjects));

                realStartPoint = end;
            }

            return result;
        }

        fromLineObjects.forEach(line -> line.setAction(ConstantVal.MINUS));
        targetLineObjects.forEach(line -> line.setAction(ConstantVal.PLUS));
        result.addAll(fromLineObjects);
        result.addAll(targetLineObjects);
        return result;
    }


    /**
     * let's do backtrack to generate the shortest path
     * now vList has total record we need: every step the (k, x) val
     *
     *    start(K-1) -- mid(K)
     *                         \
     *                          \
     *                           end
     *
     *
     */
    private static Stack<Snake> generateSnakes(int fromLineCount, int targetLineCount, int[][] vList, int finalStep) {

        Stack<Snake> snakeStack = new Stack<>();
        int fromEndX = fromLineCount;
        int targetEndY = targetLineCount;
        // step >= 0 or (fromEndX > 0  && targetEndY> 0)
        for (int step = finalStep; fromEndX > 0  && targetEndY > 0; step--) {
            final int[] v = vList[step];

            int negativeStep = v.length / 2 - step;
            int positiveStep = v.length / 2 + step;

            int k = fromEndX - targetEndY;
            int kIndex = v.length / 2 + k;

            // set current k as end point
            int xEnd = v[kIndex];
            int yEnd = xEnd - k;

            boolean down = (k == negativeStep || (k != positiveStep && v[kIndex - 1] < v[kIndex + 1]));

            int xStart = v[down? kIndex + 1: kIndex - 1];
            int yStart = xStart - (down? k + 1: k -1);

            int xMid = down? xStart: xStart + 1;
            int yMid = xMid - k;

            final Snake snake = new Snake();
            snake.setStart(new SnakePoint(xStart, yStart));
            snake.setMiddle(new SnakePoint(xMid, yMid));
            snake.setEnd(new SnakePoint(xEnd, yEnd));
            snakeStack.push(snake);

            fromEndX = xStart;
            targetEndY = yStart;
        }
        return snakeStack;
    }

    public static List<LineObject> compareSnakePoint
            (SnakePoint from, SnakePoint end, List<LineObject> fromLineObjects, List<LineObject> targetLineObjects) {
        // mid equals end
        if (from.equals(end)) {
            return Collections.emptyList();
        }

        List<LineObject> result = new ArrayList<>();
        // mid to end
        if (!from.getX().equals(end.getX()) && !from.getY().equals(end.getY())) {
            for (int fromX = from.getX(); fromX < end.getX(); fromX++) {
                final LineObject lineObject = fromLineObjects.get(fromX);
                lineObject.setAction(ConstantVal.SYNC);
                result.add(lineObject);
            }
        } else {
            // start to mid
            if (!from.getX().equals(end.getX())) {
                final LineObject lineObject = fromLineObjects.get(from.getX());
                lineObject.setAction(ConstantVal.MINUS);
                result.add(lineObject);
            } else if (!from.getY().equals(end.getY())) {
                final LineObject lineObject = targetLineObjects.get(from.getY());
                lineObject.setAction(ConstantVal.PLUS);
                result.add(lineObject);
            }
        }
        return result;
    }


    /**
     * test method
     */
    public static void main(String[] args) {
        String a = "A-B-C-A-B-B-A";
        String b = "C-B-A-B-A-C";

        final String[] aArray = a.split("-");
        final String[] bArray = b.split("-");
        List<LineObject> fromLineObjects = new ArrayList<>();
        List<LineObject> targetLineObjects = new ArrayList<>();
        for (int i = 0; i < aArray.length; i++) {
            int index = i + 1;
            final LineObject lineObject = new LineObject();
            lineObject.setIndex(index);
            lineObject.setLineContent(aArray[i]);
            fromLineObjects.add(lineObject);
        }
        for (int i = 0; i < bArray.length; i++) {
            int index = i + 1;
            final LineObject lineObject = new LineObject();
            lineObject.setIndex(index);
            lineObject.setLineContent(bArray[i]);
            targetLineObjects.add(lineObject);
        }
        final List<LineObject> diff = diff(fromLineObjects, targetLineObjects);
        diff.forEach(line -> log.debug(line.toString()));
    }


}
