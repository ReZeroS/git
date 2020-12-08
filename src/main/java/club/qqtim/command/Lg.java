package club.qqtim.command;

import club.qqtim.data.Data;
import club.qqtim.data.RefObject;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @title: Lg
 * @Author lijie78
 * @Date: 2020/12/7
 * @Version 1.0.0
 */
@lombok.Data
@Slf4j
@CommandLine.Command(name = "lg")
public class Lg implements Callable<String> {


    @Override
    public String call() {
        final List<RefObject> refObjects = Data.iteratorRefs();

        return null;
    }
}
