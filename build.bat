@echo off

ECHO *************************** DELETING CLASS FILES ***************************
del click_clack\*.class /s /f /q

ECHO *************************** BUILDING CLASS FILES ***************************
javac -cp "click_clack/." click_clack/*.java -verbose

ECHO *************************** CREATING JAR FILE ******************************
jar cvfm click_clack.jar click_clack/manifest.txt click_clack/*.class click_clack/resources/images/*.* click_clack/resources/sounds/*.*"

ECHO *************************** CREATING EXE FILE ******************************
g++ click_clack/click_clack.cpp click_clack/resources/images/icon.o -static-libgcc -static-libstdc++ -o click_clack