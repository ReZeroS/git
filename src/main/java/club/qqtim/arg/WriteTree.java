package club.qqtim.arg;


import com.google.common.base.Joiner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lijie78
 * If hash-object was for storing an individual file, then write-tree is for storing a whole directory.
 */
@Data
@Slf4j
@CommandLine.Command(name = "write-tree")
public class WriteTree implements Runnable {

    @CommandLine.Parameters(index = "0", defaultValue = ".")
    private String path;

    @Override
    public void run() {
        try {
            writeTree(path);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     *
     * @param dirPath only accept directory path as param
     * @return tree Id
     * @throws Exception
     */
    private String writeTree(String dirPath) throws Exception {
        File file = new File(dirPath);
        String[] pathList = file.list();

        if (pathList == null) {
            return null;
        }

        // only traverse the filter path
        final List<String> filterPathList = Arrays.stream(pathList)
                .filter(this::isNotIgnored)
                .map(e -> String.format("%s/%s", dirPath, e)).collect(Collectors.toList());

        List<String> treeNodes = new ArrayList<>();

        String type = null;
        String objectId = null;
        for (String path : filterPathList) {
            File currentFile = new File(path);
            if (currentFile.isFile()) {
                type = "blob";
                HashObject hashObject = new HashObject();
                hashObject.setFile(currentFile);
                hashObject.setType(type);
                objectId = hashObject.call();

            } else if (currentFile.isDirectory()) {
                type = "tree";
                objectId = writeTree(path);
            }
            treeNodes.add(String.format("%s %s %s\n", type, objectId, currentFile.getName()));
        }

        HashObject hashObject = new HashObject();
        // file with content: every object in one line
        final String fileContent = Joiner.on("").join(treeNodes);
        // return tree id
        return hashObject.hashObject(fileContent.getBytes(), "tree");
    }


    /**
     * @param path file path
     * @return whether it's zit meta file
     */
    private boolean isNotIgnored(String path) {
        return !isIgnored(path);
    }
    /**
     * @param path file path
     * @return whether it's zit meta file
     */
    private boolean isIgnored(String path) {
        return path != null &&
                (
                        path.startsWith(club.qqtim.data.Data.ZIT_DIR)
                                || path.startsWith(".git")
                                || path.startsWith("doc")
                                || path.startsWith("target")

                );
    }
}
