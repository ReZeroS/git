package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.util.FileUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;


/**
 * @author rezeros.github.io
 */
@Data
@Slf4j
@CommandLine.Command(name = "hash-object")
public class HashObject implements Callable<String> {

    @CommandLine.Parameters(index = "0")
    private File file;

    @CommandLine.Parameters(index = "1", defaultValue = "blob")
    private String type;

    @Override
    public String call() {
        return doHashObject();
    }

    private String doHashObject() {
        byte[] fileContents = FileUtil.getFileAsBytes(file);
        return hashObject(fileContents, this.type);
    }

    public static String hashObject(byte[] fileContents) {
        return hashObject(fileContents, ConstantVal.BLOB);
    }

    /**
     * get hash of file content and create it with type describe.
     * type could be: blob, tree, commit
     */
    public static String hashObject(byte[] fileContents, String type) {
        byte[] targetFileContents = Bytes.concat(type.getBytes(Charsets.UTF_8), Chars.toByteArray(ConstantVal.NULL_CHAR), fileContents);
        byte[] digest = new byte[0];
        try {
            digest = MessageDigest.getInstance(ConstantVal.HASH_ALGORITHM).digest(targetFileContents);
        } catch (NoSuchAlgorithmException e) {
            log.error("no such algorithm ");
        }

        // convert digest bytes to hex string
        String objectId = new BigInteger(1, digest).toString(16);
        log.info("objectId with {} type is {}", type, objectId);
        // create file with file name as object id
        FileUtil.createFile(targetFileContents, ConstantVal.OBJECTS_DIR + "/" + objectId);
        return objectId;
    }



}
