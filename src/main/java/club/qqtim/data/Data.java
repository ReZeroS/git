package club.qqtim.data;

import club.qqtim.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class Data {

    public static final String ZIT_DIR = ".zit";

    public static final String OBJECTS = ZIT_DIR + "/objects";

    public void init(){
        initRoot();
        initObjects();
    }

    private void initRoot() {
        FileUtil.mkdir(ZIT_DIR);
    }

    private void initObjects() {
        FileUtil.mkdir(OBJECTS);
    }

    public String getObject(String hash, String type) throws IOException {
        String path = OBJECTS + "/" + hash;
        log.info("get the content of {} file", path);
        return FileUtil.getFileAsString(path, type);
    }

}
