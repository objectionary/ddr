<img alt="logo" src="https://www.objectionary.com/cactus.svg" height="100px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/objectionary/eo-files)](http://www.rultor.com/p/objectionary/ddr)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/objectionary/ddr/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/objectionary/ddr/actions/workflows/build.yml)
[![Hits-of-Code](https://hitsofcode.com/github/objectionary/ddr)](https://hitsofcode.com/view/github/objectionary/ddr)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/objectionary/ddr/blob/master/LICENSE.txt)

DDR is an Experimental Dynamic Dispatch Removal Toolkit for [EO](https://www.eolang.org) Programs

### Launch

#### Using Maven
Just add this to your `pom.xml`

```
<dependency>
  <groupId>org.eolang</groupId>
  <artifactId>ddr</artifactId>
  <version>0.0.5</version>
</dependency>
```

And then you'll be able to use the tool like this:
```
CombinerKt.launch("${PATH_TO_YOUR_DIRECTORY}");
```

#### From IntelliJ Idea
You can launch the tool from IntelliJ Idea using MainKt configuration.
Just click `Edit configuration` in the `'Edit run/debug configurations' dialog` and
type an absolute path to your input directory with `.xmir` files in the `Program arguments` field.

After the execution a new directory named `${YOUR_DIRECTORY_NAME}_ddr` will be created near your directory.

#### From console
Use these commands to launch the tool from console
```
$ mvn clean install
$ java -jar target/ddr-1.0-SNAPSHOT-jar-with-dependencies.jar "${PATH_TO_YOUR_DIRECTORY}"
```

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

3) Add a test to [ResolverTest](src/test/kotlin/org/objectionary/ddr/integration/resolver/ResolverTest.kt).
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
