package club.qqtim.command;

import club.qqtim.context.ZitContext;
import club.qqtim.data.CommitObject;
import club.qqtim.data.RefObject;
import club.qqtim.data.RefValue;
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
        final List<RefObject> refObjects = ZitContext.iteratorRefs(false);
        for (RefObject refObject : refObjects) {
            dotGraph.append(String.format("\"{%s}\" [shape=note]\n", refObject.getRefName()));
            final RefValue refValue = refObject.getRefValue();
            dotGraph.append(String.format("\"{%s}\" -> \"{%s}\"\n", refObject.getRefName(), refValue.getValue()));
            if (!refValue.getSymbolic()) {
                idsSet.add(refObject.getRefValue().getValue());
            }
        }
        final List<String> refIds = ZitContext.iteratorCommitsAndParents(idsSet);
        refIds.forEach(refId -> {
            final CommitObject commit = Commit.getCommit(refId);
            final String shapeBox = String.format("\"{%s}\" [shape=box style=filled label=\"{%s}\"]\n", refId, refId.substring(0, 11));
            dotGraph.append(shapeBox);
            if (Objects.nonNull(commit.getParents()) && !commit.getParents().isEmpty()) {
                commit.getParents().forEach(parent -> {
                    dotGraph.append(String.format("\"{%s}\" -> \"{%s}\"\n", refId, parent));
                });
            }
        });
        dotGraph.append("}");
        log.info(dotGraph.toString());

        //todo: output rendered image to the screen
        return null;
    }
}
