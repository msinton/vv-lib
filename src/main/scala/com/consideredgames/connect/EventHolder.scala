package com.consideredgames.connect

import com.consideredgames.game.event.Event

import scala.collection.immutable.Queue

/**
  * Created by matt on 01/07/17.
  */
class EventHolder {
  private var events: Queue[Event] = Queue.empty

  def pushEvent(e: Event): Unit = {
    events = events.enqueue(e)
  }

  def popEvent: Option[Event] = {
    events.dequeueOption.map {
      case (h: Event, t: Queue[Event]) =>
        events = t
        h
    }
  }
}
