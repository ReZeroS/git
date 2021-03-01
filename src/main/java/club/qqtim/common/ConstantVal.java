package club.qqtim.common;

import club.qqtim.diff.LineObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rezeros.github.io
 */
public final class ConstantVal {
    public static final String HASH_ALGORITHM = "SHA-1";

    // action type
    public static final String SYNC = "SYNC";
    public static final String PLUS = "PLUS";
    public static final String PLUS_SYMBOL = "+";
    public static final String MINUS = "MINUS";
    public static final String MINUS_SYMBOL = "-";
    public static final String CONFLICT = "CONFLICT";


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

    public static final char NULL_CHAR = 0;

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

    // merge conflict

    public static final String HEAD_CONFLICT = "<<<<<<<<<<";
    public static final String OTHER_CONFLICT = ">>>>>>>>>>";
    public static final String ORIGIN_CONFLICT = "==========";

    public static Map<String, LineObject> MERGE_CONFLICT = new HashMap<>(4);

    static {
        final LineObject headLine = new LineObject();
        headLine.setAction(CONFLICT);
        headLine.setLineContent(HEAD_CONFLICT);
        MERGE_CONFLICT.put(HEAD_CONFLICT, headLine);

        final LineObject originLine = new LineObject();
        originLine.setAction(CONFLICT);
        originLine.setLineContent(ORIGIN_CONFLICT);
        MERGE_CONFLICT.put(ORIGIN_CONFLICT, originLine);

        final LineObject otherLine = new LineObject();
        otherLine.setAction(CONFLICT);
        otherLine.setLineContent(OTHER_CONFLICT);
        MERGE_CONFLICT.put(OTHER_CONFLICT, otherLine);

    }

}
