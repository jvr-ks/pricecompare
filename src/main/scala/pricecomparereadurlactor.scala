/************************************************
* pricecomparereadurlactor.scala
************************************************/
//ü

package de.jvr.pricecompare

import scala.concurrent._


import akka.actor.Actor

import de.jvr.pricecompare.Pricecompare._

import de.jvr.pricecompare.WorkerActor._

//#object ReadUrlActor ####################################################
object ReadUrlActor {
	//def props = Props[ReadUrlActor]
	final case class ReadUrl(i: Int, url: String, price: String, remark: String, mapExtractor: Map[String, String], codec: String)
}

//#class ReadUrlActor(magicNumber: Int) ###################################
class ReadUrlActor extends Actor{
	import ReadUrlActor._
	
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
		
		case msg => log_error("ReadUrlActor received unknown command: " + msg)

	}
}

//#END ReadUrlActor #######################################################
//
