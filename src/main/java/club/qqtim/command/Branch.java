package club.qqtim.command;

import club.qqtim.common.ConstantVal;
import club.qqtim.context.ZitContext;
import club.qqtim.converter.IdConverter;
import club.qqtim.data.RefObject;
import club.qqtim.data.RefValue;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @title: Branch
 * @Author rezeros.github.io
 * @Date: 2020/12/9
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "branch")
public class Branch implements Runnable{


    @CommandLine.Parameters(index = "0", defaultValue = ConstantVal.NONE)
    private String name;



    @CommandLine.Parameters(index = "1", defaultValue = ConstantVal.HEAD_ALIAS, converter = IdConverter.class)
    private String startPoint;


    /**
     * Pay attention, only you create a commit at branch, the branch will generate at real
     * so if you execute `zit init`, then `zit branch` immediately will get nothing, this is not a bug.
     */
    @Override
    public void run() {
        // none name for readable command
        if(ConstantVal.NONE.equals(name)) {
            final String currentBranch = ZitContext.getBranchName();
            final List<String> branchNames = iteratorBranchNames();
            branchNames.forEach(branchName ->
                    log.info("{} {}", branchName.equals(currentBranch) ? ConstantVal.STAR : ConstantVal.EMPTY, branchName)
            );
        } else {
            // got name for branch will be created base on the commit point
            createBranch(name, startPoint);
            log.info("created branch {} at {}", name, startPoint.substring(0, 11));
        }
    }

    private List<String> iteratorBranchNames(){
        final List<RefObject> refObjects = ZitContext.iteratorRefs(ConstantVal.HEADS_PATH);
        return refObjects.stream()
                .map(RefObject::getRefName)
                .map(path -> Paths.get(path).relativize(Paths.get(ConstantVal.HEADS_PATH)).toString())
                .collect(Collectors.toList());
    }

    private static void createBranch(String name, String startPoint) {
        ZitContext.updateRef(String.format(ConstantVal.BASE_REFS_HEADS_PATH, name), new RefValue(false, startPoint));
    }

    public static boolean existBranch(String name){
        final RefValue ref = ZitContext.getRef(String.format(ConstantVal.BASE_REFS_HEADS_PATH, name));
        return Objects.nonNull(ref.getValue());
    }

}
