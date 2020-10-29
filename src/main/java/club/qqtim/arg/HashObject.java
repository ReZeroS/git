package club.qqtim.arg;

import club.qqtim.util.FileUtil;
import com.google.common.io.Files;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
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

    public String call() throws Exception {
        byte[] fileContents = Files.toByteArray(file);
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(fileContents);

        // convert digest bytes to hex string
        String objectId = new BigInteger(1, digest).toString(16);
        log.info("objectId is {}", objectId);

        // create file with file name as object id
        FileUtil.createFile(fileContents, club.qqtim.data.Data.OBJECTS + "/" + objectId);
        return objectId;
    }


}
