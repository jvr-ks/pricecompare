/************************************************
* pricecompare.scala
*
* License GNU GENERAL PUBLIC LICENSE see License.txt
************************************************/
// ü

package de.jvr.pricecompare


import scala.concurrent._
import scala.concurrent.duration._
import scala.xml.XML
import scala.io.Source
import scala.util.Try
import scala.util.control.NonFatal

import akka.actor.{Props, ActorSystem}
import akka.util.Timeout

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.collections.ObservableBuffer

import scalafx.stage.Stage
import scalafx.scene.Scene
 
import scalafx.scene.control._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout._
import scalafx.scene.layout.GridPane

import scalafx.scene.control.TableColumn._
import scalafx.scene.control.TableView

import scalafx.scene.control.Menu
import scalafx.scene.input.KeyCombination
import scalafx.scene.image.Image

import scalafx.geometry.Insets
import scalafx.event.ActionEvent

import scalafx.beans.property.DoubleProperty

import javafx.application.Platform._

import com.typesafe.config.ConfigFactory

import better.files._
import java.io.{File => JFile}

import de.jvr.pricecompare.GuiUpdateActor
import de.jvr.pricecompare.GuiUpdateActor._


object Pricecompare extends JFXApp {

	val version = "0.105"
	
	val wrkDir = """C:\___jvr_work\___workspaces\____scala\pricecompare\src\main\scala\""" // used to shorten displayed lines

	val logger = com.typesafe.scalalogging.Logger("allfilesBetterLogger")
	
	def log_trace(msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
		val p = file.value.toFile.path.toString.replace(wrkDir,"[wrkDir]")
		logger.trace(s"\n$msg line: ${line.value} in: ${p}\n")
	}
	
	def log_debug(msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
		val p = file.value.toFile.path.toString.replace(wrkDir,"[wrkDir]")
		logger.debug(s"\n$msg line: ${line.value} in: ${p}\n")
	}

	def log_info(msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
		val p = file.value.toFile.path.toString.replace(wrkDir,"[wrkDir]")
		logger.info(s"\n$msg line: ${line.value} in: ${p}\n")
	}
	
	def log_warn(msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
		val p = file.value.toFile.path.toString.replace(wrkDir,"[wrkDir]")
		logger.warn(s"\n$msg line: ${line.value} in: ${p}\n")
	}
	
	def log_error(msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
		val p = file.value.toFile.path.toString.replace(wrkDir,"[wrkDir]")
		logger.error(s"\n\n$msg line: ${line.value} in: ${p}\n\n")
	}
	
	val progname = "pricecompare"
	val prognameUpper = "Pricecompare"
	
	var running = false
	
	var autoclose = false
	var sound = true
	var customcss = false
	
	var framePosX = cnf.leftDefault
	var framePosY = cnf.topDefault
	var frameWidth = cnf.widthDefault
	var frameHeight = cnf.heightDefault
	
	val frameBorderX = 5
	
	val rowsMAX = 999
	
	// binding for latest
	var taWidth = DoubleProperty(cnf.widthDefault - frameBorderX * 2)
	var taHeight = DoubleProperty(cnf.heightDefault * 0.2)
	
	var tvWidth = DoubleProperty(cnf.widthDefault - frameBorderX * 2)
	var tvHeight = DoubleProperty(cnf.heightDefault * 0.8)

	val nl = System.getProperty( "line.separator" )
	val fsepa = java.io.File.separator
	
	val configfile = ("""application.conf""").replace("\\","/")
	logger.debug(s"Configfile: $configfile\n")
	
	val pricecompareconfig = ConfigFactory.parseFile(new java.io.File(configfile))
	
	if (pricecompareconfig.hasPath("nosound")){
		if (pricecompareconfig.getString("nosound").toLowerCase.contains("yes")){
			sound = false
			logger.debug("Sound disabled!\n")
		}
	}
	
	if (pricecompareconfig.hasPath("autoclose")){
		if (pricecompareconfig.getString("autoclose").toLowerCase.contains("yes")){
			autoclose = true
			logger.debug("Autoclose!\n")
		}
	}
	

	System.setProperty( "javafx.userAgentStylesheetUrl", "CASPIAN" )
	
	if (pricecompareconfig.hasPath("modena")){
		if (pricecompareconfig.getString("modena").toLowerCase.contains("yes")){
			System.setProperty( "javafx.userAgentStylesheetUrl", "MODENA" )
			logger.debug("Stylesheet MODENA!\n")
		}
	}
	
	if (pricecompareconfig.hasPath("customcss")){
		if (pricecompareconfig.getString("customcss").toLowerCase.contains("yes")){
			customcss = true
			logger.debug("Customcss!\n")
		}
	}
	
	val versionsurl = "https://github.com/jvr-ks/pricecompare/raw/master/version.html"
	
	val updateUrl = "https://github.com/jvr-ks/pricecompare"
	
	val help_online_url = "http://www.jvr.de/" + progname + "/help.html"
	
	var urlFile = "pricecompare_urls.txt"
	
	if (pricecompareconfig.hasPath("test")){
		if (pricecompareconfig.getString("test").toLowerCase.contains("yes")){
			urlFile = "pricecompare_urls_test.txt"
			logger.debug("TEST mode: Using pricecompare_urls_test.txt!\n")
		}
	}
	
	val extractorFile = "priceextractors.txt"
	val alertSoundFile = "alertsound.mp3"
	val guiconfigFile = "guiconfig.xml"
	
	//val pathToUrlFile = FileSystems.getDefault().getPath("." + fsepa, urlFile)
	//val pathToUrlFileExists = Files.exists(pathToUrlFile)
	//val pathToExtractorFile = FileSystems.getDefault().getPath("." + fsepa, extractorFile)
	//val pathToExtractorFileExists = Files.exists(pathToExtractorFile)

//#gui ######################################################################
	// scalafx.collections.ObservableBuffer
	val rows = ObservableBuffer[PricecompareResultRow]()
	
	val pricecompareResultView = new TableView[PricecompareResultRow](rows) {
		columns ++= List(
			new scalafx.scene.control.TableColumn[PricecompareResultRow, String] {
				text = "Nr."
				cellValueFactory = {_.value.index}
				prefWidth = 30
			},
			new scalafx.scene.control.TableColumn[PricecompareResultRow, String]() {
				text = "Url"
				cellValueFactory = {_.value.url}
				prefWidth = 550
			},
				new scalafx.scene.control.TableColumn[PricecompareResultRow, String] {
				text = "List"
				cellValueFactory = {_.value.priceList}
				prefWidth = 60
			},
			new scalafx.scene.control.TableColumn[PricecompareResultRow, String] {
				text = "Web"
				cellValueFactory = {_.value.priceWeb}
				prefWidth = 100
			},
			new scalafx.scene.control.TableColumn[PricecompareResultRow, String] {
				text = "Ok"
				cellValueFactory = {_.value.ok}
				prefWidth = 27
				cellFactory = {
					_: Any => new TableCell[PricecompareResultRow, String] {
						item.onChange { (_, _, newvalue) =>
							var newStyle = ""
							if (newvalue != null && newvalue != "" && newvalue == "!!!") newStyle = "-fx-background-color: red"
							style = newStyle
							text = newvalue
						}
					}
				}
 			}
		)
		padding = Insets(5, 5, 5, 5)
	}
	
	val ta = new TextArea {
		prefHeight <== taHeight
		hgrow = Priority.Always
		vgrow = Priority.Always
		padding = Insets(5, 5, 5, 5)
		text = ""
	}
//#ActorSystem ##############################################################

	implicit val system = ActorSystem("ActorSystem")
	implicit val executionContext = system.dispatcher
	implicit val timeout = Timeout(5 second)
	
	val pricecompareWorkerActor = system.actorOf(Props[WorkerActor](), "pricecompareWorkerActor")
	val pricecompareGuiUpdateActor = system.actorOf(Props[GuiUpdateActor](), "pricecompareGuiUpdateActor")
	val pricecompareReadUrlActor = system.actorOf(Props[ReadUrlActor](), "pricecompareReadUrlActor")
	
//#menuBar ##################################################################
	val menuBar = new scalafx.scene.control.MenuBar {
		useSystemMenuBar = true
		minWidth = frameWidth
	}
	
//#menuCompare #################################################################
	val menuCompare = new Menu( "Compare prices" ){
		items = Seq(
			new MenuItem {
				text = "Start"
				accelerator = KeyCombination.keyCombination("Alt + c")
				onAction = (evt: ActionEvent) => {
					running = true
					compare()
				}
			},
	)}
	
//#menuHelp #################################################################
	val menuHelp = new Menu( "Help" ){
		items = Seq(
			new MenuItem {
				text = "Online-Help"
				accelerator = KeyCombination.keyCombination("Alt + h")
				onAction = (evt: ActionEvent) => {
					openURL( help_online_url )
			}},
			
			new MenuItem {
				text = "Check for updates"
				onAction = (evt: ActionEvent) => {
					pricecompareGuiUpdateActor ! GuiUpdateActor.CHECKVERSION
	}})}
	
//#menuLicense #################################################################
	val menuLicense = new Menu( "License" ){
		items = Seq(
			new MenuItem {
				text = "Show License regarding audiofile"
				onAction = (evt: ActionEvent) => {
					pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_replace(Licenses.audio1)
					pricecompareGuiUpdateActor ! GuiUpdateActor.TA_SCROLLTOTOP
			}},
			
			new MenuItem {
				text = "Show License regarding program"
				onAction = (evt: ActionEvent) => {
					pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_replace(Licenses.gnu)
					pricecompareGuiUpdateActor ! GuiUpdateActor.TA_SCROLLTOTOP
	}})}
	
//#menuQuit #################################################################
	val menuQuit = new Menu( "Quit" ){
		items = Seq(
			new MenuItem {
			text = "Exit program"
			onAction = (evt: ActionEvent) => {
				stopAll()
	}})}
	
	val menus = Array(menuCompare, menuHelp, menuLicense, menuQuit)
	for (i <- menus) menuBar.menus.add( i )
	
//#stage ####################################################################
	stage = new PrimaryStage {
		title = "Pricecompare Version: " + version
		width <= frameWidth
		height <= frameHeight
		x <= framePosX
		y <= framePosY
		
		val gp1 = new GridPane() {
			hgap = 4
			vgap = 6
			margin = Insets(18)
			add(menuBar, 0, 0)
			add(pricecompareResultView, 0, 1)
			add(ta, 0, 2)

			prefWidth = 497.0
			prefHeight = 445.0
		}
		
		logger.debug(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm)
		
		scene = new Scene() {
			root = gp1
			if (customcss) stylesheets = List(getClass.getClassLoader().getResource("pricecompare.css").toExternalForm)
		}

	}

	stage.widthProperty().addListener(_ -> {
		pricecompareGuiUpdateActor ! GuiUpdateActor.SAVEGUIPARAM
	});
	
	stage.heightProperty().addListener(_ -> {
		pricecompareGuiUpdateActor ! GuiUpdateActor.SAVEGUIPARAM
	});
	
	stage.xProperty().addListener(_ -> {
		pricecompareGuiUpdateActor ! GuiUpdateActor.SAVEGUIPARAM
	});

	stage.yProperty().addListener(_ -> {
		pricecompareGuiUpdateActor ! GuiUpdateActor.SAVEGUIPARAM
	});
	
//#work #####################################################################
	stage.delegate.getIcons().add(new Image("pricecompare.png"))
	
	stage.delegate.setOnCloseRequest(new javafx.event.EventHandler[javafx.stage.WindowEvent]() {
			def handle(e: javafx.stage.WindowEvent): Unit = {
				stopAll()
			}
	})
	
	val fileName = "notfound.html"
	val file: File = fileName.toFile
	file.overwrite("<html><body>")(charset = "UTF-8")
	
	//write.over(pwd/"notfound.html", "") //clear
	
	loadGuiConfig(system, pricecompareGuiUpdateActor, progname, guiconfigFile)
	
	checkfile(urlFile)
	checkfile(extractorFile)
	checkfile(alertSoundFile)
	checkfile(guiconfigFile)
	
	
	Thread.sleep(500)
	pricecompareGuiUpdateActor ! GuiUpdateActor.TA_SCROLLTOTOP
	
	
	if (pricecompareconfig.hasPath("autostart")){
		if (pricecompareconfig.getString("autostart").toLowerCase.contains("yes")){
			compare()
		}
	}
	
	
//#def compare #####################################################################
	def compare() = {
		rows.clear()
		for (i <- 0 to rowsMAX) rows += new PricecompareResultRow((i + 1).toString, "", "", "", "")
		
		//write.over(pwd/"notfound.html", "<html><body>") //clear
		val fileName = "notfound.html" //clear
		val file: File = fileName.toFile
		file.overwrite("<html><body>")(charset = "UTF-8")
	
		readUrlFile(urlFile) match {
			case Right(x) => {
				readExtractorFile(extractorFile) match {
					case Right(y) => pricecompareWorkerActor ! WorkerActor.Compare(x, y)
					case Left(y) => pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add(s"\n$y \n")
				}
			}
			case Left(x) => pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("\n" + x + "\n")
		}
	}
	
//#def checkfile #####################################################################
	def checkfile(filename: String) = {
		if (!((filename.toFile).exists)) pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("Error, missing file: " + filename + "\n\n\n")
	}
	
//#stopAll() #####################################################
	def stopAll(): Unit = {
		//#Shutdown
		stage.title = "Shutdown ... bye ..."
		pricecompareReadUrlActor ! akka.actor.PoisonPill
		pricecompareGuiUpdateActor ! akka.actor.PoisonPill
		Await.result(system.terminate(), 20.seconds)
		stage.close()
	}
//#openBrowser() #####################################################
		def openBrowser(url: String) = {
			if (pricecompareconfig.hasPath("openbrowseriferror")){ 
				if (pricecompareconfig.getString("openbrowseriferror").toLowerCase.contains("yes")){
					if ((System.getProperty("os.name").toLowerCase()).contains("win")) {
						logger.debug("Open in browser: " + url + "\n")
						val cmd = scala.sys.process.stringToProcess("cmd /C openbrowser.bat" + " " + url)
						val errorstatus = cmd.!
						if(errorstatus != 0 ) pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("\nExecuting \"openbrowser.bat\" failed!\n\n")
					}
				}
			}
		}
/* 		
//#printCODEC(pricecompareGuiUpdateActor: akka.actor.ActorRef, filename: String, t: String, codec: String, replace: Boolean = true, sendjustsaved: Boolean = true) 
	def printCODEC(pricecompareGuiUpdateActor: akka.actor.ActorRef, filename: String, t: String, codec: String, replace: Boolean = true, sendjustsaved: Boolean = true):Boolean = {

		var r = true
		var msg = ""
		var buffer: BufferedWriter = null
		
		try {
			buffer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(filename), codec))
			def ow(buffer: BufferedWriter) = {
				msg = if (replace) t.replace("\n", sys.props("line.separator")) else t
				buffer.write(msg)
				true
			}
			r = withResources(buffer)(ow)
		} catch {
			case NonFatal(e) => {
				r = false
				val m = s"Cannot save the file $filename: " + e.toString
				pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("\n" + m + "\n")
			}
		}
		r
	}
 */	
//#loadGuiConfig(system: ActorSystem, pricecompareGuiUpdateActor: akka.actor.ActorRef, progname: String, guiconfigFile: String) 
	def loadGuiConfig(system: ActorSystem, pricecompareGuiUpdateActor: akka.actor.ActorRef, progname: String, guiconfigFile: String) = {
		var guiConfigXML = new scala.xml.Elem(null, "root", scala.xml.Null , scala.xml.TopScope, false)

		val guiConfigFileLocal = guiconfigFile
		if (new JFile(guiConfigFileLocal).exists) {
			guiConfigXML = Try(XML.loadFile(guiConfigFileLocal)) getOrElse new scala.xml.Elem(null, "root", scala.xml.Null , scala.xml.TopScope, false)
			val frameWidth = (Try((guiConfigXML \\ "mainframe_width").text) getOrElse cnf.widthDefault.toString).toDouble
			val frameHeight = (Try((guiConfigXML \\ "mainframe_height").text) getOrElse cnf.heightDefault.toString).toDouble
			val framePosX = (Try((guiConfigXML \\ "mainframe_position_x").text) getOrElse cnf.topDefault.toString).toDouble
			val framePosY = (Try((guiConfigXML \\ "mainframe_position_y").text) getOrElse cnf.leftDefault.toString).toDouble
			
			system.scheduler.scheduleOnce(1 seconds, pricecompareGuiUpdateActor, SETWIDTH(frameWidth))
			system.scheduler.scheduleOnce(1 seconds, pricecompareGuiUpdateActor, SETHEIGHT(frameHeight))
			system.scheduler.scheduleOnce(1 seconds, pricecompareGuiUpdateActor, SETX(framePosX))
			system.scheduler.scheduleOnce(1 seconds, pricecompareGuiUpdateActor, SETY(framePosY))
		}
	}

//#saveGuiConfig(pricecompareGuiUpdateActor: akka.actor.ActorRef, stage: Stage, progname: String) 
	def saveGuiConfig(pricecompareGuiUpdateActor: akka.actor.ActorRef, stage: Stage, progname: String) = {
		var guiConfigXML = new scala.xml.Elem(null, "root", scala.xml.Null , scala.xml.TopScope, false)
		
		val guiConfigFile = "guiconfig.xml"

		if (new JFile(guiConfigFile).exists) {
			guiConfigXML = Try(XML.loadFile(guiConfigFile)) getOrElse new scala.xml.Elem(null, "root", scala.xml.Null , scala.xml.TopScope, false)
		}

		val nb = new scala.xml.NodeBuffer
		val oldchilds = guiConfigXML \\ "root" \ "_"

		oldchilds foreach (n => nb += n)

		var rm = oldchilds \\ "mainframe_position_x"
		if (rm.length > 0) rm foreach (n => nb -= n)

		rm = oldchilds \\ "mainframe_position_y"
		if (rm.length > 0) rm foreach (n => nb -= n)

		rm = oldchilds \\ "mainframe_width"
		if (rm.length > 0) rm foreach (n => nb -= n)

		rm = oldchilds \\ "mainframe_height"
		if (rm.length > 0) rm foreach (n => nb -= n)

		nb += <mainframe_position_x>{stage.getX().toDouble}</mainframe_position_x>
		nb += <mainframe_position_y>{stage.getY().toDouble}</mainframe_position_y>
		nb += <mainframe_width>{stage.getWidth().toDouble}</mainframe_width>
		nb += <mainframe_height>{stage.getHeight().toDouble}</mainframe_height>

		val new_guiConfigXML = <root>{nb}</root>

		val prettyPrinter = new scala.xml.PrettyPrinter(120, 2)
		val prettyXml = prettyPrinter.format(new_guiConfigXML)
		//printCODEC(pricecompareGuiUpdateActor, guiConfigFile, prettyXml, "UTF-8", true, false)
		
		val file: File = guiConfigFile.toFile
		file.overwrite(prettyXml)(charset = "UTF-8")

	}

//#openURL(url: String) #####################################################
	def openURL(url: String) = { //#openURL
		try {
			val desktop = java.awt.Desktop.getDesktop()
			desktop.browse(new java.net.URI( url ))
		} catch {
			case _:Throwable => {
					pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("\nError opening browser!\n")
					logger.warn("Error opening browser!\n")
				}
		}
		""
	}
	
//#withResources #####################################################
	//ff. Code from: https://medium.com/@dkomanov/scala-try-with-resources-735baad0fd7d
	def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
		val resource: T = r
		require(resource != null, "resource is null")
		var exception: Throwable = null
		try {
			f(resource)
		} catch {
			case NonFatal(e) =>
			exception = e
			throw e
		} finally {
			closeAndAddSuppressed(exception, resource)
		}
	}
	
//#closeAndAddSuppressed #####################################################
	private def closeAndAddSuppressed(e: Throwable, resource: AutoCloseable): Unit = {
		if (e != null) {
			try {
				resource.close()
			} catch {
				case NonFatal(suppressed) => e.addSuppressed(suppressed)
			}
		} else {
			resource.close()
		}
	}
	
//#showAlert #####################################################
	def showAlert(m: String) = {
		runLater( new Runnable() {
			override def run() = {
				new Alert(AlertType.Error, m){}.showAndWait()
			}
		})
	}
	
//#readUrlFile #####################################################
	def readUrlFile(urlFile: String) = {
		var linesUrl: Either[String, List[String]] = Left("")
		var lines: List[String] = null
		def gl(s: Source) = s.getLines().toList
		try {
			lines = withResources(Source.fromFile(urlFile, "UTF-8"))(gl)
			if (lines.length > 0) linesUrl = Right(lines)
		} catch {
			case NonFatal(e) =>
				s"Problem with URL-file $urlFile !"
				logger.error(e.toString)
		}
		linesUrl
	}

//#readExtractorFile #####################################################
	def readExtractorFile(extractorFile: String) = {
		var mapExtractor: Either[String, Map[String, String]] = Left("")
		
		def doit(s: Source) = s.getLines().toList.map(x => x.split("§")(0) -> x.split("§")(1)).toMap
		try {
			mapExtractor = Right(withResources(Source.fromFile(extractorFile, "UTF-8"))(doit))
		} catch {
			case NonFatal(e) =>
				val m = s"Problem with Extractor-file $extractorFile !"
				pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("\n" + m + "\n")
				logger.error(e.toString)
		}
		mapExtractor
	}
	
//#readURLToList #####################################################
	def readURLToList(url: String, i: Int, codec: String = "UTF-8") = {
		var lines = List.empty[String]
		def gl(s: Source) = s.getLines().toList
		try {
			lines = withResources(Source.fromURL(url, codec))(gl)
		} catch {
			case NonFatal(e) => {
				pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add(s"Nr.: ${i + 1}: Cannot open the URL $url error is:\n" + e.toString + "\n\n")
				logger.debug(e.toString)
			}
		}
		lines
	}
//#safely #####################################################	
	def safely[T](handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
		case ex: scala.util.control.ControlThrowable => throw ex
		// case ex: OutOfMemoryError (Assorted other nasty exceptions you don't want to catch)
		
		//If it's an exception they handle, pass it on
		case ex: Throwable if handler.isDefinedAt(ex) => handler(ex)
		
		// If they didn't handle it, rethrow. This line isn't necessary, just for clarity
		case ex: Throwable => throw ex
	}

}
//#END Pricecompare #############################################################


