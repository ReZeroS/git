package club.qqtim.converter;

import club.qqtim.context.ZitContext;
import picocli.CommandLine;

/**
 * @title: IdConverter
 * @Author rezeros.github.io
 * @Date: 2020/12/12
 * @Version 1.0.0
 */
public class IdConverter implements CommandLine.ITypeConverter<String> {

    @Override
    public String convert(String id) {
        return ZitContext.getId(id);
    }
}
