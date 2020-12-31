package club.qqtim.command;

import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @title: MergeBase
 * @Author lijie78
 * @Date: 2020/12/31
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "merge-base")
public class MergeBase implements Runnable {


    @CommandLine.Parameters(index = "0", converter = IdConverter.class)
    private String commitFirst;

    @CommandLine.Parameters(index = "1", converter = IdConverter.class)
    private String commitSecond;


    @Override
    public void run() {

        final String mergeBase = getMergeBase(commitFirst, commitSecond);
        log.debug("Compute common ancestor of a commit: {}", mergeBase);

    }

    /**
     * @param commitFirst first commit
     * @param commitSecond second commit
     * @return Compute common ancestor of a commit
     */
    public static String getMergeBase(String commitFirst, String commitSecond) {
        final List<String> parentIdsOfFirstCommit = ZitContext.iteratorCommitsAndParents(Collections.singleton(commitFirst)).stream().distinct().collect(Collectors.toList());

        final List<String> parentIdsOfSecondCommit = ZitContext.iteratorCommitsAndParents(Collections.singleton(commitSecond));

        for (String secondId : parentIdsOfSecondCommit) {
            if (parentIdsOfFirstCommit.contains(secondId)) {
                return secondId;
            }
        }
        return null;
    }
}
