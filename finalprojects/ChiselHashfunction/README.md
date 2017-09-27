Chisel Hash Function
=======================

You've done the chisel [tutorials](https://github.com/ucb-bar/chisel-tutorial.git), and now you 
are ready to start your own chisel project.  The following procedure should get you started
with a clean running [Chisel3](https://github.com/ucb-bar/chisel3.git) project.

## Make your own Chisel3 project
### How to get started
The first thing you want to do is clone this repo into a directory of your own.  I'd recommend creating a chisel projects directory somewhere
```sh
mkdir ~/ChiselProjects
cd ~/ChiselProjects

git clone https://github.com/ucb-bar/chisel-template.git MyChiselProject
cd MyChiselProject
```
### Make your project into a fresh git repo
There may be more elegant way to do it, but the following works for me. **Note:** this project comes with a magnificent 339 line (at this writing) .gitignore file.
 You may want to edit that first in case we missed something, whack away at it, or start it from scratch.
```sh
rm -rf .git
git init
git add .gitignore *
git commit -m 'Starting MyChiselProject'
```
Connecting this up to github or some other remote host is an exercise left to the reader.
### Did it work?
You should now have a project based on Chisel3 that can be run.  **Note:** With a nod to cargo cult thinking, some believe 
it is best to execute the following sbt before opening up this directory in your IDE. I have no formal proof of this assertion.
So go for it, at the command line in the project root.
```sh
sbt test
```
You should see a whole bunch of output that ends with something like the following lines
```
[warn] one warning found
[info] [0,001] Elaborating design...
Shift divider 0 newBinaryPoint 12
[info] [0,201] Done elaborating.
Total FIRRTL Compile Time: 403,9 ms
Total FIRRTL Compile Time: 60,3 ms
End of dependency graph
Circuit state created
[info] [0,001] SEED 1505317500082
[info] [0,024] hash of (0.0) is         0,0000000000 scala says         0,0000000000
[info] [0,026] hash of (0.5) is         0,0625000000 scala says         0,0625000000
[info] [0,036] hash of (1.0) is         0,1250000000 scala says         0,1250000000
test HardwareHashMaker Success: 0 tests passed in 8 cycles taking 0,059680 seconds
[info] [0,037] RAN 3 CYCLES PASSED
[info] [0,000] Elaborating design...
Shift divider 0 newBinaryPoint 12
[info] [0,019] Done elaborating.
Total FIRRTL Compile Time: 185,2 ms
Total FIRRTL Compile Time: 114,0 ms
End of dependency graph
Circuit state created
[info] [0,000] SEED 1505317501489
[info] [0,024] hash of (0.0,0.0,0.0,0.0) is         0,0000000000 scala says         0,0000000000
[info] [0,050] hash of (0.5,0.5,0.5,0.5) is         1,2500000000 scala says         1,2500000000
[info] [0,058] hash of (1.0,1.0,1.0,1.0) is         2,5000000000 scala says         2,5000000000
test HardwareHashMaker Success: 0 tests passed in 8 cycles taking 0,097032 seconds
[info] [0,058] RAN 3 CYCLES PASSED
[info] [0,000] Elaborating design...
Shift divider 2 newBinaryPoint 38
[info] [0,008] Done elaborating.
Total FIRRTL Compile Time: 78,0 ms
Total FIRRTL Compile Time: 209,1 ms
End of dependency graph
Circuit state created
[info] [0,000] SEED 1505317502106
[info] [0,006] hash of (0.0,0.0,0.0,0.0) is         0,0000000000 scala says         0,0000000000
[info] [0,022] hash of (0.5,0.5,0.5,0.5) is         2,0439218718 scala says         0,5109804680
[info] [0,042] hash of (1.0,1.0,1.0,1.0) is         4,0878437439 scala says         1,0219609361
test HardwareHashMaker Success: 0 tests passed in 8 cycles taking 0,097677 seconds
[info] [0,043] RAN 3 CYCLES PASSED
[info] HashFunctionSpec:
[info] - Trivial example
[info] - A slightly bigger example
[info] - Foutse's example 4.0, 6.2, 4, 5 (w changed to be power of 2)
[info] ScalaTest
[info] Run completed in 3 seconds, 63 milliseconds.
[info] Total number of tests run: 3
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 3, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 3, Failed 0, Errors 0, Passed 3
[success] Total time: 35 s, completed 13 sept. 2017 17:45:02

```
If you see the above then...
### It worked!
You are ready to go. We have a few recommended practices and things to do.
* Use packages and following conventions for [structure](http://www.scala-sbt.org/0.13/docs/Directories.html) and [naming](http://docs.scala-lang.org/style/naming-conventions.html)
* Package names should be clearly reflected in the testing hierarchy
* Build tests for all your work.
 * This template includes a dependency on the Chisel3 IOTesters, this is a reasonable starting point for most tests
 * You can remove this dependency in the build.sbt file if necessary
* Change the name of your project in the build.sbt
* Change your README.md

## Code Explanation:
This function can be recalled repeatedly with different keys.
We made a hardware form and a scala of the hashfunction
both take in a MxN array of doubles, so that we can compare the results.
One restriction made was to only allow w to be a power of 2 so divide can be done easier in hardware. 
There are other approaches but this seemed like a good simplification for a start.
There is still some bug, it works for trivial cases, but there is a problem with the divide code.

