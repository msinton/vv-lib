package com.consideredgames.connect

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.consideredgames.connect.register.{LoginHandler, RegisterHandler}
import com.consideredgames.game.event.Event
import com.consideredgames.message.Messages.Message

/**
  * Created by matt on 01/07/17.
  */
trait PopsEvent {
  def popEvent: Option[Event]
}

class MessageHandler(messageHandlerActor: ActorRef, messageHolder: EventHolder) extends PopsEvent {

  def sendMessage(message: Message): Unit = {
    messageHandlerActor ! message
  }

  def popEvent: Option[Event] = messageHolder.popEvent
}

object MessageHandler {

  def apply(): MessageHandler = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val httpResponseParser = system.actorOf(Props(new HttpResponseParser()), "http-response-parser")
    val registerHandler = system.actorOf(Props(new RegisterHandler(httpResponseParser, system.settings.config)), "register-handler")
    val loginHandler = system.actorOf(Props(new LoginHandler(httpResponseParser, system.settings.config)), "login-handler")

    val messageHolder = new EventHolder()

    val messageHandlerActor: ActorRef = system.actorOf(Props(new MessageHandlerActor(
      messageHolder,
      new ConnectToServer(),
      registerHandler,
      loginHandler
    )))

    new MessageHandler(messageHandlerActor, messageHolder)
  }
}