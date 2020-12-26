package club.qqtim.diff;

import club.qqtim.algorithm.Equalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @title: LineObject
 * @Author lijie78
 * @Date: 2020/12/16
 * @Version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineObject implements Equalizer<LineObject> {

    /**
     * row number, default from zero
     */
    private Integer index;

    /**
     * can not be null
     * line content which contains the whole content of the current line
     */
    private String lineContent;

    @Override
    public String toString() {
        return index + " " + lineContent;
    }

    @Override
    public boolean sameLine(LineObject b) {
        return this.lineContent.equals(b.getLineContent());
    }
}
