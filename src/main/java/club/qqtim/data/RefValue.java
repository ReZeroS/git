package club.qqtim.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @title: RefValue
 * @Author lijie78
 * @Date: 2020/12/10
 * @Version 1.0.0
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RefValue {

    /**
     * whether it's a symbolic or a direct ref.
     */
    private Boolean symbolic;

    private String value;
}
