package com.sgn.rest

import play.api.mvc.{Action, AsyncResult}
import play.api.mvc.Results._
import play.api.libs.concurrent._

import scala.util.Random
import annotation.tailrec

import akka.routing.RoundRobinRouter

import akka.actor.{Props, ActorSystem, Actor}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.{Await, Future}
import com.typesafe.play.mini.{POST, Path, GET, Application}
import play.api.data.Form
import play.api.data.Forms._

import com.sgn.db._

/**
 * Inspiration taken from http://en.wikipedia.org/wiki/Infinite_monkey_theorem
 */
object RestServer extends Application {
  println("RestServer is up and running");
  val num = 10

  lazy val system = ActorSystem("ShakespeareGenerator")
  implicit val timeout = Timeout(5000 milliseconds)
  
  val gdip = system.actorOf(
				Props[GetDispatcher].withRouter(RoundRobinRouter(num))/*, name = "gdip"*/)
  val pdip = system.actorOf(
				Props[PostDispatcher].withRouter(RoundRobinRouter(num))/*, name = "pdip"*/)

  def route = {
    case GET(Path("/ping")) => Action { 
		Ok("Alive and kicking @ %s\n".format(System.currentTimeMillis)) 
	}
	case GET(Path(p)) => Action { 
		//odeslij request do PostDispatcher i czekaj na odpowiedz
		AsyncResult {
			println("GET(p :Path) => arg " + p)
			(gdip ? p).mapTo[Mongoable].asPromise.map { result ⇒
				println("GET(p :Path) => data " + result)
				Ok("Data: " + result)
			}
        }
    }
	case POST(Path(p)) => Action { implicit request =>
      AsyncResult {
		println("POST(p :Path) => arg " + p)	
		val post_form = writeForm.bindFromRequest//.get
		println("POST(p :Path) => form " + post_form)
		var data_map = collection.mutable.Map[String, Any]()
		//chyba nie umiem tego dobrze wysłać, bo nie ma parametru
		
		post_form.fold(
			errors => println("BadRequest " + errors),
			{ 
				case (id) => data_map = data_map + ("id" -> id.get)
			}
		)
		
		println(data_map)
        (pdip ? (p, data_map)).mapTo[Int].asPromise.map { result =>
          if(result != -1){
			Ok("Ok: " + result)
		  }else{
			Ok("Not so ok: " + result)
		  }
        }
      }
    }
  }

  val writeForm = Form("id" -> optional(number))
}

//case class Data(data: Map[Any])
case class Result(shakespeareMagic: Set[String], unworthyWords: Set[String])
