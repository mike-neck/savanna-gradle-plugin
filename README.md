savanna-gradle-plugin
===

A gradle plugin for preventing no tests or skipping tests,
inspired by [kawasima/savanna-maven-plugin](https://github.com/kawasima/savanna-maven-plugin).

Usage
---

Add this plugin to your gradle build file.

```groovy
plugins {
  id 'org.mikeneck.savanna-gradle-plugin' version 'v0.1'
}
```

Lion
---

Lion will come in a case listed bellow.

- The project has no tests.
- User skipped test task(`./gradlew build -x test`).

Example
---

If a user runs build with skipping tests...

```shell-session
$ ./gradlew build -x test
> Task :compileJava
> Task :processResources NO-SOURCE
> Task :classes
> Task :jar
> Task :assemble
> Task :check
> Task :build

> Task :savanna
　　　　 ,、,,,、,,,
　　　 _,,;' '" '' ;;,,
　　（rヽ,;''""''゛゛;,ﾉｒ）　　　　
　　 ,; i ___　、___iヽ゛;,　　テスト書いてないとかお前それ@t_wadaの前でも同じ事言えんの？
　 ,;'''|ヽ・〉〈・ノ |ﾞ ';,
　 ,;''"|　 　▼　　 |ﾞ゛';,
　 ,;''　ヽ ＿人＿  /　,;'_
 ／ｼ、    ヽ  ⌒⌒  /　 ﾘ　＼
|　　 "ｒ,,｀"'''ﾞ´　　,,ﾐ|
|　　 　 ﾘ、　　　　,ﾘ　　 |
|　　i 　゛ｒ、ﾉ,,ｒ" i _ |
|　　｀ー――-----------┴ ⌒´ ）
（ヽ  _____________ ,, ＿´）
 （_⌒_______________ ,, ィ
     T                 |
     |                 |


BUILD SUCCESSFUL in 149ms
3 actionable tasks: 3 executed
```
