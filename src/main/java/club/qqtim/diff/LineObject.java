package club.qqtim.diff;

import club.qqtim.diff.algorithm.Equalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @title: LineObject
 * @Author rezeros.github.io
 * @Date: 2020/12/16
 * @Version 1.0.0
 */
@Data
@NoArgsConstructor
public class LineObject implements Equalizer<LineObject> {

    /**
     * sync, plus, minus
     */
    private String action;

    /**
     * row number, default from zero
     */
    private Integer index;

    /**
     * can not be null
     * line content which contains the whole content of the current line
     */
    private String lineContent;

    public LineObject(Integer index, String lineContent) {
        this.index = index;
        this.lineContent = lineContent;
    }

    @Override
    public String toString() {
        return index + " " + lineContent;
    }

    @Override
    public boolean sameLine(LineObject b) {
        return this.lineContent.equals(b.getLineContent());
    }
}
