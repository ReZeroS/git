package club.qqtim.common;

import java.util.Arrays;
import java.util.List;

/**
 * @author rezeros.github.io
 */
public final class ConstantVal {
    public static final String HASH_ALGORITHM = "SHA-1";

    // action type
    public static final String SYNC = "SYNC";
    public static final String PLUS = "PLUS";
    public static final String MINUS = "MINUS";


    // ref type

    public static final String HEAD = "HEAD";
    public static final String HEAD_ALIAS = "@";

    public static final String MERGE_HEAD = "MERGE_HEAD";

    // param type

    public static final String NONE = "None";


    // object type

    public static final String PARENT = "parent";

    public static final String TREE = "tree";

    public static final String BLOB = "blob";

    public static final String COMMIT = "commit";


    // symbol type
    public static final String STAR = "*";

    public static final String EMPTY = "";

    public static final String NEW_LINE = "\n";

    public static final String SINGLE_SPACE = " ";

    public static final String UNIX_PATH_SEPARATOR = "/";

    public static final String BASE_PATH = "./";


    public static final String BASE_FORMAT = "%s";
    public static final String BASE_REFS_PATH = "refs/%s";
    public static final String BASE_REFS_TAGS_PATH = "refs/tags/%s";

    public static final String HEADS_PATH = "refs/heads";
    public static final String BASE_REFS_HEADS_PATH = HEADS_PATH + "/%s";

    public static final List<String> REF_REGISTRY_DIRECTORIES =
            Arrays.asList(BASE_FORMAT, BASE_REFS_PATH, BASE_REFS_TAGS_PATH, BASE_REFS_HEADS_PATH);


    // default value

    public static final String DEFAULT_BRANCH = "main";

    public static final String ZIT_DIR = ".zit";
    public static final String OBJECTS_DIR = ZIT_DIR + "/objects";
    public static final String REFS_DIR = "refs";
    public static final String REFS_DIR_REAL = ZIT_DIR + "/" + REFS_DIR;

    public static final String INDEX = ZIT_DIR + "/index";

}
