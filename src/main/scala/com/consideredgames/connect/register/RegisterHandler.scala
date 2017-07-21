package com.consideredgames.connect.register

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.consideredgames.message.MessageMapper
import com.consideredgames.message.Messages.Register
import com.consideredgames.serializers.JsonSupport
import com.typesafe.config.Config

import scala.concurrent.Future

/**
  * Created by matt on 11/06/17.
  */
class RegisterHandler(httpResponseParser: ActorRef, config: Config)
                     (implicit val system: ActorSystem, implicit val materializer: ActorMaterializer)
  extends Actor with JsonSupport {

  import context.dispatcher

  override def receive: Receive = {

    case r: Register =>
      println("in reg")
      register(MessageMapper.toJson(r)).pipeTo(httpResponseParser)(sender())

    case x => println("reg hand fail", x)
  }

  def register(json: String): Future[HttpResponse] = {
    val host = config.getString("app.host")
    val port = config.getInt("app.port")
    val requestEntity = HttpEntity(`application/json`, json)

    Http().singleRequest(
      HttpRequest(
        HttpMethods.POST,
        uri = s"http://$host:$port/register",
        entity = requestEntity
      )
    )
  }
}
