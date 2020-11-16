/************************************************
* pricecompareworkeractor.scala
************************************************/
//ü

package de.jvr.pricecompare


import scala.xml._
import scala.xml.XML
import scala.io.Source
import scala.util.matching._
import scala.util.matching.Regex.Match

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import scala.language.postfixOps
import scala.sys.process._ 
import scala.collection.JavaConverters._

import scala.util.{Try,Success,Failure}
import scala.util.control.NonFatal

import akka.actor.{Props, Actor, ActorRef, ActorSystem, PoisonPill, Terminated, ActorLogging }
import akka.actor.SupervisorStrategy._

import akka.util.Timeout
//import org.log4s._
import akka.pattern.{ ask, pipe }

import akka.pattern.gracefulStop
//import org.log4s._

/* import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.stage.Stage
import scalafx.scene.{Scene, Group, Node}
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color 
import scalafx.scene.control._
import scalafx.scene.layout.{Pane, BorderPane, GridPane}
import scalafx.scene.layout.HBox
import scalafx.scene.layout.VBox
import scalafx.scene.input.MouseEvent
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.scene.control.Menu
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.input.KeyCombination
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView */

import javafx.{geometry => jfxg}
import javafx.application.Platform._

import better.files._
import File._
import java.io.{File => JFile}

//import ammonite.ops._

//import org.log4s._

import de.jvr.pricecompare.Pricecompare._
import de.jvr.pricecompare.GuiUpdateActor._
import de.jvr.pricecompare.ReadUrlActor._

//#object WorkerActor ####################################################
object WorkerActor {
	//def props = Props[WorkerActor]
	
	final case class Compare(x:List[String], y: Map[String, String])
	final case class ReadUrlResult(i: Int, url: String, price: String, remark: String, mapExtractor: Map[String, String], result: String)
}

//#class WorkerActor(magicNumber: Int) ###################################
class WorkerActor extends Actor {
	import WorkerActor._
	
	var comparePosition = 0
	var linecounter = 0
	
	def receive = {
		
		case Compare(linesUrl: List[String], mapExtractor: Map[String, String]) => {
			pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_replace("Comparing, takes a while, please be patient... \n")
			
			var url = ""
			for (i <- comparePosition until linesUrl.length){
				val line = linesUrl(i)
				if (line.length > 1){
					try {
						val ls = line.split(" ")
						url = ls(0)
						val price = ls(1)
						var remark = ""
						if(ls.length > 2){
							for (i <- 2 until ls.length) remark += ls(i) + " "
						}
						var codec = "UTF-8"
						if (remark.contains("cp1252")) codec = "CP1252"
						pricecompareReadUrlActor ! ReadUrlActor.ReadUrl(i, url, price, remark, mapExtractor, codec)
					} catch {
						case e: Throwable => {
							pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("Problem reading URL: " + url +"\n")
							if (sound) pricecompareGuiUpdateActor ! ALERTSOUND
						}
					}
				}
			}
		}
		
		case ReadUrlResult(i, url, price, remark, mapExtractor, result) => {
			val html = result
			log_debug(url + ":\n")
			log_debug("First 100 characters of contents: \n" + html.substring(0, math.min(100, html.length)))
			//log_debug("Complete contents: \n" + html)
			var toSearch = ""
			val domainRex = """www\.(\w+?)\."""
			val urlHasDomain = (domainRex.r("domainName").unanchored).findFirstMatchIn(url)
			if (urlHasDomain.isDefined) {
				val domain = urlHasDomain.get.group("domainName")
				log_debug("domain: " + domain + " \n")
				if (url.contains(domain)) toSearch = mapExtractor(domain)
				log_debug("toSearch: " + toSearch + " \n")
				val pattern = toSearch.r("webprice").unanchored
				val result = pattern.findFirstMatchIn(html)
				val urlfield = if (remark == "") url else url + " (" + remark + ")"
				
				if (result.isDefined) {
					val r = result.get.group("webprice")
					if (r.replace(",",".").toDouble == price.replace(",",".").toDouble){
						pricecompareGuiUpdateActor ! Addrow(i, new PricecompareResultRow((i + 1).toString, urlfield, price, r, "\u2713"))
					} else {
						pricecompareGuiUpdateActor ! Addrow(i, new PricecompareResultRow((i + 1).toString, urlfield, price, r, "!!!"))
						pricecompareGuiUpdateActor ! ALERTSOUND
						Pricecompare.openBrowser(url)
					}
				} else {
					pricecompareGuiUpdateActor ! Addrow(i, new PricecompareResultRow((i + 1).toString, urlfield, price, "not found!", "!!!"))
					
					val fileName = "notfound.html"
					val file: File = fileName.toFile
					file.appendLine().append("<a href=" + urlfield + ">" + urlfield + "</a><br />\n")
					
					//write.append(pwd/"notfound.html", "<a href=" + urlfield + ">" + urlfield + "</a><br />\n")
					pricecompareGuiUpdateActor ! ALERTSOUND
				}
			}
		}

		case e: akka.actor.Status.Failure => pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("Error: "+ e.toString)
		
		case _ => logger.error("WorkerActor received unknown command!")

	}
	
}

//#END WorkerActor #######################################################
//
