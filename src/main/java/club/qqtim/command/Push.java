package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.RefObjValue;
import club.qqtim.data.RefValue;
import club.qqtim.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @title: Push
 * @Author lijie78
 * @Date: 2021/2/1
 * @Version 1.0.0
 *
 * Instead of pushing all objects in remote.push(), let's add a simple check to determine which objects the remote has:
 *
 * Take all remote refs that exist on the remote. Since the remote might have refs that point to branches that we didn't pull yet, filter out all refs that point to unknown OIDs.
 * Collect into a set all objects that are reachable from the known remote OIDs.
 * Collect into a set all objects that are reachable from the local branch that is being pushed.
 * Take the difference between the two sets. This effectively gives us the objects that are needed to fully describe the pushed branch but are missing from the remote.
 * Push those missing objects.
 */
@Slf4j
@lombok.Data
@CommandLine.Command(name = "push")
public class Push implements Runnable{

    @CommandLine.Parameters(index = "0")
    private String remote;

    @CommandLine.Parameters(index = "1")
    private String branch;




    @Override
    public void run() {
        push(remote, String.format(ConstantVal.BASE_REFS_HEADS_PATH, branch));
    }

    private void push(String remotePath, String refName) {
        final List<RefObjValue> remoteRefs = Fetch.getRemoteRefs(remotePath);
        final String localRef = ZitContext.getRef(refName).getValue();

        final List<String> knownRemoteRefs = remoteRefs.stream().map(RefObjValue::getValue).filter(ZitContext::objectExists).collect(Collectors.toList());

        final Set<String> remoteObjects = new HashSet<>(ZitContext.iteratorObjectsInCommits(knownRemoteRefs));
        final Set<String> localObjects = new HashSet<>(ZitContext.iteratorObjectsInCommits(Collections.singletonList(localRef)));
        final List<String> objectsToPush = localObjects.stream().filter(object -> !remoteObjects.contains(object)).collect(Collectors.toList());

        // only push missing objects
        for (String objectId : objectsToPush) {
            ZitContext.pushObject(objectId, remotePath);
        }

        FileUtil.setRootPathContext(remotePath);
        {
            ZitContext.updateRef(refName, new RefValue(false, localRef));
        }
        FileUtil.removeRootPathContext();

    }
}
