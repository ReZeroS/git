package club.qqtim.data;

import com.google.common.collect.Comparators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @title: ZitObject
 * @Author rezeros.github.io
 * @Date: 2020/10/31
 * @Version 1.0.0
 */
@Data
@ToString
@AllArgsConstructor
public class ZitObject implements Comparable<ZitObject> {

    private String type;

    private String objectId;

    private String name;


    @Override
    public int compareTo(ZitObject o) {
        return Integer.compare(this.hashCode(), o.hashCode());
    }
}
