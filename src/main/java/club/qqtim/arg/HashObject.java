package club.qqtim.arg;

import club.qqtim.util.FileUtil;
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
 * @author lijie78
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
    public String call() throws Exception {
        byte[] fileContents = Files.toByteArray(file);
        return hashObject(fileContents, this.type);
    }

    protected String hashObject(byte[] fileContents, String type) throws NoSuchAlgorithmException, IOException {
        char nullChar = 0;
        byte[] targetFileContents = Bytes.concat(type.getBytes(), Chars.toByteArray(nullChar), fileContents);
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(targetFileContents);

        // convert digest bytes to hex string
        String objectId = new BigInteger(1, digest).toString(16);
        log.info("objectId is {}", objectId);
        // create file with file name as object id
        FileUtil.createFile(targetFileContents, club.qqtim.data.Data.OBJECTS + "/" + objectId);
        return objectId;
    }


}
