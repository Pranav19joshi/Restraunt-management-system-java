@echo off
echo Compiling Restaurant Management System...
if not exist out mkdir out

javac -d out -sourcepath src src\Main.java src\enums\*.java src\interfaces\*.java src\exception\*.java src\model\*.java src\repository\*.java src\concurrent\*.java src\service\*.java src\ui\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful. Launching...
java -cp out Main
pause
