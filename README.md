# [GIT](https://github.com/ReZeroS/git) Development Log

Special thanks to Nikita for the excellent tutorial: [https://www.leshenko.net/p/ugit](https://www.leshenko.net/p/ugit)

## Resources

https://deepwiki.com/ReZeroS/git/1-overview

I've compiled useful reference materials in the `./doc` directory. Additionally, these resources may be helpful:

- [Nick Butler](http://simplygenius.net/Article/DiffTutorial1): A concise overview of diff algorithms
- [jcoglan](https://blog.jcoglan.com/2017/02/12/the-myers-diff-algorithm-part-1/): Detailed explanation of the Myers diff algorithm
- [Visualization](https://blog.robertelder.org/diff-algorithm/): Helpful tool for visualizing and debugging diff algorithms
- [PDF](https://github.com/ReZeroS/git/blob/main/doc/ZIT.pdf): A brief pdf about Git that you might find interesting

If you're interested in this project, feel free to share your ideas in the discussion forum or contact me via email.

## Usage

0. `alias zit='java -jar ../zit-1.0-SNAPSHOT-shaded.jar'` - Create an alias for the zit executable
   
   - On Windows, add this to `C:\Program Files\Git\etc\profile.d\aliases.sh`

1. `zit init` - Initialize the `.zit` directory with an `objects` subdirectory, create an index file (staging area), and set up the default `main` branch
    
   - The default HEAD file contains: `ref: ref/heads/main`

2. `zit hash-object file` 
   
   - Accepts a file path
   - Reads the file content
   - Hashes the content using SHA-1
   - Stores the file at `.ugit/objects/{SHA-1-hash}`

3. `zit cat-file hash [object|tree|commit|...type]` - Display file content

4. `zit write-tree` - Generate a tree structure representing the entire repository
   
   - Typically used after the `add` command to create a tree from the index file

5. `zit read-tree hash` 
   
   - Warning: This will remove existing content before reading
   - Use `cat-file` to locate the `root` tree
   - `write-tree` logs can also help identify all trees

6. While `write-tree` captures snapshots, it lacks contextual information. Use `zit commit -m "message"` for proper versioning
   
   - Check commit content with `cat-file hash commit-id`
   - `HEAD` records commits with parent information

7. Use `log` to view commit history after making commits

8. Checkout: Select a commit ID from `log` to restore that state
   
   - [Fixed with getBytes(Charsets.UTF_8)] Bug: Chinese file/directory names previously displayed incorrectly
   - Accepts head alias, hash, or ref (branch, tags, HEAD, etc.)

9. `tag` creates an alias for a commit ID, introducing a core Git concept
   
   - The [git-ref](https://git-scm.com/book/en/v2/Git-Internals-Git-References) documentation provides essential reference knowledge

10. TODO: Implement `zit lg` graph visualization using Graphviz

11. `zit branch name [id]` - Create branches as in standard Git
    
    - All refs under `refs/heads` are treated as branches
    - File content is simply the commit ID, defaulting to HEAD

12. `zit show` displays detailed changes using diff, while `status` shows summary information

13. `zit add` stages files or directories in `.zit/index`

14. `zit commit` invokes `write-tree` and updates the HEAD pointer to the new commit ID
    
    - First use creates the default `main` branch and updates HEAD
    - Removes MERGE_HEAD and includes message in commit

15. `zit status` shows the current repository state
    
    - Displays current branch (unless in detached HEAD state)
    - Shows merge hash ID during merge operations
    - Lists changes to be committed (diff between HEAD tree and index)
    - Lists unstaged changes (diff between index and working tree)

16. `zit diff` implements the Myers diff algorithm without linear space refinement

17. `zit reset` moves HEAD to the specified commit (difference from `checkout` pending implementation)

18. `zit merge` checks if the merge base equals HEAD, using fast-forward merge when possible
    
    - Fast-forward requires no additional commit
    - Otherwise uses diff3 to merge the base, HEAD tree, and other tree
    - Leaves `merge_head` in the zit root directory, requiring manual commit
    - `zit merge-base` helps find the common ancestor commit for merging and debugging

19. `zit fetch` and `zit push` download/upload objects and update references

## Summary

### UPDATED 2021.02.21

- Implemented diff (Myers algorithm without linear space optimization) and merge (simple diff3) algorithms instead of relying on Unix tools
- While `Ugit` uses Pythonic code, zit aims to provide code that's easily understandable for developers from various language backgrounds

### TODO

1. Enhance `git hash-object` to match real Git behavior:
   - Write object size to file
   - Compress objects
   - Distribute objects across 256 directories to avoid performance issues with large file counts
