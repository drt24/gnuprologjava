cd vanilla
@attrib -R *.*
java -jar ..\..\build\gnuprolog.jar -once gnuprolog.pl validate >..\suite.out 2>..\suite.err
cd ..