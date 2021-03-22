# Pricecompare  
ScalaFX programm comparing content selected by a regular expression (i.e. product-prices) on webpages with a list in a text-file.  
  
##### Status  
usable.  

##### Requirements  
Java runtime = Java 8  

##### Files required in running directory
All files are UTF-8 (no BOM)  
  
* "pricecompare.exe" (Windows Startfile, portable, no installlation).  
[Download pricecompare.exe from github](https://github.com/jvr-ks/pricecompare/raw/master/pricecompare.exe)   
Viruscheck see below.  
* "openbrowser.bat" (Windows only, batch to open browser, example content   
[Download openbrowser.bat from github](https://github.com/jvr-ks/pricecompare/raw/master/openbrowser.bat)   
* "application.conf" optional configuration file (HOCON).  
[Download application.conf from github](https://github.com/jvr-ks/pricecompare/raw/master/application.conf)    
   
If you don't have your own file(s) already:   
* "pricecompare_urls_test.txt" (containing URLs and prices).  
[Download pricecompare_urls_test.txt from github](https://github.com/jvr-ks/pricecompare/raw/master/pricecompare_urls_test.txt)  
and/or:  
* "pricecompare_urls.txt" (containing URLs and prices).  
[Download pricecompare_urls.txt from github](https://github.com/jvr-ks/pricecompare/raw/master/pricecompare_urls.txt)    
  
* "priceextractors.txt" 
[Download priceextractors.txt from github](https://github.com/jvr-ks/pricecompare/raw/master/priceextractors.txt)  
  
* "alertsound.mp3" (original mp3 file &copy; by Mike Koenig).    
[Download alertsound.mp3 from github](https://github.com/jvr-ks/pricecompare/raw/master/alertsound.mp3)  
   
* "pricecompare.jar" to start on Windows/Linux via Java (without Pricecompare*.exe), "run.sh", "run.bat".   
[Download pricecompare.jar from github](https://github.com/jvr-ks/pricecompare/raw/master/pricecompare.jar)  
  
* "guiconfig.xml" created if not present, containing position and size [XML-format]. 

##### File format:  
All files are UTF-8 (no BOM)  

"pricecompare_urls.txt":  
contains 3 parts on every line splitted by a **blank**.  
 
* The URL.  
* The price.  
* The rest of the line is a remark field.  
* * The remark field can contain codec-information, if other than "UTF8" i.e. "cp1252".  
  
"priceextractors.txt":  
contains 2 parts on every line splitted by a "~".  
  
* The domain-name.
* The extractors regular expression. 
  
* Lines without a "~" character somewhere are treated as a comment.  

##### Config file:  
  
from 0.095:  
* "application.conf" is in running directory (not in .../user.home/...)  
  
has the following configuration parameters:  
  
* nosound : no  
disable sound.  

* autostart : yes  
Starts comparing immediately.  

* speed : yes  
Opens URLs faster (asynchron). 

* test : no   
if entry "yes"the file "pricecompare_urls_test.txt" is used instead of "pricecompare_urls.txt"   

* openbrowseriferror : no  
Opens browser-tabs with changed product pages via openbrowser.bat (not on linux).  
 
* modena : yes 
Use Modena theme (use CASPIAN theme otherwise).  
  
* customcss : no  
Custom theme modifications in "pricecompare.css"    

  
##### Linux:  
* not tested at the moment ...  

##### JDK: 
Java 8, Java 11 not tested 

  
##### Build from source:  
* Needs exe4j a commercial Java jar to exe converter.   
Graalvm native is under construction.  

##### Remarks:  

* Generates file "notfound.html" in working directory (Version 0.07s+)  
* Nothing to install, all portable.  
* The file "priceextractors.txt" contains the extractors regular-expressions in the format  
  
DOMAIN-NAME**~**REGULAR-EXPRESSION.  
  
* All txt-files must be UTF-8 encoded.  
* Directory must be writable by program.  
  
##### Test:  
* [Testfile on server](https://www.jvr.de/pricecompare/pricetest.txt)  
  
###### License: MIT, -> MIT_License.txt  
Copyright (c) 2019/2020/2021 J. v.Roos  





##### Viruscheck at Virustotal 
[Check here](https://www.virustotal.com/gui/url/145061d02b1aad7245f947cc0f42b0eef8a00150efd1031dc57856fc6c49e1b4/detection/u-145061d02b1aad7245f947cc0f42b0eef8a00150efd1031dc57856fc6c49e1b4-1616379341
)  
Use [CTRL] + Click to open in a new window! 
