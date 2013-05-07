Transiterator
=============

LNB Transliterator, version 2

Basicaly intended for including as library. For testing and experimenting primitive CLI is provided. To run it, compile project with Ant build.xml and run "dist/run.bat" (on Windows) or TransliteratorCLI.main() from the directory where path.conf file is. Bigger tests for exploring possibilities of given rule sets will available in "dist/testdata" and can be launched by "dist/run.bat". These tests my require several minutes to complite and 1-2GB RAM.

Build.xml assumes that https://github.com/PeterisP/morphology is in the directory right next to Transliterator.