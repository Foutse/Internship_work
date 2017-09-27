Project square distance
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
g++  -I.  -MMD -I/usr/local/share/verilator/include -I/usr/local/share/verilator/include/vltstd -DVL_PRINTF=printf -DVM_TRACE=1 -DVM_COVERAGE=0 -Wno-char-subscripts -Wno-parentheses-equality -Wno-sign-compare -Wno-uninitialized -Wno-unused-but-set-variable -Wno-unused-parameter -Wno-unused-variable     -Wno-undefined-bool-conversion -O1 -DTOP_TYPE=VSqDist -DVL_USER_FINISH -include VSqDist.h   -c -o VSqDist__ALLsup.o VSqDist__ALLsup.cpp
g++  -I.  -MMD -I/usr/local/share/verilator/include -I/usr/local/share/verilator/include/vltstd -DVL_PRINTF=printf -DVM_TRACE=1 -DVM_COVERAGE=0 -Wno-char-subscripts -Wno-parentheses-equality -Wno-sign-compare -Wno-uninitialized -Wno-unused-but-set-variable -Wno-unused-parameter -Wno-unused-variable     -Wno-undefined-bool-conversion -O1 -DTOP_TYPE=VSqDist -DVL_USER_FINISH -include VSqDist.h   -c -o VSqDist__ALLcls.o VSqDist__ALLcls.cpp
      Archiving VSqDist__ALL.a ...
ar r VSqDist__ALL.a VSqDist__ALLcls.o VSqDist__ALLsup.o
ar: création de VSqDist__ALL.a
ranlib VSqDist__ALL.a
g++    SqDist-harness.o verilated.o verilated_vcd_c.o VSqDist__ALL.a    -o VSqDist -lm -lstdc++  2>&1 | c++filt
make: Leaving directory '/users/yuehgoh/ChiselWork/squareDistancechisel/test_run_dir/squaredistance.SquareDistanceSpec2022490972'
sim start on travo.lipn.univ-paris13.fr at Wed Sep 13 12:14:18 2017
inChannelName: 00022633.in
outChannelName: 00022633.out
cmdChannelName: 00022633.cmd
STARTING test_run_dir/squaredistance.SquareDistanceSpec2022490972/VSqDist
[info] [0,000] SEED 1505297653439
[info] [0,130]  0 hardware 24,05845642    software 24,05845153    error 0,000020 %
[info] [0,130]  0 Elapsed timeH = 740815 and timeS = 93852 
[info] [0,208]  1 hardware 27,62966919    software 27,62962262    error 0,000169 %
[info] [0,209]  1 Elapsed timeH = 74205 and timeS = 91003 
[info] [0,302]  2 hardware 27,67158508    software 27,67157518    error 0,000036 %
[info] [0,302]  2 Elapsed timeH = 94805 and timeS = 86451 
[info] [0,407]  3 hardware 26,22372437    software 26,22388036    error 0,000595 %
[info] [0,407]  3 Elapsed timeH = 74072 and timeS = 87273 
[info] [0,535]  4 hardware 23,87585449    software 23,87587920    error 0,000103 %
[info] [0,535]  4 Elapsed timeH = 73294 and timeS = 86494 
[info] [0,604]  5 hardware 27,70588684    software 27,70594323    error 0,000204 %
[info] [0,604]  5 Elapsed timeH = 67115 and timeS = 85847 
[info] [0,689]  6 hardware 24,76515198    software 24,76510261    error 0,000199 %
[info] [0,690]  6 Elapsed timeH = 67441 and timeS = 86196 
[info] [0,777]  7 hardware 20,43077087    software 20,43085891    error 0,000431 %
[info] [0,777]  7 Elapsed timeH = 67669 and timeS = 86450 
[info] [0,855]  8 hardware 23,99240112    software 23,99243435    error 0,000138 %
[info] [0,855]  8 Elapsed timeH = 67975 and timeS = 86557 
[info] [0,930]  9 hardware 27,84428406    software 27,84424843    error 0,000128 %
[info] [0,930]  9 Elapsed timeH = 69301 and timeS = 85657 
[info] [1,023] 10 hardware 28,13565063    software 28,13566230    error 0,000041 %
[info] [1,023] 10 Elapsed timeH = 93401 and timeS = 86360 
[info] [1,078] 11 hardware 27,06149292    software 27,06149089    error 0,000008 %
[info] [1,078] 11 Elapsed timeH = 81251 and timeS = 86771 
[info] [1,140] 12 hardware 21,88769531    software 21,88781999    error 0,000570 %
[info] [1,141] 12 Elapsed timeH = 63352 and timeS = 86999 
[info] [1,210] 13 hardware 24,34025574    software 24,34030655    error 0,000209 %
[info] [1,210] 13 Elapsed timeH = 64480 and timeS = 90775 
[info] [1,241] 14 hardware 20,70016479    software 20,70017909    error 0,000069 %
[info] [1,241] 14 Elapsed timeH = 59698 and timeS = 91389 
[info] [1,333] 15 hardware 23,14614868    software 23,14622510    error 0,000330 %
[info] [1,333] 15 Elapsed timeH = 62265 and timeS = 86945 
[info] [1,449] 16 hardware 26,07984924    software 26,07988960    error 0,000155 %
[info] [1,449] 16 Elapsed timeH = 60939 and timeS = 86358 
[info] [1,557] 17 hardware 22,09533691    software 22,09538643    error 0,000224 %
[info] [1,557] 17 Elapsed timeH = 60408 and timeS = 86397 
[info] [1,639] 18 hardware 31,16087341    software 31,16096421    error 0,000291 %
[info] [1,639] 18 Elapsed timeH = 59181 and timeS = 85929 
[info] [1,781] 19 hardware 25,86264038    software 25,86258733    error 0,000205 %
[info] [1,781] 19 Elapsed timeH = 62559 and timeS = 87533 
[info] [1,837] 20 hardware 26,91731262    software 26,91742843    error 0,000430 %
[info] [1,838] 20 Elapsed timeH = 60063 and timeS = 85437 
Enabling waves..
Exit Code: 0
[info] [1,864] RAN 21 CYCLES PASSED
[info] SquareDistanceSpec:
[info] SqDist
[info] - should calculate proper square distance of two vectors (with firrtl)
[info] SqDist
[info] - should calculate proper square distance of two vectors (with verilator)
[info] ScalaTest
[info] Run completed in 11 seconds, 369 milliseconds.
[info] Total number of tests run: 2
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 2, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 2, Failed 0, Errors 0, Passed 2
[success] Total time: 21 s, completed 13 sept. 2017 12:14:20

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

## Code explanations
In this code we do two kind of simulations just to test the functionality of our code:
-one with C++ verilator which is what is presented here. This was done by setting the backend to "verilator". verilator simulations are about 6 times faster than the interpreter.
-the other without the verilator, i.e the default scala interpreter.
Both are all much slower than hardware. An FPGA could easily be 1.000 or 10.000 times faster than simulation.

We did a test with 20 times trials with vectors of sizes 150 and we calculated the computation time for both the hardware and the software and we compared to observe that the hardware gradually becames faster.
