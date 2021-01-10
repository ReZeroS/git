package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.CommitObject;
import club.qqtim.context.ZitContext;
import club.qqtim.data.RefObject;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @title: Log
 * @Author rezeros.github.io
 * @Date: 2020/10/31
 * @Version 1.0.0
 */

@lombok.Data
@Slf4j
@CommandLine.Command(name = "log")
public class Log implements Runnable{

    @CommandLine.Parameters(index = "0", defaultValue = ConstantVal.HEAD_ALIAS, converter = IdConverter.class)
    private String id;

    @Override
    public void run() {
        Map<String, List<String>> refValueName = new HashMap<>(8);

        final List<RefObject> refObjects = ZitContext.iteratorRefs();
        refObjects.forEach(refObject -> {
            final String refName = refObject.getRefName();
            final String value = refObject.getRefValue().getValue();
            refValueName.putIfAbsent(value, new ArrayList<>());
            final List<String> existRefNames = refValueName.get(value);
            existRefNames.add(refName);
        });


        // if no args, set HEAD
        // else use tag or hash as object id
        final List<String> idList = ZitContext.iteratorCommitsAndParents(Collections.singletonList(id));
        idList.forEach(objectId -> {
            CommitObject commit = Commit.getCommit(objectId);
            final List<String> refNames = refValueName.getOrDefault(objectId, Collections.emptyList());
            printCommit(objectId, commit, refNames);
        });
    }

    public static void printCommit(String objectId, CommitObject commit) {
        printCommit(objectId, commit, Collections.emptyList());
    }

    public static void printCommit(String objectId, CommitObject commit, List<String> refNames) {
        String refsStr = String.join(",", refNames);
        log.info(String.format("%s %s (%s)\n", ConstantVal.COMMIT, objectId, refsStr));
        log.info(String.format("%s\n", commit.getMessage()));
    }
}
