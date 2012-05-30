package com.sgn.rest
import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.Logging
import akka.actor.Props
import com.typesafe.play.mini.{POST, Path, GET, Application}

import com.sgn.db._

class GetDispatcher extends Actor {
  
	val num = 3
	val log = Logging(context.system, this)
	
	def receive = {
		case url :String =>
			url.split("/")(1) match {
				case "user" => 
				  	println("GetDispatcher: user " + url.split("/")(2))
				  	log.debug("GetDispatcher: user " + url.split("/")(2))
				  	
				  	val id = (url.split("/")(2)).toInt
				  	println("Zwracam " + id)
					sender ! getUserFromDB(id)	//wyslij wynik operacji bazo-danowej
				case x => 
				  	println("GetDispatcher: bledny kontroler " + x + "!")
				  	log.debug("GetDispatcher: bledny kontroler " + x + "!")
					sender ! "GetDispatcher: bledny kontroler " + x + "!"
			}//match
		case x => 
			println("GetDispatcher: nie Path " + x + "!")
			log.debug("GetDispatcher: nie Path " + x + "!")
			sender ! "GetDispatcher: nie Path " + x + "!"
	}
	
	//wyciagamy dane z DB
	def getUserFromDB(id:Int) :User ={
		println("GetDispatcher.getUserFromDB: user-" + id)
		log.debug("GetDispatcher.getUserFromDB: user-" + id)
		val search_for = Map("id"->id)
		val found = DB.get(search_for)
		if(found.size > 0){
			found(0).asInstanceOf[User]
		}else{
			User(-1)
		}
	}
}