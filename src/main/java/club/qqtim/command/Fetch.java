package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.RefObjValue;
import club.qqtim.data.RefObject;
import club.qqtim.data.RefValue;
import club.qqtim.util.FileUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Note that real Git supports multiple remotes, and each remote has a name.
 * For example, if there is a remote named "origin", its branches would go under refs/remote/origin/.
 * But in zit we assume for simplicity that there's only one remote and just put its branches under refs/remote/.
 * @title: Fetch
 * @Author rezeros.github.io
 * @Date: 2021/1/1
 * @Version 1.0.0
 */
@Data
@Slf4j
@CommandLine.Command(name = "fetch")
public class Fetch implements Runnable {

    @CommandLine.Parameters(index = "0")
    private String remote;


    /**
     * if the remote repository has refs/heads/master we're going to save it locally as refs/remote/master
     */
    private static final String REMOTE_REFS_BASE = "refs/heads";
    private static final String LOCAL_REFS_BASE = "refs/remote";

    @Override
    public void run() {
        fetch(remote);
    }

    public static void fetch(String remotePath) {
        final String currentDir = FileUtil.getCurrentDir();
        FileUtil.setRootPathContext(remotePath);

        {
            log.debug("Will fetch the following refs:");
            final List<RefObjValue> remoteRefs = getRemoteRefs(remotePath, REMOTE_REFS_BASE);

            final List<String> objectIds = ZitContext.iteratorObjectsInCommits(remoteRefs.stream()
                    .map(RefObjValue::getValue).distinct().collect(Collectors.toList()));
            objectIds.forEach(objectId -> ZitContext.fetchObjectIfMissing(objectId, remotePath));

            remoteRefs.forEach(remoteRef -> {
                final String remoteName = remoteRef.getRefName();
                final Path refName = Paths.get(REMOTE_REFS_BASE).relativize(Paths.get(remoteName));

                ZitContext.updateRef(Paths.get(LOCAL_REFS_BASE).resolve(refName).toString(),
                        new RefValue(false, remoteRef.getValue()));
            });
        }

        FileUtil.setRootPathContext(currentDir);
    }

    public static List<RefObjValue> getRemoteRefs(String remotePath) {
        final String currentDir = FileUtil.getCurrentDir();
        FileUtil.setRootPathContext(remotePath);

        final List<RefObjValue> remoteRefs = getRemoteRefs(remotePath, ConstantVal.EMPTY);

        FileUtil.setRootPathContext(currentDir);

        return remoteRefs;
    }

    public static List<RefObjValue> getRemoteRefs(String remotePath, String prefix) {

        final List<RefObject> refObjects = ZitContext.iteratorRefs(prefix);

        return refObjects.stream().map(refObject -> {
            final RefObjValue refObjValue = new RefObjValue();
            refObjValue.setRefName(refObject.getRefName());
            refObjValue.setValue(refObject.getRefValue().getValue());
            return refObjValue;
        }).collect(Collectors.toList());
    }

}
