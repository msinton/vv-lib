package com.consideredgames.connect

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.consideredgames.message.MessageMapper
import com.consideredgames.message.Messages.Message

import scala.concurrent.Future

/**
  * Created by matt on 19/04/17.
  */
class ConnectToServer()(implicit val system: ActorSystem, implicit val materializer: ActorMaterializer) {

  private val source = Source.actorRef[Message](bufferSize = 5, OverflowStrategy.dropNew)

  private def webSocketFlow(username: String, sessionId: String) = Http().webSocketClientFlow(
    WebSocketRequest(s"ws://localhost:8080/?username=$username&sessionId=$sessionId"
  )).collect {
    case TextMessage.Strict(msg) => MessageMapper.deJsonify(msg)
  }

  def run(messageSink: Sink[Message, Future[Done]],
          username: String,
          sessionId: String): ((ActorRef, Future[WebSocketUpgradeResponse]), Future[Done]) = {

    source.map(msg => TextMessage(MessageMapper.toJson(msg)))
      .viaMat(webSocketFlow(username, sessionId))(Keep.both)
      .toMat(messageSink)(Keep.both)
      .run()
  }
}
