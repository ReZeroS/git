# [GIT](https://github.com/ReZeroS/git) Development Log

Thanks for the great tutorial: [https://www.leshenko.net/p/ugit](https://www.leshenko.net/p/ugit)

Thanks again, Nikita!

## Resources

I found some good reference materials and placed them in the `./doc` directory.

Additionally, these links might be helpful:

- [Nick Butler](http://simplygenius.net/Article/DiffTutorial1): A concise post that provides a high-level understanding of diff
- [jcoglan](https://blog.jcoglan.com/2017/02/12/the-myers-diff-algorithm-part-1/): A detailed description of the diff algorithm
- [Visualization](https://blog.robertelder.org/diff-algorithm/): A great resource for debugging or visualizing the diff algorithm
- [PDF](https://github.com/ReZeroS/zit/blob/main/doc/ZIT.pptx): A short PDF about Git that I hope you'll find interesting

If you're interested in this project, feel free to share your ideas in the discussion or contact me via email.

## Usage

0. `alias zit='java -jar ../zit-1.0-SNAPSHOT-shaded.jar'` - Alias the zit executable file
   
   - On Windows, you can set this in `C:\Program Files\Git\etc\profile.d\aliases.sh`

1. `zit init` - Initialize the `.zit` directory with an `objects` subdirectory, create an index file (stage area), and set the default `main` branch
    
   - The default HEAD file content is `ref: ref/heads/main`

2. `zit hash-object file` 
   
   - Get the file path to store
   - Read the file
   - Hash the *content* of the file using SHA-1
   - Store the file under `.ugit/objects/{the SHA-1 hash}`

3. `zit cat-file hash [object|tree|commit|...type]` - Print the file content

4. `zit write-tree` - Generate a tree describing the entire repository
   
   - Executed after the `add` command, creating a tree from the index file

5. `zit read-tree hash` 
   
   - Caution: This action will delete all existing content before reading
   - Use `cat-file` to find the `root` tree
   - Logs from `write-tree` can also help you find all trees

6. While `write-tree` can save versions, it lacks context information, so a `zit commit -m "message"` command is needed
   
   - Use `cat-file hash commit-id` to check commit content
   - `HEAD` will record the commit with its parent

7. Enjoy committing and use `log` to view commit history

8. Checkout: Select a commit ID from the `log` and verify the state
   
   - [Fixed with getBytes(Charsets.UTF-8)] Bug: Chinese file or directory names may appear garbled
   - Arguments can be head alias, hash, or ref (branch, tags, HEAD...)

9. `tag` will alias a commit ID, introducing a core concept
   
   - [git-ref](https://git-scm.com/book/en/v2/Git-Internals-Git-References) official post helps learn basic reference knowledge

10. TODO: `zit lg` graph feature with Graphviz

11. `zit branch name [id]` - Familiar branch creation
    
    - Every ref under `refs/heads` is treated as a branch
    - File content is simply the commit ID, defaulting to the head point

12. `zit show` will display detailed changes using diff, while `status` shows simple change information

13. `zit add` adds files or directories to the stage file: `.zit/index`

14. `zit commit` calls `write-tree` and updates the HEAD pointer to the commit ID
    
    - First-time usage creates the default `main` branch and rewrites the HEAD file content
    - Merge HEAD is deleted, and the message is added to the commit message

15. `zit status` shows the current situation
    
    - If not in a detached HEAD state, logs the current HEAD-pointed branch
    - Logs merge hash ID if in a merge state
    - Lists changes to be committed (diff between HEAD tree and index)
    - Lists changes not staged for the next commit (diff between index and working tree)

16. `zit diff` uses the Myers diff algorithm without linear space refinement optimization

17. `zit reset` changes HEAD to the current commit (difference from `checkout` is pending)

18. `zit merge` checks if the merge base equals the head, using fast-forward merge if possible
    
    - Fast-forward requires no commit
    - Otherwise, uses diff3 to merge the merge base, head tree, and other tree
    - Leaves `merge_head` in the zit root directory, requiring manual commit
    - `zit merge-base` helps find the first common parent commit for merging and debugging

19. `zit fetch` and `zit push` download or upload objects and update references

## Summary

### UPDATED 2021.02.21

- Implemented diff (Myers diff without linear space optimization) and merge algorithms (simple diff3) instead of using Unix tools
- `Ugit` uses Pythonic code, while zit aims to make the code easily understandable for developers of other languages

### TODO

1. `git hash-object` improvement: When real Git stores objects, it:
   - Writes the object size to the file
   - Compresses objects
   - Divides objects into 256 directories to avoid performance issues with large numbers of files
