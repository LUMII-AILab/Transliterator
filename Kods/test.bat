mkdir res-testdata
mkdir res-ord-testdata

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aarste6-1kol.txt -file -F -Fraktur_Double
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aarste6-1kol.txt -file -F -Fraktur_Double -order
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aarste6-2kol.txt -file -F -Fraktur_Double

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aviize1861-1kol.txt -file -F -Fraktur_Double
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aviize1861-1kol.txt -file -F -Fraktur_Double -order
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aviize1861-2kol.txt -file -F -Fraktur_Double

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aviize1918-1kol.txt -file -F -Fraktur
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aviize1918-1kol.txt -file -F -Fraktur -order
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/aviize1918-2kol.txt -file -F -Fraktur

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Laachpleesis-1dz-1kol.txt -file -F -Latin
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Laachpleesis-1dz-1kol.txt -file -F -Latin -order
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Laachpleesis-1dz-2kol.txt -file -F -Latin

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Laachpleesis-2dz-1kol.txt -file -F -Latin
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Laachpleesis-2dz-1kol.txt -file -F -Latin -order
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Laachpleesis-2dz-2kol.txt -file -F -Latin

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Meernieki-1nod-1kol.txt -file -F -Fraktur
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Meernieki-1nod-1kol.txt -file -F -Fraktur -order
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Meernieki-1nod-2kol.txt -file -F -Fraktur

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Meernieki-2nod-1kol.txt -file -F -Fraktur
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Meernieki-2nod-1kol.txt -file -F -Fraktur -order
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/Meernieki-2nod-2kol.txt -file -F -Fraktur

java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/kraupeens.txt -file -F -Fraktur
java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/kraupeens.txt -file -F -Fraktur -order

::These tests shall take a looooong time.
::java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/gadagraamata1797.txt -file -F -Fraktur_Double
::java -Xmx1024m -cp ".;./res/morpho/morphology.jar" TransliteratorCLI testdata/gadagraamata1797.txt -file -F -Fraktur_Double -order

pause
