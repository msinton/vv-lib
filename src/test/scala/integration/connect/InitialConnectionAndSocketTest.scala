package integration.connect

import com.consideredgames.connect.MessageHandler
import com.consideredgames.game.event.{FeedEventsToState, StartGame}
import com.consideredgames.game.model.player.PlayerColours._
import com.consideredgames.game.state.State
import com.consideredgames.message.Messages._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}

import scala.util.Random

/**
  * Created by matt on 08/05/17.
  */
class InitialConnectionAndSocketTest extends FunSuite with Matchers with Eventually with BeforeAndAfterEach {

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(15, Seconds)), interval = scaled(Span(15, Millis)))

  // Server should be running

  var messageHandler: MessageHandler = _
  var feedEventsToState: FeedEventsToState = _

  override def beforeEach() {
    messageHandler = MessageHandler()
    feedEventsToState = new FeedEventsToState(messageHandler, State())
  }

  override def afterEach() {
    messageHandler.sendMessage(Logout())
  }

  val password = "29XxCKUHU6yaBxjrD4pb3ICFB1cSFGGLg+Fw6krRk07YbQMPZQtm5yIpuLG0HRJz"

  test("Register and connect to server") {

    val i = new Random().nextInt()
    messageHandler.sendMessage(Register(s"bob$i", password, s"email$i@email.com"))

    eventually {
      val isConnected = feedEventsToState.run().connectivity.connected
      if (isConnected)
        messageHandler.sendMessage(Logout())
      assert(isConnected)
    }

    eventually(assert(!feedEventsToState.run().connectivity.connected))
  }

  test("Register, Logout then Login") {

    val i = new Random().nextInt()
    val password = "qwerty12345!"
    val username = s"bob$i"
    val email = s"email$i@email.com"
    messageHandler.sendMessage(Register(username, password, email))

    eventually(assert(feedEventsToState.run().connectivity.connected))

    messageHandler.sendMessage(Logout())
    eventually(assert(!feedEventsToState.run().connectivity.connected))

    messageHandler.sendMessage(Login(username, password, email))
    eventually(assert(feedEventsToState.run().connectivity.connected))
  }

  test("Login and connect to server and join game") {

    val i = 1

    messageHandler.sendMessage(Login(s"bob$i", password, s"email$i@email.com"))

    eventually(assert(feedEventsToState.run().connectivity.connected))

    messageHandler.sendMessage(Join(myColour = Blue))
    eventually(assert(feedEventsToState.run().activity.joinedGames.nonEmpty))
  }

  test("Login and connect to server and create private game for 1 and start") {

    val i = 1
    messageHandler.sendMessage(Login(s"bob$i", password, s"email$i@email.com"))

    eventually(assert(feedEventsToState.run().connectivity.connected))

    val gameOptions = NewGameOptions(privateGame = true)
    messageHandler.sendMessage(NewGameRequest(numberOfPlayers = 1, myColour = Blue, newGameOptions = Option(gameOptions)))
    eventually(assert(feedEventsToState.run().activity.joinedGames.nonEmpty))

    // TODO join 2 more players and get activeGameId
    // Also test other scenarios - try to join gameId - when exists or does not exist
    // try joins in quick succession - to ensure new games spawned correctly

    eventually(assert(feedEventsToState.run().ready.games.nonEmpty))

    eventually {
      val gameState = for {
        game <- feedEventsToState.run().ready.games.headOption
        nextState = feedEventsToState.run(StartGame(game.gameId))
        g <- nextState.game
      } yield g
      assert(gameState.nonEmpty)
    }
  }

  test("Register, with errors") {

    val i = 1

    messageHandler.sendMessage(Register(s"bob$i", password, s"email-new$i@email.com"))

    eventually(assert(feedEventsToState.run().errors.exists(_.isInstanceOf[RegisterResponseUsernameUnavailable])))

    //    try again with email in use and new username
    messageHandler.sendMessage(Register(s"bob-new$i", password, s"email$i@email.com"))

    eventually(assert(feedEventsToState.run().errors.exists(_.isInstanceOf[RegisterResponseInvalid])))

    messageHandler.sendMessage(Register(s"bob-new-${new Random().nextInt()}", password, s"email-${new Random().nextInt()}@email.com"))

    eventually(assert(feedEventsToState.run().errors.isEmpty))
    eventually(assert(feedEventsToState.run().connectivity.connected))
  }

  test("Login, with errors") {

    val i = 1

    messageHandler.sendMessage(Login(s"bob-new-${new Random().nextInt()}", password, s"email-${new Random().nextInt()}@email.com"))

    eventually{
      val haveInvalidLogin = feedEventsToState.run().errors.exists(_.isInstanceOf[LoginResponseInvalid])
      // correct login
      if (haveInvalidLogin)
        messageHandler.sendMessage(Login(s"bob$i", password, s"email$i@email.com"))
      assert(haveInvalidLogin)
    }

    eventually {
      val state = feedEventsToState.run()
      assert(state.errors.isEmpty && state.connectivity.connected)
    }

  }
}
