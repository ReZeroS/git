package club.qqtim.diff.algorithm;

import club.qqtim.common.ConstantVal;
import club.qqtim.diff.DiffUtil;
import club.qqtim.diff.LineObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * @title: MyersDiff
 * @Author rezeros.github.io
 * @Date: 2021/2/19
 * @Version 1.0.0
 */
@Slf4j
public class MyersDiff implements DiffAlgorithm {

    /**
     * complexity of both time and space : O ((N + M)D)
     * k = x - y
     * 0 - 1 - 2 - 3 // x
     * |
     * 1
     * |
     * 2  // y
     */
    @Override
    public List<LineObject> diff(List<LineObject> fromLineObjects, List<LineObject> targetLineObjects) {

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
                    break;
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
        for (int step = finalStep; step >= 0; step--) {
            final int[] v = vList[step];

            int negativeStep = v.length / 2 - step;
            int positiveStep = v.length / 2 + step;

            int k = fromEndX - targetEndY;
            int kIndex = v.length / 2 + k;

            // set current k as end point
            int xEnd = v[kIndex];
            int yEnd = xEnd - k;

            boolean down = (kIndex == negativeStep || (kIndex != positiveStep && v[kIndex - 1] < v[kIndex + 1]));

            int xStart = v[down? kIndex + 1: kIndex - 1];
            int yStart = xStart - (down? k + 1: k -1);

            int xMid = down? xStart: xStart + 1;
            int yMid = xMid - k;

            fromEndX = xStart;
            targetEndY = yStart;
            if (fromEndX < 0 || targetEndY < 0) {
                break;
            }
            final Snake snake = new Snake();
            snake.setStart(new SnakePoint(xStart, yStart));
            snake.setMiddle(new SnakePoint(xMid, yMid));
            snake.setEnd(new SnakePoint(xEnd, yEnd));
            snakeStack.push(snake);

        }
        return snakeStack;
    }

    /**
     * compare start, mid and end point to generate result of diff lines
     * receive two points: from and end
     */
    public static List<LineObject> compareSnakePoint
            (SnakePoint from, SnakePoint end, List<LineObject> fromLineObjects, List<LineObject> targetLineObjects) {
        // mid equals end
        if (from.equals(end)) {
            return Collections.emptyList();
        }

        List<LineObject> result = new ArrayList<>();
        // mid to end
        if (!from.getX().equals(end.getX()) && !from.getY().equals(end.getY())) {
            for (int fromX = from.getX(), fromY = from.getY(); fromX < end.getX() && fromY < end.getY(); fromX++, fromY++) {
                final LineObject lineObject = fromLineObjects.get(fromX);
                final LineObject anotherLine = targetLineObjects.get(fromY);
                lineObject.setAction(ConstantVal.SYNC);
                lineObject.setAnotherIndex(anotherLine.getIndex());
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
        String a = "celery-garlic-onions-salmon-tomatoes-wine";
        String b = "celery-salmon-tomatoes-garlic-onions-wine";

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
//        final List<LineObject> diff = new MyersDiff().diff(fromLineObjects, targetLineObjects);
        DiffUtil.mergeBlobs(fromLineObjects, targetLineObjects, null);
//        diff.forEach(line -> log.debug(line.toString()));
    }

}
