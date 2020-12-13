# ZIT development log

## Summary

原作者地址: [https://www.leshenko.net/p/ugit](https://www.leshenko.net/p/ugit)

对 `git` 挺感兴趣，就照着实现了一个java版本的，多谢作者大佬写出这样的文章系列能让我们小白也体验一把。

## TODO

1. git hash-object todo. When real Git stores objects it does a few extra things, such as writing the size of the object to the file as well, compressing them and dividing the objects into 256 directories. This is done to avoid having directories with huge number of files, which can hurt performance.


## Notes

1. 这个暂时记录下，没查明白：打包用 shade, 要不然要和 classpath相关联导致idea运行没啥问题，打包后却各种问题

2. 命令行解析用了 `picocli` 这个库，可参考 `git init`, `git hash-object` 了解基本使用，很容易上手且巨好用，简答说命令行的参数就两种，`options` 和 `positional parameters`，参考下图

![options and param](./doc/OptionsAndParameters2.png)

3. 工具库用了 `guava`, 里面的方法都是 `@Beta`, 所以后续看看是不是要提取下或者用 `zerobox` 作为依赖

4. `write-tree` 几个注意点

   - 对象分两种，一个 是 `tree` 代表目录且仅返回一级目录的情况, 一个是 `blob` 即文件
   - 在递归过程中不要忘记了路径的变换要带上前置路径来确保总是使用同一个位置的相对路径
   - `File` 类为空文件夹 `listPath` 会返回长度为 0 的数组，只有当不是文件夹时该函数才返回 `null`
   - `listFiles` 获取的 `File` 列表中的 `file` 调用 `getPath` 返回的路径是拼接前置路径的，但是会因为环境的不同，路径的分隔符就会不同，这点需要考虑到 `ignore` 函数上
   
5. `os.walk` 的返回值是一个生成器(generator),也就是说我们需要不断的遍历它，来获得所有的内容。而每次遍历的对象都是返回的是一个三元组(root,dirs,files)
   
   - `root` 所指的是当前正在遍历的这个文件夹的本身的地址
   - `dirs` 是一个 list ，内容是该文件夹中所有的目录的名字(不包括子目录)
   - `files` 同样是 list , 内容是该文件夹中所有的文件(不包括子目录)
   - Java nio 包的 `Files` class 也同样提供了类似的功能 
   
## Usage

0. `alias zit='java -jar ../zit-1.0-SNAPSHOT-shaded.jar'` alias the zit executable file.
   
   - windows could set here `C:\Program Files\Git\etc\profile.d\aliases.sh`

1. `zit init` init directory `.zit` which include `objects` subdirectory and set `main` branch as default to prevent detached head

2. `zit hash-object file` 
   
   - Get the path of the file to store.
   - Read the file.
   - Hash the *content* of the file using SHA-1.
   - Store the file under ".ugit/objects/{the SHA-1 hash}".
   
3. `zit cat-file hash [object|tree|commit|...type]` print the file content

4. `zit write-tree` generate the tree which is the whole description of the repository

5. `zit read-tree hash` 
   
   - pay attention！this action will delete all existing stuff before reading.
   - So you can use `cat-file` to find which tree is the `root`, and the logs of `write-tree` also help you find all the trees.  

6. Although `write-tree` can save version, but it does not take any context information, so will need to develop `zit commit -m "message"` command. 
   
   - you can use `cat-file hash commit` to check your commit content
   - `HEAD` will record your commit with its parent.
   
7. Just enjoy commit and the type `log` to see the logs.

8. Now we get the first point: `checkout`. Pick a commit id from the `log` and checkout whether things as expected.

   - [fixed with getBytes(Charsets.UTF-8)] find bug todo: chinese file or dir name got messy code
   
9. `tag` will alias commit id, and at this time, you will get first inner core concept.
   
   - [git-ref](https://git-scm.com/book/en/v2/Git-Internals-Git-References)

10. todo: `zit lg` graph feature with Graphviz

11. `zit branch name [id]` so familiar.
   
   - every ref under refs/heads as a branch.
     
12. 