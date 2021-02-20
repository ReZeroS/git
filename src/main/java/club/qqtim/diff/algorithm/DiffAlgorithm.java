package club.qqtim.diff.algorithm;

import club.qqtim.diff.LineObject;

import java.util.List;

/**
 * @author rezeros.github.io
 */
public interface DiffAlgorithm {

    /**
     * convert from content to target content
     * @param fromLineObjects from content
     * @param targetLineObjects target content
     * @return  convert short edit script
     */
    List<LineObject> diff(List<LineObject> fromLineObjects, List<LineObject> targetLineObjects);

}
