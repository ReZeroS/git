package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.data.RefObjValue;
import club.qqtim.data.RefValue;
import club.qqtim.util.FileUtil;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @title: Push
 * @Author ReZeroS
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
        final RefObjValue remoteRef = remoteRefs.stream().filter(e -> e.getRefName().equals(refName)).findAny().orElse(null);

        final String localRef = ZitContext.getRef(refName).getValue();

        // new branch or have common parent commit
        if(!(Objects.isNull(remoteRef) || isAncestorOf(localRef, remoteRef.getValue()))){
            log.error("force pushed fail");
            throw new IllegalArgumentException("can be forced push");

        }

        final String currentDir = FileUtil.getCurrentDir();
        FileUtil.setRootPathContext(remotePath);

        final List<String> knownRemoteRefs = remoteRefs.stream().map(RefObjValue::getValue).filter(ZitContext::objectExists).collect(Collectors.toList());
        final Set<String> remoteObjects = new HashSet<>(ZitContext.iteratorObjectsInCommits(knownRemoteRefs));

        FileUtil.setRootPathContext(currentDir);


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
        FileUtil.setRootPathContext(currentDir);

    }



    /**
     * To prevent it from happening we're going to allow pushing only in two cases:
     *
     * The ref that we're pushing doesn't exist yet on the remote. It means that it's a new branch and there is no risk of overwriting other's work.
     *
     * If the remote ref does exist, it must point to a commit that is an ancestor of the pushed ref.
     * This ancestry means that the local commit is based on the remote commit,
     * which means that the remote commit not getting overwritten, since it's part of the history of the newly pushed commit.
     */
    private boolean isAncestorOf(String localRef, String maybeAncestor) {
        return ZitContext.iteratorObjectsInCommits(Collections.singletonList(localRef)).contains(maybeAncestor);
    }


}
