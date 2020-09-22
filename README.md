# Pricecompare  
ScalaFX programm comparing content selected by a regular expression (i.e. product-prices) on webpages with a list in a text-file:  
  
"pricecompare_urls.txt" (UTF-8).    
  
##### Status  
usable.  

##### Requirements  
Java runtime >= Java 8  

##### Special Files  
  
* "pricecompare.exe" (Windows Startfile, portable, no installlation).  
[Download from github](https://github.com/jvr-ks/pricecompare/raw/master/pricecompare.exe)  
Viruscheck see below.  
* "openbrowser.bat" (Windows only, batch to open browser, example content   
* "application.conf" optional configuration file (HOCON).
  
##### Required files in running directory:  
* "pricecompare.exe",  
* "pricecompare_urls.txt" (containing URLs and prices).  

included is the file "pricecompare_urls.test.txt" rename it to "pricecompare_urls.txt" if you don't have your own file already.  
  
* "priceextractors.txt" contains 3 parts on every line splitted by a blank.  
  
* * The URL.  
  
* * The extractors regular expression.  
  
* * The rest of the line is a remark field.  
  
* * * The remark field can contain codec-information, if other than "UTF8" i.e. "cp1252".  
  
* "guiconfig.xml" created if not present, containing position and size [XML-format].  
* "alertsound.mp3" (original mp3 file &copy; by Mike Koenig).  
* "pricecompare.jar" to start on Windows/Linux via Java (without  Pricecompare*.exe), "run.sh", "run.bat".  
  
##### Config file:  
  
from 0.095:  
* "application.conf" is in running directory (not in .../user.home/...)  
  
has the follwing configuration parameters:  
  
* nosound : no  
* autostart : yes  
* speed : yes  
* test : no  
* openbrowseriferror : yes  
  
test : yes -> use "pricecompare_urls_test.txt" instead of "pricecompare_urls.txt"  
  
##### Startparam:  
* "autostart" Starts comparing immediately.  
* "openbrowser" Opens browser-tabs with changed product pages via openbrowser.bat (not on linux).  
* "modena" Modena theme (default ist CASPIAN).  
* "customcss" Custom theme Modifications.  
* "test" takes "pricecompare_urls_test.txt" instead of pricecompare_urls.txt".  
* "nosound".  
* "speed" Only changed entries are shown in the list, opens URLs faster (asynchron).  
  
##### Linux:  
* not supported at the moment ...  

##### JDK:  
Graalvm20
  
##### Build from source:  
* Needs exe4j a commercial Java exee converter.   
Graalvm native is underconstruction.  

##### Remarks:  

* Generates file "notfound.html" in working directory (Version 0.07s+)  
* Nothing to install, all portable.  
* The file "priceextractors.txt" contains the extractors regular-expressions in the format  
  
DOMAIN-NAME**&sect;**REGULAR-EXPRESSION.  
  
* All txt-files must be UTF-8 encoded.  
* Directory must be writable by program.  
  
##### Test:  
* [Testfile on server](https://www.jvr.de/pricecompare/pricetest.txt)  
  
###### License: MIT, -> MIT_License.txt  
Copyright (c) 2019/2020 J. v.Roos  





##### Viruscheck at Virustotal 
[Check here](https://www.virustotal.com/gui/url/145061d02b1aad7245f947cc0f42b0eef8a00150efd1031dc57856fc6c49e1b4/detection/u-145061d02b1aad7245f947cc0f42b0eef8a00150efd1031dc57856fc6c49e1b4-1600771586
)  
Use [CTRL] + Click to open in a new window! 
