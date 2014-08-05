@echo off

ECHO *************************** DELETING CLASS FILES ***************************
del click_clack\*.class /s /f /q
del click_clack.exe
del click_clack.jar
del click_clack.zip

ECHO *************************** BUILDING CLASS FILES ***************************
javac -cp "click_clack/." click_clack/*.java -verbose
pause

ECHO *************************** RUNNING GAME ******************************
java click_clack.Window