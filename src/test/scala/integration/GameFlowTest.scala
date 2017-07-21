//package com.consideredgames.integration
//
//import com.consideredgames.game.model.hex.Hex
//import com.consideredgames.game.model.person.NewPersonInstruction
//import com.consideredgames.game.model.player.DummyPlayer
//import com.consideredgames.game.model.player.PlayerColours._
//import com.consideredgames.message._
//import com.consideredgames.message.Messages._
//import org.scalatest.{FunSuite, OptionValues}
//
///**
// * Created by matt on 09/10/15.
// */
//class GameFlowTest extends FunSuite with OptionValues {
//
//  object hex1 extends Hex(1)
//  object hex2 extends Hex(2)
//  object hex30 extends Hex(30)
//  object hex31 extends Hex(31)
//
//  test("the whole shebang (well, focusing on gameProcessors)") {
//
//    val bobDummy = DummyPlayer("bob", Black)
//    val fredDummy = DummyPlayer("fred", Blue)
//
//    val players: List[DummyPlayer] = List(bobDummy, fredDummy)
//
//    val bobsEventProcessor = new EventProcessor()
//    bobsEventProcessor.process(NewGame("gameId", players, 123456789,
//      NewGameOptions(None, None, None, Map("stone axe" -> 1, "fishing rod" -> 1, "stone pickaxe" -> 1)), success = true, startNow = true))
//
//    //board groups:
//// Set(Hex(52), Hex(53), Hex(60), Hex(51), Hex(44), Hex(59), Hex(58), Hex(54), Hex(45), Hex(46), Hex(35), Hex(36), Hex(61), Hex(43), Hex(42), Hex(26), Hex(27), Hex(47), Hex(38), Hex(62), Hex(55), Hex(34), Hex(28), Hex(37), Hex(48), Hex(39), Hex(29), Hex(56), Hex(22), Hex(21), Hex(63), Hex(30), Hex(20), Hex(23), Hex(40), Hex(15), Hex(14), Hex(57), Hex(64), Hex(49), Hex(31), Hex(50), Hex(41), Hex(9), Hex(32), Hex(33)),
//// Set(Hex(66), Hex(7), Hex(68), Hex(67), Hex(6), Hex(8), Hex(5), Hex(4), Hex(13), Hex(17), Hex(18), Hex(3), Hex(65), Hex(12), Hex(16), Hex(11), Hex(2), Hex(24), Hex(19), Hex(10), Hex(1), Hex(25), Hex(69), Hex(0)))
//    val notification = bobsEventProcessor.nextNotification()
//    println(notification)
//
//    val bobsGP = bobsEventProcessor.startGame(bobsEventProcessor.newGames("gameId"), bobDummy)
//
//    //server sends first player their deployments - or client works it out themselves?
//    val bobsGState = bobsGP.game.gameState
//
//    assert(bobsGState.turnState.currentPlayers === List(0))
//    // implies turn of
//    val bob = bobsGP.game.playerData.getPlayer(0).value
//    assert(bob.name === "bob")
//    val deployments = List(NewPersonInstruction(1, Black), NewPersonInstruction(2, Black))
//
//    val deployer = bobsGP.game.controllers.deployment.getDeployerFor(deployments)
//
//    val board = bobsGP.game.boardUtils.boardData
//    // bob takes his turn
//    // bobs placements
//    deployer.toPlace.foreach(person => deployer.place(person, board.getHex(person.id).value))
//
//    assert(deployer.finished())
//    val bobsPlaced1 = deployer.placedPeople
//    //send to server
//    deployer.submit()
//    assert(board.getHexes.filter(_.person.nonEmpty) == List(hex1, hex2))
//
//    //process on server TODO
//
//    // server sends to other player
//    val fredsEP = new EventProcessor()
//    fredsEP.process(NewGame("gameId", players, 123456789, NewGameOptions(), success = true, startNow = true))
//    val fredsGP = fredsEP.startGame(fredsEP.newGames("gameId"), fredDummy)
//    // fred processes bob's turn
//    fredsGP.processMove(Deployments(bobsPlaced1))
//
//    val fredsState = fredsGP.game.gameState
//    assert(fredsState.turnState.currentPlayers == List(1))
//    assert(fredsGP.game.playerData.getPlayer(1).value.name === "fred")
//
//    // server send freds deployments
//    val deploymentsFred1 = List(NewPersonInstruction(3, Blue), NewPersonInstruction(4, Blue))
//    val fredsDeployer1 = fredsGP.game.controllers.deployment.getDeployerFor(deploymentsFred1)
//    var people = fredsDeployer1.toPlace
//    val fredsBoard = fredsGP.game.boardUtils.boardData
//
//    // fred takes his turn
//    // freds placements
//    fredsDeployer1.place(people.toList(0), fredsBoard.getHex(30).value)
//    fredsDeployer1.place(people.toList(1), fredsBoard.getHex(31).value)
//
//    assert(fredsDeployer1.finished())
//    fredsDeployer1.submit()
//    assert(fredsBoard.getHexes.filter(_.person.nonEmpty) == List(hex1, hex2, hex30, hex31))
//
//    // bob processes fred's move
//    val fredsPlaced1 = fredsDeployer1.placedPeople
//    bobsGP.processMove(Deployments(fredsPlaced1))
//    assert(board.getHexes.filter(_.person.nonEmpty) === List(hex1, hex2, hex30, hex31))
//    assert(bobsGState.turnState.phase === Principal)
//
//    // bob takes his turn
//    // bob's actions
//    val bobsActions = bobsGP.game.gameProcessors.principal.actions
//    val bobsActionMaker = bobsGP.game.controllers.principal.getActionMaker
//
//    bobsActionMaker.add(bobsActions.MineStone, bobsPlaced1(0).person)
//    bobsActionMaker.add(bobsActions.ChopWood, bobsPlaced1(1).person)
//
//    bobsActionMaker.submit()
//
//    // bob takes his turn
//    // bob's actions
//    val fredsActions = fredsGP.game.gameProcessors.principal.actions
//    val fredsActionMaker = fredsGP.game.controllers.principal.getActionMaker
//
//    fredsActionMaker.add(fredsActions.MineStone, fredsPlaced1(0).person)
//    fredsActionMaker.add(fredsActions.ChopWood, fredsPlaced1(1).person)
//
//    fredsActionMaker.submit()
//    //todo - submit to send to server, also for deployments
//
//    // process the actions
//    // check everyone has resource gain etc
//
//
//    assert(false)
//    //todo test attacking
//    //TwoPersonActionParameter(attacker1, defender)
//  }
//}
