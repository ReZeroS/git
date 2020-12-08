package club.qqtim.data;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @title: DataTest
 * @Author lijie78
 * @Date: 2020/12/8
 * @Version 1.0.0
 */

public class DataTest {



    @Test
    public void testIteratorRefs() throws IOException {
        final Path objectPath = Paths.get(".zit/objects");
        final Path refsDir = Paths.get("refs");
        final List<String> pathList = Files.walk(objectPath, Integer.MAX_VALUE)
                .filter(Files::isRegularFile)
                .map(path -> refsDir.resolve(objectPath.relativize(path)).toString()).collect(Collectors.toList());
        pathList.forEach(System.out::println);
    }
}
