package club.qqtim.data;

import club.qqtim.common.ConstantVal;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @title: DataTest
 * @Author rezeros.github.io
 * @Date: 2020/12/8
 * @Version 1.0.0
 */

public class DataTest {



    @Test
    public void testIteratorRefs() throws IOException {
        final Path basePath = Paths.get(".");
        final Path objectPath = Paths.get(".zit/objects");
        final Path refsDir = Paths.get("refs");
        final List<String> pathList =  Files.walk(basePath, Integer.MAX_VALUE)
                .filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
        pathList.forEach(System.out::println);
    }
}
