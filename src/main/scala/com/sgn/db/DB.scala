package com.sgn.db

import com.mongodb.casbah.Imports._
//import com.mongodb.casbah._
import Control._
import scala.collection.mutable.ListBuffer
//import scalaj.collection.Imports._

case class User(id: Int = -1) extends Mongoable {
	def this() = this(-1)
}

trait Mongoable{
  
	def toMongo():MongoDBObject = {
		val builder = MongoDBObject.newBuilder
		getFields(this).foreach( (field)=>
			builder+=field._1 -> field._2
		)
		val newObj = builder.result
		newObj
	}
	
	//zwraca wszystkie poal klasy jako mape: (nazwa, wartosc)
	private def getFields(o: Any): Map[String, Any] = {
		val fieldsAsPairs = for (field <- o.getClass.getDeclaredFields) yield {
			val access = field.isAccessible()
			field.setAccessible(true)
			val pair = (field.getName, field.get(o)) 
			field.setAccessible(access)
			pair
		}
		Map(fieldsAsPairs :_*)
	}
}

object Mongoable{
	//to powinno byc statyczne, ale companion object dla trait'a?
	// a moze implicit cast?
	def fromMongo(mongo :MongoDBObject, class_type :Class[_ <: Mongoable]): Mongoable = {
		var obj :Mongoable = class_type.newInstance() 
		
		for (field <- class_type.getDeclaredFields) {
			val access = field.isAccessible()
			field.setAccessible(true)
			//zwaraca some, wiec trzeba zrobic jeszcze get
			field.set(obj, mongo.get(field.getName).get)
			field.setAccessible(access)
		}
		
		obj 
	}
}

object DB {
	def save(obj: Mongoable) :Int = {
		try { 
			//zamien obiekt scali na obiekt mongoDB(casbaha) 
			val mongoObj = obj.toMongo()
			using(MongoFactory.getConnection) { conn =>
				val collection = MongoFactory.getCollection(conn)
				collection+=mongoObj
				collection
			}
		} catch {
			case e: Exception => return -1
		}
		return 1//?
	}
	
	def get(obj: Map[String,Any]) :List[Mongoable] = {
		var result :MongoCursor = null
		//skonstruuj obiekt "wzorcowy" szukania
		val builder = MongoDBObject.newBuilder
		obj.foreach( (field)=>
			builder+=field._1 -> field._2
		)
		val newObj = builder.result
		
		var found = List[Mongoable]()
		using(MongoFactory.getConnection) { conn =>
		  	val collection = MongoFactory.getCollection(conn)
			result = collection.find(newObj)
			for(f_id <- 0 until result.count) {
				found = Mongoable.fromMongo(result.next(), classOf[User]) ::  found
			}
			collection
		}
		found
	}
}

object MongoFactory {
  
  private val SERVER     = "localhost"
  private val PORT       = 27017
  private val DATABASE   = "db"
  private val COLLECTION = "col"

  def getConnection: MongoConnection = {
    return MongoConnection(SERVER, PORT) 
  }

  def getCollection(conn: MongoConnection): MongoCollection = {
    return conn(DATABASE)(COLLECTION)
  }

  def closeConnection(conn: MongoConnection) {
    conn.close
  }

}

object Control {
  
  def using[A <: { def close(): Unit }, B](param: A)(f: A => B): B =
    try {
      f(param)
    } finally {
      param.close()
    }

  def bmap[T](test: => Boolean)(block: => T): List[T] = {
    val ret = new ListBuffer[T]
    while (test) ret += block
    ret.toList
  }
}
