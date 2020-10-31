package club.qqtim.command;


import picocli.CommandLine;

/**
 * @author lijie78
 */
@CommandLine.Command(name = "zit", subcommands = {
        Init.class,
        HashObject.class,
        CatFile.class,
        WriteTree.class,
        ReadTree.class,
        Commit.class,
        Log.class
})
public class Zit {
}
