package club.qqtim.command;

import club.qqtim.data.CommitObject;
import club.qqtim.data.Data;
import club.qqtim.data.RefObject;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @title: Lg
 * @Author lijie78
 * @Date: 2020/12/7
 * @Version 1.0.0
 */
@Slf4j
@lombok.Data
@CommandLine.Command(name = "lg")
public class Lg implements Callable<String> {


    @Override
    public String call() {
        Set<String> idsSet = new HashSet<>();
        final List<RefObject> refObjects = Data.iteratorRefs();
        refObjects.forEach(refObject -> {
            log.debug(refObject.toString());
            idsSet.add(refObject.getRef());
        });
        final List<String> refIds = Data.iteratorCommitsAndParents(idsSet);
        refIds.forEach(refId -> {
            final CommitObject commit = Commit.getCommit(refId);
            log.info(refId);
            if (Objects.nonNull(commit.getParent())) {
                log.info("Parent: {}", commit.getParent());
            }
        });
        return null;
    }
}
