package club.qqtim.command;


import com.google.common.base.Joiner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author lijie78
 * If hash-object was for storing an individual file, then write-tree is for storing a whole directory.
 */
@Data
@Slf4j
@CommandLine.Command(name = "write-tree")
public class WriteTree implements Callable<String> {

    @CommandLine.Parameters(index = "0", defaultValue = ".")
    private String path;

    @Override
    public String call() {
        return writeTree(path);
    }


    /**
     *
     * @param dirPath only accept directory path as param
     * @return tree Id
     * @throws Exception
     */
    private String writeTree(String dirPath) {
        File file = new File(dirPath);
        String[] pathList = file.list();

        // if dirPath is not a directory then return null
        // if dirPath is an empty directory then return array with length equal zero
        if (pathList == null || pathList.length == 0) {
            return null;
        }

        // only traverse the filter path
        final List<String> filterPathList = Arrays.stream(pathList)
                .filter(club.qqtim.data.Data::isNotIgnored)
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
            // above key: ensure will write object id
            // for wrong example: empty directory
            if (objectId != null) {
                treeNodes.add(String.format("%s %s %s\n", type, objectId, currentFile.getName()));
            }
        }

        // path got all dir are empty directory, see the above key
        if (treeNodes.isEmpty()) {
            return null;
        }
        HashObject hashObject = new HashObject();
        // file with content: every object in one line
        final String fileContent = Joiner.on("").join(treeNodes);
        // return tree id
        return hashObject.hashObject(fileContent.getBytes(), "tree");
    }



}
