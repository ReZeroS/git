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
 * https://www.leshenko.net/p/ugit/#k-render-graph
 */
@Slf4j
@lombok.Data
@CommandLine.Command(name = "lg")
public class Lg implements Callable<String> {


    @Override
    public String call() {
        StringBuilder dotGraph = new StringBuilder("digraph commits {\n");


        Set<String> idsSet = new HashSet<>();
        final List<RefObject> refObjects = Data.iteratorRefs();
        for (RefObject refObject : refObjects) {
            dotGraph.append(String.format("\"{%s}\" [shape=note]\n", refObject.getRefName()));
            dotGraph.append(String.format("\"{%s}\" -> \"{%s}\"\n", refObject.getRefName(), refObject.getRef()));
            idsSet.add(refObject.getRef());
        }
        final List<String> refIds = Data.iteratorCommitsAndParents(idsSet);
        refIds.forEach(refId -> {
            final CommitObject commit = Commit.getCommit(refId);
            final String shapeBox = String.format("\"{%s}\" [shape=box style=filled label=\"{%s}\"]\n", refId, refId.substring(0, 11));
            dotGraph.append(shapeBox);
            if (Objects.nonNull(commit.getParent())) {
                final String parent = String.format("\"{%s}\" -> \"{%s}\"\n", refId, commit.getParent());
                dotGraph.append(parent);
            }
        });
        dotGraph.append("}");
        log.info(dotGraph.toString());

        //todo: output rendered image to the screen
        return null;
    }
}
