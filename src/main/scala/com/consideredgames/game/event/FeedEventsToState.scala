package com.consideredgames.game.event

import com.consideredgames.connect.PopsEvent
import com.consideredgames.game.event.process.EventProcessor
import com.consideredgames.game.state.State

/**
  * Created by matt on 20/07/17.
  */
class FeedEventsToState(events: PopsEvent, var state: State) {

  def run(event: Event): State = {
    state = EventProcessor.run(event, state)
    state
  }
  
  def run(): State = {
    events.popEvent.map(EventProcessor.run(_, state)).fold(state) { newState =>
      state = newState
      state
    }
  }

}
