package com.consideredgames.connect

import akka.Done
import akka.actor.{Actor, ActorRef}
import akka.stream.scaladsl.Sink
import com.consideredgames.game.event.{ConnectAttempt, Connected, Disconnected}
import com.consideredgames.message.Messages.{Message, _}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by matt on 03/05/17.
  */
class MessageHandlerActor(eventHolder: EventHolder,
                          connector: ConnectToServer,
                          registerHandler: ActorRef,
                          loginHandler: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  private var requestHandler: Option[ActorRef] = None

  private def setDisconnected() = {
    eventHolder.pushEvent(Disconnected)
    requestHandler = None
  }

  private def startConnection(message: Message, username: String, sessionId: String) = {
    eventHolder.pushEvent(message)
    val ((requestActor, upgradeResponse), streamEnd) = connector.run(sink, username, sessionId)

    requestHandler = Option(requestActor)

    upgradeResponse.onComplete {
      case Success(_) => println("Upgrade to websocket successful")
        eventHolder.pushEvent(Connected)
      case Failure(e) => println("Upgrade to websocket failed", e)
        setDisconnected()
    }

    streamEnd.onComplete {
      case Success(_) => println("Stream completed successfully")
        setDisconnected()
      case Failure(e) => println("Stream completed bad - probably internet/server down", e)
        setDisconnected()
    }
  }

  def sink: Sink[Message, Future[Done]] = Sink.foreach[Message] { m: Message => handleMessage(m) }

  def handleMessage(message: Message): Unit = {
    message match {

      case LoginResponseSuccess(username, sessionId) => startConnection(message, username, sessionId)
      case RegisterResponseSuccess(username, sessionId) => startConnection(message, username, sessionId)
      case _ => eventHolder.pushEvent(message)
    }
  }

  def sendMessage(message: Message): Unit = {
    message match {

      case r: Register => registerHandler ! r
        eventHolder.pushEvent(ConnectAttempt)
      case l: Login => loginHandler ! l
        eventHolder.pushEvent(ConnectAttempt)

      case _ => requestHandler.foreach {actor => actor ! message}
    }
  }

  override def receive: Receive = {

    case r: Request => sendMessage(r)

    case m: Message => handleMessage(m)

    case x => println("unexpected non-message in message handler", x)
  }
}



