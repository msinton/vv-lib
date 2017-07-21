package com.consideredgames.connect.register

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.consideredgames.message.MessageMapper
import com.consideredgames.message.Messages.Login
import com.consideredgames.serializers.JsonSupport
import com.typesafe.config.Config

import scala.concurrent.Future

/**
  * Created by matt on 11/06/17.
  */
class LoginHandler(httpResponseParser: ActorRef, config: Config)
                  (implicit val system: ActorSystem, implicit val materializer: ActorMaterializer)
  extends Actor with JsonSupport {

  import context.dispatcher

  override def receive: Receive = {

    case r: Login =>
      println("in login")
      login(MessageMapper.toJson(r)).pipeTo(httpResponseParser)(sender())

    case x => println("login handler fail", x)
  }

  def login(json: String): Future[HttpResponse] = {
    val host = config.getString("app.host")
    val port = config.getInt("app.port")
    val requestEntity = HttpEntity(`application/json`, json)

    Http().singleRequest(
      HttpRequest(
        HttpMethods.POST,
        uri = s"http://$host:$port/login",
        entity = requestEntity
      )
    )
  }
}
