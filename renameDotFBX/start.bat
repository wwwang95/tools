@ECHO OFF
chcp 936

goto start

:start
java -jar RenameDotFBX.jar

set /p continue=是否继续(y/n):
if "%continue%" == "y" (
    goto start
) else (
    exit
)
