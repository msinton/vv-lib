package integration.connect

import com.consideredgames.connect.MessageHandler
import com.consideredgames.game.event.FeedEventsToState
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.state.State
import com.consideredgames.message.Messages._
import org.scalatest.FunSuite


/**
  * Created by matt on 08/05/17.
  */
class InitialConnectionAndSocketTest extends FunSuite {

  // Server should be running

  val password = "29XxCKUHU6yaBxjrD4pb3ICFB1cSFGGLg+Fw6krRk07YbQMPZQtm5yIpuLG0HRJz"

//  test("Register and connect to server") {
//
//    val messageHandler = MessageHandler()
//
//    val i = new Random().nextInt()
//    messageHandler.sendMessage(Register(s"bob$i", password, s"email$i@email.com"))
//
//    while(true) {
//      messageHandler.popEvent.foreach(s => println(s"popped: $s"))
//      Thread.sleep(500)
//    }
//    assert(true)
//  }

  test("Login and connect to server and join game") {

    val messageHandler = MessageHandler()

    val i = 1
    val feedEventsToState = new FeedEventsToState(messageHandler, State())

    messageHandler.sendMessage(Login(s"bob$i", password, s"email$i@email.com"))

    while(!feedEventsToState.run().connectivity.connected) {
      Thread.sleep(500)
    }

    println("-1-", feedEventsToState.state)
    messageHandler.sendMessage(Join(myColour = Blue))
    while(feedEventsToState.run().activity.joinedGames.isEmpty) {
      Thread.sleep(500)
    }

    println("-2-", feedEventsToState.state)
    messageHandler.sendMessage(Logout())
    while(feedEventsToState.run().connectivity.connected) {
      Thread.sleep(500)
    }

//    TODO Stream completed successfully -> add event to set disconnected
    assert(true)
  }

  test("Login and connect to server and create private game") {

    val messageHandler = MessageHandler()
    val feedEventsToState = new FeedEventsToState(messageHandler, State())

    val i = 1
    messageHandler.sendMessage(Login(s"bob$i", password, s"email$i@email.com"))

    while(!feedEventsToState.run().connectivity.connected) {
      Thread.sleep(500)
    }

    val gameOptions = NewGameOptions(privateGame = true)
    messageHandler.sendMessage(NewGameRequest(myColour = Blue, newGameOptions = Option(gameOptions)))
    while(feedEventsToState.run().activity.joinedGames.isEmpty) {
      Thread.sleep(500)
    }

    messageHandler.popEvent.foreach(s => println(s"popped: $s"))
    messageHandler.popEvent.foreach(s => println(s"popped: $s"))

    // TODO join 2 more players and get activeGameId
    // Also test other scenarios - try to join gameId - when exists or does not exist
    // try joins in quick succession - to ensure new games spawned correctly

    messageHandler.sendMessage(Logout())
    while(feedEventsToState.run().connectivity.connected) {
      Thread.sleep(500)
    }

    assert(true)
  }
}
