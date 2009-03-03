cd vanilla_stock
@attrib -R *.*
java -jar ..\..\build\gnuprolog.jar -once gnuprologjava.pl validate >..\test.out 2>..\test.err
cd ..
