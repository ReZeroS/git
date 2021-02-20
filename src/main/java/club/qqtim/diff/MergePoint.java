package club.qqtim.diff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @title: MergePoint
 * @Author lijie78
 * @Date: 2021/2/20
 * @Version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MergePoint {

    private Integer origin;

    private Integer head;

    private Integer other;

}
