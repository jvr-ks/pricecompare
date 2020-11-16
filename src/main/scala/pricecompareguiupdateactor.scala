/************************************************
* pricecompareguiupdateactor.scala
************************************************/
//ü

package de.jvr.pricecompare

import scala.xml._
import scala.xml.XML
import scala.io.Source
import scala.util.matching._
import scala.util.matching.Regex.Match
import scala.util.Try
import scala.util.Success
import scala.util.Failure

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import scala.language.postfixOps
import scala.sys.process._ 
import scala.collection.JavaConverters._
import scala.util.Try

import akka.actor.{Props, Actor, ActorRef, ActorSystem, PoisonPill, Terminated, ActorLogging }
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import akka.util.Timeout
//import org.log4s._

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.stage.Stage
import scalafx.scene.{Scene, Group, Node}
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color 
import scalafx.scene.control._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.{Pane, BorderPane, GridPane}
import scalafx.scene.layout.HBox
import scalafx.scene.layout.VBox
import scalafx.scene.input.MouseEvent
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.scene.control.Menu
import scalafx.scene.input.KeyCombination
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView

import better.files._
import File._
import java.io.{File => JFile}

import java.text.SimpleDateFormat

import javafx.application.Platform._
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer

//import org.log4s._

import de.jvr.pricecompare.Pricecompare._

//#object GuiUpdateActor ####################################################
object GuiUpdateActor {
	//def props = Props[GuiUpdateActor]
	
	final case class Ta_replace(s: String)
	final case class Ta_add(s: String)
	final case object TA_SCROLLTOTOP
	final case object TA_SCROLLTOBOTTOM
	
	final case class SETWIDTH(n: Double)
	final case class SETHEIGHT(n: Double)
	final case class SETX(n: Double)
	final case class SETY(n: Double)
	
	final case class Addrow(i: Int, row: PricecompareResultRow)

	final case object CHECKVERSION
	final case object SHUTDOWN
	final case object ALERTSOUND

	final case object SAVEGUIPARAM
}


//#class GuiUpdateActor ###################################
class GuiUpdateActor extends Actor {
	import GuiUpdateActor._
	
	def onFX(body : =>Unit) = {
		runLater( new Runnable() {
			override def run() = body
		})
	}
	
	var justSaved = List[String]("")

	def receive = {
		case Ta_replace( t: String ) => {
			onFX({
				ta.text = t
			})
		}
		
		case Ta_add( t: String ) => {
			onFX({
				ta.text = ta.text.get() + t
				ta.delegate.positionCaret( ta.text.get().length )
			})
		}
		
		case TA_SCROLLTOTOP => {
			onFX({
				ta.positionCaret(0)
			})
		}
		
		case TA_SCROLLTOBOTTOM => {
			onFX({
				ta.positionCaret(ta.getText().length())
			})
		}
		
		case CHECKVERSION => {
			onFX({
				try {
					val html = Source.fromURL( versionsurl ).mkString
					
					var toSearch = """<li>""" + prognameUpper + """ (\d*.\d*)</li>"""
					
					val pattern = toSearch.r("onlineVersion").unanchored
					
					val result = pattern.findFirstMatchIn(html)
					
					if (result.isDefined) {
						val r = result.get.group("onlineVersion")

						if (r.toDouble > version.toDouble){
							val alert = new Alert(AlertType.Confirmation) {
							  initOwner(stage)
							  title = "Confirmation Dialog"
							  headerText = "Newer version available, visit downloadpage?"
							  contentText = ""
							}

							val result = alert.showAndWait()

							result match {
							  case Some(ButtonType.OK) => openURL(updateUrl)
							  case _                   => 
							}
						} else {
							pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_replace("Your program version is up to date!")
						}
					} else {
						pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_replace("Problem reading online version-info!")
					}
				} catch {
					case e:Throwable => new Alert(AlertType.Error) {
						initOwner(stage)
						title = "Error occured"
						headerText = "Get version info is not possible!"
						contentText = e.toString
					}.showAndWait()
				}
			})
		}

		case SAVEGUIPARAM => {
			saveGuiConfig(pricecompareGuiUpdateActor, stage, progname)
		}


		case SETWIDTH(n: Double) => {
			onFX({
				stage.setWidth(n)
			})
		}

		case SETHEIGHT(n: Double) => {
			onFX({
				stage.setHeight(n)
			})
		}

		case SETX(n: Double) => {
			onFX({
				stage.setX(n)
			})
		}

		case SETY(n: Double) => {
			onFX({
				stage.setY(n)
			})
		}

		case SHUTDOWN => {
			stopAll()
		}

		case ALERTSOUND => {
			if(sound) {
				try {
					var mediaPlayer = new MediaPlayer(new Media(new JFile("alertsound.mp3").toURI().toString()))
					mediaPlayer.play()
					Thread.sleep(5000)
					mediaPlayer.stop()
					mediaPlayer = null
				} catch {
					case e:Throwable => java.awt.Toolkit.getDefaultToolkit().beep()
				}
			}
		}
		
		case Addrow(i: Int, row: PricecompareResultRow) => {
			onFX({
				//rows += row
				rows.remove(i, 1)
				rows.insert(i, row)
				pricecompareResultView.scrollTo(i)
			})
		}
		
		case _ => ;

	}
}




