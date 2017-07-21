package com.consideredgames.connect

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.consideredgames.message.Messages.Message
import com.consideredgames.serializers.JsonSupport

/**
  * Created by matt on 27/06/17.
  */
class HttpResponseParser(implicit val system: ActorSystem, implicit val mat: ActorMaterializer)
  extends Actor with JsonSupport {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {

    case HttpResponse(_: StatusCodes.Success, headers, entity: HttpEntity.Strict, _) =>
      println("http res parser", entity, headers)
      Unmarshal(entity).to[Message] pipeTo sender()

    case HttpResponse(StatusCodes.ServiceUnavailable, headers, entity, _) =>
      println("server down - try again later", entity, headers)

    case HttpResponse(status, headers, entity, _) =>
      println("bad status", status, entity, headers)
  }
}
