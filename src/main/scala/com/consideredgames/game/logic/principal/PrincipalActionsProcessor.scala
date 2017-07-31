package com.consideredgames.game.logic.principal

import com.consideredgames.game.logic.season.WeatherManager
import com.consideredgames.game.model.animals._
import com.consideredgames.game.model.board.BoardData
import com.consideredgames.game.model.hex.HexType
import com.consideredgames.game.model.info.person.skill.Skills._
import com.consideredgames.game.model.info.person.skill._
import com.consideredgames.game.model.person._
import com.consideredgames.game.model.person.tools.Tool
import com.consideredgames.game.model.resources._
import com.consideredgames.game.model.round.principal._
import com.consideredgames.game.model.player.PlayerColours.PlayerColour
import com.consideredgames.game.model.player._
import com.consideredgames.message._

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * Created by matt on 05/06/15.
 */
class PrincipalActionsProcessor(val actions: Actions, random: Random, weatherManager: WeatherManager, boardData: BoardData) {

  private var personsProducedCount = 0

  def preProcessForServer(playerActions: List[(FullPlayer, List[ActionFulfillment])]) = {

    var allResults = playerActions.map {
      case (player, actionFulfillments) => (player, {
        actionFulfillments.map {

          case ActionFulfillment(action: OnePersonResultAction, param: PersonActionParameter) =>
            action match {
              case action: HiddenHexType => processHiddenHexTypeResult(action, param.person)

              case _ => (param.person, action.result(param.person))
            }

          case ActionFulfillment(action: TwoPersonResultAction, param: TwoPersonActionParameter) =>
            (param.p1, action.result(param.p1, param.p2))

          case _ => ???
        }
      })
    }

    val attacks = allResults.map { case (player: FullPlayer, actionFulls) =>
      (player, actionFulls.collect {
        case (p, actionResult: AttackDamageResult) => (p, actionResult)
      })
    }

    val defends = allResults.map { case (player: FullPlayer, actionFulls) =>
      (player, actionFulls.collect {
        case (p, actionResult: DefendResult) => (p, actionResult)
      })
    }

    val attackResults = preProcessAttackAndDefense(defends, attacks)

    //filter out dead people - they can't perform any actions!
    val allDeaths = attackResults._1.deaths.flatMap(_._2).toSet
    allResults = allResults.map { case (player, actionResults) => (player, actionResults.filterNot { case (p, act) => allDeaths.contains(p) }) }

    // TODO use actor to do this bit
    var animalsKilled: List[Animal] = List()
    var newPersonInstructionsByPlayer: List[(FullPlayer, List[NewPersonInstruction])] = List()

    allResults.foreach {
      case (player, actionResults: List[(Person, ActionResult)]) =>
        var newPersons: List[NewPersonInstruction] = List()
        actionResults.foreach { case (person, actionResult) =>
          val processActions_Results = processActionResults(person, actionResult, player, forClient = false)
          animalsKilled = processActions_Results._1 ::: animalsKilled
          newPersons = processActions_Results._2 ::: newPersons
        }
        newPersonInstructionsByPlayer = (player, newPersons) :: newPersonInstructionsByPlayer
    }

    (attackResults._1, attackResults._2, allResults, animalsKilled, newPersonInstructionsByPlayer)
  }

  /**
   * transforms the result from preProcessForServer into a Message that applies to the player.
   */
  def actionResultsByPlayerForServer(player: FullPlayer, attackResults: AttackResults, actionResults: List[(FullPlayer, List[(Person, ActionResult)])],
                                     animalsKilled: List[Animal], newPersonInstructions: List[(FullPlayer, List[NewPersonInstruction])]): PrincipalPhasePreProcess = {
    PrincipalPhasePreProcess(attackResults, actionResults.collect {
      case (p, rest) if p == player => rest map { r => PersonToActionResult(r._1, r._2) }
    }.flatten,
      animalsKilled, newPersonInstructions.collect {
        case (p, instrs) if p == player => instrs
      }.flatten
    )
  }

  def processResultsForClient(actionProcessResult: PrincipalPhasePreProcess, player: FullPlayer): Unit = {

    actionProcessResult.allActionResults.foreach { r =>
      processActionResults(r.person, r.actionResult, player, forClient = true)
    }
  }

  def postProcessForServer(actionProcessResult: PrincipalPhasePreProcess, playerData: List[PlayerWithPeople]): Unit = {

    actionProcessResult.attackResults.deaths.foreach {
      case (colour, people) => playerData.filter { _.colour == colour }.foreach { player =>
        people.foreach(player.kill)
      }
    }
  }

  private def increaseXP(xpGain: Int, person: Person, skillType: SkillType): Unit = {

    val stat = person.skills.getOrElseUpdate(skillType, new SkillStat(0))

    val newStatLevel = stat.xpLevel + xpGain

    //Defenders increase HP - each Defender Level gained is another hit point
    if (skillType == Defender) {
      val level = Skills.skillLevel(stat.xpLevel, skillType)
      val newLevel = Skills.skillLevel(newStatLevel, skillType)
      person.hp = person.hp + newLevel - level
    }

    person.skills.update(skillType, new SkillStat(newStatLevel))
  }

  private def processActionResults(person: Person, actionResult: ActionResult, player: FullPlayer, forClient: Boolean) = {

    var killed: List[Animal] = List()
    var newPersonInstructions: List[NewPersonInstruction] = List()

    actionResult match {
      case actionResult: ResourceResult =>
        resourcesResult(actionResult, player.itemContainer)
        levelUp(person, actionResult)

      case actionResult: CraftingResult =>
        craftingResult(actionResult, player.itemContainer)
        levelUp(person, actionResult)

      case actionResult: PersonProductionResult =>
        val instr = personProductionResult(actionResult, player, random)
        instr.foreach { n =>
          newPersonInstructions = n :: newPersonInstructions
        }
        levelUp(person, actionResult)

      case actionResult: HuntingResult =>
        killed = huntingResult(person, actionResult, player.itemContainer, random) ::: killed
        levelUp(person, actionResult)

      case actionResult: ArableResult =>
        arableResult(actionResult, player.itemContainer)
        levelUp(person, actionResult)

      case actionResult: HarvestResult =>
        killed = harvestResult(person, actionResult, player.itemContainer) ::: killed

      case actionResult: AttackDamageResult =>
        levelUp(person, actionResult)

      case actionResult: DefendResult =>
        if (forClient) {
          levelUp(person, actionResult)
        }

      case _ => // do nothing
    }
    (killed, newPersonInstructions)
  }

  private def levelUp(person: Person, personResult: PersonResult): Unit = {
    increaseXP(personResult.xpGain, person, personResult.skill)
  }

  private def resourcesResult(resourceResult: ResourceResult, itemContainer: ItemContainer): Unit = {
    resourceResult.result.product.foreach(itemContainer.add)
  }

  private def craftingResult(craftingResult: CraftingResult, itemContainer: ItemContainer): Unit = {
    craftingResult.result.foreach(x => itemContainer.add(x.product, Tool(x.product.startLife)))
  }

  private def arableResult(arableResult: ArableResult, itemContainer: ItemContainer): Unit = {
    val n = arableResult.weatherResourceProduct.value(weatherManager.current())
    itemContainer.add(ResourceGroup(arableResult.weatherResourceProduct.resource, n))
  }

  private def harvestResult(person: Person, harvestResult: HarvestResult, itemContainer: ItemContainer): List[Animal] = {
    var killed: List[Animal] = List()
    person.hex.foreach(_.animalManager.foreach(_.containers.foreach { case (animalInfo, container) if animalInfo == harvestResult.animal =>
      killed = AnimalHarvester.harvest(container, harvestResult.number, itemContainer)
    }))
    killed
  }

  private def personProductionResult(personProductionResult: PersonProductionResult, player: PlayerWithPeople, random: Random): Option[NewPersonInstruction] = {

    if (random.nextInt(100) < personProductionResult.percent) {
      personsProducedCount = personsProducedCount + 1
      val newPersonInstr = NewPersonInstruction(personsProducedCount, player.colour)
      player.create(newPersonInstr)
      Option(newPersonInstr)
    } else {
      None
    }
  }

  private def huntingResult(person: Person, result: HuntingResult, container: ItemContainer, random: Random) = {
    //TODO fair way to resolve hunting when multiple people hunt the same hex - atm can only hunt hex they are on so not a prob. but planning ahead...
    var killed: List[Animal] = List()
    val roll = random.nextInt(100)

    val killAnimalsInstructions = result.percents.zipWithIndex.reverse.collectFirst {
      case (percent, number) if roll < percent => (result.animal, number)
    }

    killAnimalsInstructions.foreach {
      case (animalInfo, number) => person.hex.foreach(h =>
        if (h.animalManager.exists(_.containers.get(animalInfo).isDefined))
          killed = AnimalHarvester.harvest(h.animalManager.get.containers.get(animalInfo).get, number, container)
      )
    }
    killed
  }

  // calculates attack results but also levels up defenders
  private def preProcessAttackAndDefense(defends: List[(Player, List[(Person, DefendResult)])], attacks: List[(Player, List[(Person, AttackDamageResult)])]) = {

    val defend = actions.Defend
    val attack = actions.Attack

    // level up defenders
    defends.foreach(_._2.foreach {
      case (person, result) =>
        increaseXP(defend.xpGain, person, Skills.Defender)
    })

    // produce Damage listing
    val damages: ArrayBuffer[Damage] = ArrayBuffer()
    val damageMap: mutable.AnyRefMap[Person, Int] = mutable.AnyRefMap.empty[Person, Int]

    attacks.foreach(_._2.foreach {
      case (person, result) =>
        damages += Damage(result.amount, person, result.attacked)
        damageMap.update(result.attacked, damageMap.getOrElse(result.attacked, 0) + result.amount)
    })

    // get defender survivors
    val survivors = (for {
      damage <- damageMap
      if damage._1.hp > damage._2
    } yield damage._1).toList

    // the defenders still alive retaliate
    attacks.foreach(_._2.foreach {
      case (defendingAttacker, result) if survivors.contains(result.attacked) =>

        val damage = attack.resultWhenPossible(result.attacked, defendingAttacker).amount
        damages += Damage(damage, result.attacked, defendingAttacker)
        damageMap.update(defendingAttacker, damageMap.getOrElse(defendingAttacker, 0) + damage)
      case _ => // do nothing if not already dead
    })

    val deaths = for {
      damage <- damageMap
      if damage._1.hp <= damage._2
    } yield damage._1

    val deathsMap: collection.Map[PlayerColour, Set[Person]] = {
      val map = mutable.AnyRefMap.empty[PlayerColour, Set[Person]]
      deaths.foreach { d =>
        map.update(d.playerColour, map.getOrElse(d.playerColour, Set()) + d)
      }
      map
    }

    // survive an attack then get defender skill bonus (if had not already)
    val survivorsThatDidNotDefend: List[Person] = survivors.filterNot(p => defends.exists(_._2.contains(p))).toList

    val bonusXpGain = survivorsThatDidNotDefend.map(p => (p, defend.xpGain, Defender))
    bonusXpGain.foreach { case (person, xpGain, skill) => increaseXP(xpGain, person, skill) }

    (AttackResults(damages.toList, deathsMap), bonusXpGain)
  }

  private def processHiddenHexTypeResult(action: HiddenHexType with OnePersonResultAction, person: Person) = {

    // if is still plains
    if (person.hex.exists(h => boardData.getHex(h.id).forall(_.hiddenType.isEmpty))) {

      import scala.collection.JavaConverters._

      val boundaries = HexType.upperEarthBoundaries.asScala
      val n = random.nextInt(boundaries.last._2)

      @tailrec
      def getType(hexType: HexType, bound: Int, boundaries: collection.Map[HexType, Integer]): HexType = {
        if (n < bound || boundaries.isEmpty) hexType else getType(boundaries.head._1, boundaries.head._2, boundaries.tail)
      }
      val hexType = getType(boundaries.head._1, boundaries.head._2, boundaries.tail)
      action.foundType(hexType)

      //update in boardData
      boardData.getHex(person.hex.get.id).foreach(_.hiddenType = Option(hexType))

    } else {
      person.hex.foreach(_.hiddenType.foreach(action.foundType))
    }

    (person, action.result(person))
  }
}
