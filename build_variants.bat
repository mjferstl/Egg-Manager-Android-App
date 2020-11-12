@echo off

gradlew assemble
pause

echo ----------------
echo Cleaning project
echo ----------------
gradlew clean
echo project cleaned up
echo 