<img alt="logo" src="https://www.objectionary.com/cactus.svg" height="100px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/objectionary/ddr/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/objectionary/ddr/actions/workflows/build.yml)
[![Hits-of-Code](https://hitsofcode.com/github/objectionary/ddr)](https://hitsofcode.com/view/github/objectionary/ddr)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/objectionary/ddr/blob/master/LICENSE.txt)

DDR is an Experimental Dynamic Dispatch Removal Toolkit for [EO](https://www.eolang.org) Programs

### Launch

You can launch the tool from IntelliJ Idea using MainKt configuration.  
Just click `Edit configuration` in the `'Edit run/debug configurations' dialog` and
type an absolute path to your input directory with `.xmir` files in the `Program arguments` field.

After the execution a new directory named `${YOUR_DIRECTORY_NAME}_ddr` will be created near your directory.

### Tests

#### How to add integration tests:
1) Add your directory with `.eo` source files to `src/test/resources/integration/in`.  
Note that directory name must be written in snake case.

2) Add your directory with desired output `.eo` files to `src/test/resources/integration/out`.  
Note that `.eo` files in the output directory must use dot notation like shown below:
```
[] > greet
  QQ
  .io
  .stdout > @
    "Hello, world!"
```

3) Add a test to [ResolverTest](src/test/kotlin/org/polystat/eodv/integration/resolver/ResolverTest.kt).  
Your test name must be identical to your added directory name with `_` symbols replaced with spaces and a word `test` inserted in the beginning.  
Example:  
Directory name: `basic_example`  
Test:
```
@Test
fun `test basic example`() = doTest()
```

If your tests don't pass - you can take a look at temporary files in `src/test/resources/integration/eo_outputs` directory.
It should be more convenient for you, because you will have all your actual `.eo` files located there.

That's it :)