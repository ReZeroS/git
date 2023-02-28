# [GIT](https://github.com/ReZeroS/git) development log

Thanks for the great tutorial: [https://www.leshenko.net/p/ugit](https://www.leshenko.net/p/ugit)

Thanks again Nikita!
  

## Resource

I found some good reference materials, and I put them under the `./doc` directory.

Besides, the follow links maybe help you too.

 - [Nick Butler](http://simplygenius.net/Article/DiffTutorial1) this post is short but enough to help you have a higher level to understand diff
 - [jcoglan](https://blog.jcoglan.com/2017/02/12/the-myers-diff-algorithm-part-1/) this posts make a detail description about the diff.
 - [visualize](https://blog.robertelder.org/diff-algorithm/) if you wanna have a debugger or visualization about diff algorithm, this will be a good choice.
 - [pdf](https://github.com/ReZeroS/zit/blob/main/doc/ZIT.pptx) I also made a short pdf about git, hope you would like it.
If you feel interested about this project, you can post your idea on the discussion or just contact me with email.

## Usage

0. `alias zit='java -jar ../zit-1.0-SNAPSHOT-shaded.jar'` alias the zit executable file.
   
    - windows could set here `C:\Program Files\Git\etc\profile.d\aliases.sh`

1. `zit init` init directory `.zit` which include `objects` subdirectory, init index file which would be treated as stage area, and set `main` branch as default to prevent detached head
    
    - the default HEAD file content is `ref: ref/heads/main`

2. `zit hash-object file` 
   
    - Get the path of the file to store.
    - Read the file.
    - Hash the *content* of the file using SHA-1.
    - Store the file under `.ugit/objects/{the SHA-1 hash}`.
 
3. `zit cat-file hash [object|tree|commit|...type]` print the file content

4. `zit write-tree` generate the tree which is the whole description of the repository.
   
    - after finished `add` command, it will write tree which is generated by index file

5. `zit read-tree hash` 
   
    - pay attention！this action will delete all existing stuff before reading.
    - So you can use `cat-file` to find which tree is the `root`, and the logs of `write-tree` also help you find all the trees.  

6. Although `write-tree` can save version, but it does not take any context information, so will need to develop `zit commit -m "message"` command. 
   
    - you can use `cat-file hash commit-id` to check your commit content
    - `HEAD` will record your commit with its parent.
   
7. Just enjoy commit and the type `log` to see the logs.

8. Now we get the first point: `checkout`. Pick a commit id from the `log` and checkout whether things as expected.

    - [fixed with getBytes(Charsets.UTF-8)] find bug todo: chinese file or dir name got messy code
    - args could be head alias, hash and ref(branch, tags, HEAD...)
    
9. `tag` will alias commit id, and at this time, you will get first inner core concept.
    
    - [git-ref](https://git-scm.com/book/en/v2/Git-Internals-Git-References) the official post will help you learn some basic knowledge about the git.

10. todo: `zit lg` graph feature with Graphviz

11. `zit branch name [id]` so familiar.
    
    - Every ref under refs/heads will be treated as a branch.
    - file content still just commit id, by default it is the head point
     
12. `zit show` will use diff show changes detail while status only show simply changes info.

13. `zit add` will add paths which could be file or directory into stage file: `.zit/index`.
 
14. `zit commit` will call `write tree` and update head pointer to the commit id.
    
    - first time it will create default branch: `main` and will rewrite the HEAD file content to the commit id
    - the merge HEAD will be deleted and leave the message into commit message
    
15. `zit status` this command will tell you what is the situation you are in now.
    
    - if you are not detached HEAD, it will log your current HEAD pointed branch first,
    - after that, if you are working in merge, it will log the merge hash id,
    - then it will log changes to be committed which will diff head tree to index(stage items),
    - finally, it will log changes not staged for the next commit which diff index(stage) to work tree.
    
16. `zit diff` the default diff algorithm is myers diff without linear space refinement optimized

17. `zit reset` just change head to the current commit, the difference between it and `checkout` is the  // todo

18. `zit merge` will check if the merge base equals the head, it will use fast-forward to merge 
    
    - if fast-forward work, it will be no need to commit
    - if not work, we will use diff3 merge to merge the **merge base**, **head tree**, **other tree**
    - pay attention: diff3 will leave merge_head in the zit root directory and that means you need to commit manually.
    - `zit merge-base` is used to help the merge command find the first common parent commit of the commits which will be merged. But you also can use this command to do debug task.
    
19. `zit fetch`, `zit push` these two combined commands are used to download or upload objects and update the ref.



## Summary

### UPDATED 2021.02.21

-  implemented the diff(myers diff but without linear space optimized) and merge algorithms(simple diff3) instead of using unix tools.

- `Ugit` use some cool `Pythonic` code while zit trying to make code easy understood for the other language developer.

### TODO

1. git hash-object todo. When real Git stores objects it does a few extra things, such as writing the size of the object to the file as well, compressing them and dividing the objects into 256 directories. This is done to avoid having directories with huge number of files, which can hurt performance.

