/************************************************
* pricecomparereadurlactor.scala
************************************************/
//ü

package de.jvr.pricecompare

import scala.concurrent._


import akka.actor.Actor

import de.jvr.pricecompare.Pricecompare._

import de.jvr.pricecompare.WorkerActor._

//------------------------------- ReadUrlActor -------------------------------
object ReadUrlActor {
	//def props = Props[ReadUrlActor]
	final case class ReadUrl(i: Int, url: String, price: String, remark: String, mapExtractor: Map[String, String], codec: String)
}

//------------------------------- ReadUrlActor -------------------------------
class ReadUrlActor extends Actor{
	import ReadUrlActor._
	
	// First set the default cookie manager.
	java.net.CookieHandler.setDefault(new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL))
	
	def receive = {
		
		case ReadUrl(i, url, price, remark, mapExtractor, codec) => {
	
			if (pricecompareconfig.hasPath("speed")){
				if (pricecompareconfig.getString("speed").toLowerCase.contains("yes")){
					val result = Future {readURL(url, i, codec).mkString}
					result foreach {
						case s => pricecompareWorkerActor ! WorkerActor.ReadUrlResult(i, url, price, remark, mapExtractor, s)
					}
				}
			} else {
				val result = readURL(url, i, codec).mkString
				pricecompareWorkerActor ! ReadUrlResult(i, url, price, remark, mapExtractor, result)
			}
		}
		
		case m => pricecompareGuiUpdateActor ! GuiUpdateActor.Ta_add("ReadUrlActor received unknown command: " + m)
	}
}

