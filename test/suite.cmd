cd vanilla
set CLASSPATH=..\classes;%CLASSPATH%
rem set JAVA_COMPILER=xxx
echo n | call ..\run gnuprolog.pl validate >..\suite.out 2>..\suite.err
cd ..