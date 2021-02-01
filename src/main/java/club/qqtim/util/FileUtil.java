package club.qqtim.util;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.primitives.Chars;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author rezeros.github.io
 */
@Slf4j
public final class FileUtil {

    private FileUtil() {
    }

    private static final ThreadLocal<String> rootPathContext = new ThreadLocal<>();

    public static void setRootPathContext(String path) {
        rootPathContext.set(path);
    }
    public static void removeRootPathContext() {
        rootPathContext.remove();
    }

    public static void mkdir(String dirName) {
        boolean mkdir = new File(dirName).mkdir();
        if (mkdir) {
            log.info("Init {} directory in .zit repository", Paths.get(Objects.requireNonNull(FileUtil.getCurrentDir()), dirName));
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

    public static void deleteDir(String path){
        deleteDir(new File(path), null);
    }

    public static void deleteDir(String path, Predicate<String> ignorePredicate){
        deleteDir(new File(path), ignorePredicate);
    }

    public static void deleteDir(File file, Predicate<String> ignorePredicate){
        if (ignorePredicate != null) {
            final boolean ignorePath = ignorePredicate.test(file.getPath());
            if (ignorePath) {
                return;
            }
        }
        if (file.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .forEach(currentFile -> deleteDir(currentFile, ignorePredicate));
        }
        final boolean delete = file.delete();
        if (!delete) {
            log.error("delete file failed, please check your access right.");
        }
    }

    public static void createFile(byte[] fileContents, String fileName) {
        final String rootPath = rootPathContext.get();
        if (!Objects.isNull(rootPath)) {
            fileName = Paths.get(rootPath).resolve(fileName).toString();
        }
        File hashObject = new File(fileName);
        try {
            // first create the parent directory
            Files.createParentDirs(hashObject);
            // then create the file
            Files.write(fileContents, hashObject);
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    public static void createParentDirs(String path) {
        try {
            Files.createParentDirs(new File(path));
        } catch (IOException e) {
            log.error(e.toString());
        }
    }


    public static String getFileAsString(String path, String type) throws IOException {
        return getFileByteSource(path, type).asCharSource(Charsets.UTF_8).read();
    }

    public static ByteSource getFileByteSource(String path, String type) throws IOException {
        char nullChar = 0;
        ByteSource byteSource = Files.asByteSource(new File(path));
        if (ConstantVal.NONE.equals(type)) {
            return byteSource;
        }
        // todo: refactor the below code to extract a method like obj.partition in python
        byte[] fileWithHeader = byteSource.read();
        byte[] header = new byte[type.getBytes(Charsets.UTF_8).length];
        byte[] nullBytes = new byte[Chars.toByteArray(nullChar).length];
        byte[] fileContent = new byte[fileWithHeader.length - header.length - nullBytes.length];
        ByteBuffer fileWithHeaderBuffer = ByteBuffer.wrap(fileWithHeader);
        fileWithHeaderBuffer.get(header, 0, header.length);
        log.debug("current object type is {}:", new String(header, Charsets.UTF_8));
        fileWithHeaderBuffer.get(nullBytes, 0, nullBytes.length);
        fileWithHeaderBuffer.get(fileContent, 0, fileContent.length);
        return ByteSource.wrap(fileContent);
    }



    public static void emptyCurrentDir() {
        File file = new File(ConstantVal.BASE_PATH);
        final String[] paths = file.list();
        if (paths != null) {
            Arrays.stream(paths).forEach(path -> {
                if (ZitContext.isNotIgnored(path)) {
                    FileUtil.deleteDir(path);
                }
            });
        }
    }


    public static boolean isFile(String path) {
        final String rootPath = rootPathContext.get();
        if (!Objects.isNull(rootPath)) {
            String fileName = Paths.get(rootPath).resolve(path).toString();
            File file = new File(fileName);
            return file.isFile();
        }
        return new File(path).isFile();
    }



    public static void copy(String from, String to) {
        try {
            java.nio.file.Files.copy(Paths.get(from), Paths.get(to));
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

}
