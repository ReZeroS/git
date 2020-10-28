package club.qqtim.data;

import club.qqtim.util.FileUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Data {

    private static final String ZIT_DIR = ".zit";

    public void init(){
        boolean mkdir = FileUtil.mkdir(ZIT_DIR);
        if (mkdir) {
            log.info("Init empty .zit repository in {}{}{}", FileUtil.getCurrentDir(), "\\", ZIT_DIR);
        } else {
            log.info("Create directory failed, please check your access right.");
        }
    }


}
