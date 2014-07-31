::Must have 7zip installed and included in system path for this to work

@echo off

ECHO *************************** DELETING CLASS FILES ***************************
del click_clack\*.class /s /f /q

ECHO *************************** BUILDING CLASS FILES ***************************
javac -cp "click_clack/." click_clack/*.java -verbose

ECHO *************************** CREATING JAR FILE ******************************
jar cvfm click_clack.jar click_clack/manifest.txt click_clack/*.class click_clack/resources/images/*.* click_clack/resources/sounds/*.*"

ECHO *************************** CREATING EXE FILE ******************************
g++ click_clack/click_clack.cpp click_clack/resources/images/icon.o -static-libgcc -static-libstdc++ -o click_clack

ECHO *************************** ZIPPING IT UP **********************************
7z u click_clack.zip click_clack.exe click_clack.jar

ECHO *************************** DELETING JAR AND EXE ***************************
del click_clack.exe click_clack.jar /s /f /q

ECHO *************************** BUILD COMPLETE *********************************
PAUSE