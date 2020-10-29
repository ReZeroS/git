package club.qqtim.util;

import club.qqtim.common.ConstantVal;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.primitives.Chars;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author lijie78
 */
@Slf4j
public final class FileUtil {

    private FileUtil() {
    }


    public static void mkdir(String dirName) {
        boolean mkdir = new File(dirName).mkdir();
        if (mkdir) {
            log.info("Init empty .zit repository in {}{}{}", FileUtil.getCurrentDir(), "\\", dirName);
        } else {
            log.info("Create directory failed, please check your access right.");
        }
    }

    public static String getCurrentDir() {
        File file = new File(".");
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            log.error("can not get the current dir, please check your access right");
        }
        return null;
    }

    public static void createFile(byte[] fileContents, String fileName) throws IOException {
        File hashObject = new File(fileName);
        Files.write(fileContents, hashObject);
    }


    public static String getFileAsString(String path, String type) throws IOException {
        char nullChar = 0;
        ByteSource byteSource = Files.asByteSource(new File(path));
        if (ConstantVal.NONE.equals(type)) {
            return byteSource.asCharSource(Charsets.UTF_8).read();
        }
        // todo: refactor the below code to extract a method like obj.partition in python
        byte[] fileWithHeader = byteSource.read();
        byte[] header = new byte[type.getBytes().length];
        byte[] nullBytes = new byte[Chars.toByteArray(nullChar).length];
        byte[] fileContent = new byte[fileWithHeader.length - header.length - nullBytes.length];
        ByteBuffer fileWithHeaderBuffer = ByteBuffer.wrap(fileWithHeader);
        fileWithHeaderBuffer.get(header, 0, header.length);
        fileWithHeaderBuffer.get(nullBytes, 0, nullBytes.length);
        fileWithHeaderBuffer.get(fileContent, 0, fileContent.length);

        return ByteSource.wrap(fileContent).asCharSource(Charsets.UTF_8).read();
    }
}
