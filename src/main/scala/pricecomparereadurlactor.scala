/************************************************
* pricecomparereadurlactor.scala
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
import org.log4s._
import akka.pattern.{ ask, pipe }

import akka.pattern.gracefulStop
import org.log4s._

import scalafx.Includes._
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
import scalafx.scene.image.ImageView

import javafx.{geometry => jfxg}
import javafx.application.Platform._

import java.io.File

import org.log4s._

import de.jvr.pricecompare.Pricecompare._
import de.jvr.pricecompare.GuiUpdateActor._
import de.jvr.pricecompare.WorkerActor._

//#object ReadUrlActor ####################################################
object ReadUrlActor {
	def props = Props[ReadUrlActor]
	final case class ReadUrl(i: Int, url: String, price: String, remark: String, mapExtractor: Map[String, String], codec: String)
}

//#class ReadUrlActor(magicNumber: Int) ###################################
class ReadUrlActor extends Actor{
	import ReadUrlActor._
	
	private[this] val logger = getLogger
	
	// First set the default cookie manager.
	java.net.CookieHandler.setDefault(new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL))
	
	def receive = {
		
		case ReadUrl(i, url, price, remark, mapExtractor, codec) => {
	
			if (pricecompareconfig.hasPath("speed")){
				if (pricecompareconfig.getString("speed").toLowerCase.contains("yes")){
					val result = Future {readURLToList(url, i, codec).mkString}
					result foreach {
						case s => pricecompareWorkerActor ! WorkerActor.ReadUrlResult(i, url, price, remark, mapExtractor, s)
					}
				}
			} else {
				val result = readURLToList(url, i, codec).mkString
				pricecompareWorkerActor ! ReadUrlResult(i, url, price, remark, mapExtractor, result)
			}
		}
		
		case msg => logger.error("ReadUrlActor received unknown command: " + msg)

	}
}

//#END ReadUrlActor #######################################################
//
