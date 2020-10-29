# ZIT development log

## TODO

1. git hash-object todo. When real Git stores objects it does a few extra things, such as writing the size of the object to the file as well, compressing them and dividing the objects into 256 directories. This is done to avoid having directories with huge number of files, which can hurt performance.


## Notes

1. 这个暂时记录下，没查明白：打包用 shade, 要不然要和 classpath相关联导致idea运行没啥问题，打包后却各种问题

2. 命令行解析用了 `picocli` 这个库，可参考 `git init`, `git hash-object` 了解基本使用，很容易上手且巨好用，简答说命令行的参数就两种，`options` 和 `positional parameters`，参考下图

![options and param](./doc/OptionsAndParameters2.png)

3. 工具库用了 `guava`, 里面的方法都是 `@Beta`, 所以后续看看是不是要提取下或者用 `zerobox` 作为依赖

4. 