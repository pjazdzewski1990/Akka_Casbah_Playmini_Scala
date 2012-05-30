package com.sgn.rest
import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.Logging
import akka.actor.Props
import akka.routing.RoundRobinRouter
import akka.actor.PoisonPill

import com.sgn.db._

class PostDispatcher  extends Actor {
  
	val log = Logging(context.system, this)
	
	def receive = {
		case (url :String, m :collection.mutable.Map[String, Any]) =>
			url.split("/")(1) match {
				case "user" => 
				  	//podwojny get, bo some
				  	val id = m.get("id").get.asInstanceOf[Int]
				  	sender ! insertUserToDB(id)	//wyslij wynik operacji bazo-danowej + inf. dokad wyslac odpowiedz 	
				case _ => 
				  println("PostDispatcher: bledna akcja")
				  log.debug("PostDispatcher: bledna akcja")
			}//match
		case x => 
			println("PostDispatcher: dziwny format " + x + "!")
			log.debug("PostDispatcher: dziwny format " + x + "!")
			// powinnismy odsylac obiekt typu mongoable, ktory zasygnalizuje error
			sender ! "PostDispatcher: dziwny format " + x + "!"
	}//receive
	
	//wyciagamy dane z DB
	def insertUserToDB(id:Int) :Int ={
		//tu powinnismy laczyc sie z DB i takie tam
		println("PostDispatcher.insertUserToDB: user")
		log.debug("PostDispatcher.insertUserToDB: user")
		val tmp = User(id)
		var ret = DB.save(tmp)
		ret
	}
}