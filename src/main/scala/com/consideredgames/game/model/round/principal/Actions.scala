package com.consideredgames.game.model.round.principal

import com.consideredgames.game.model.animals.AnimalInfo
import com.consideredgames.game.model.exceptions.ResourceInvalidException
import com.consideredgames.game.model.hex.HexType
import com.consideredgames.game.model.hex.HexType._
import com.consideredgames.game.model.info.person.skill.Skills
import com.consideredgames.game.model.info.person.skill.Skills.{SkillType, _}
import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.{RichToolInfo, ToolProduct, ToolUtils, Tools}
import com.consideredgames.game.model.resources.Resources._
import com.consideredgames.game.model.resources.{ItemContainer, ResourceGroup, ResourceProduct}
import com.consideredgames.serializers.{Named, NamedSetSerializer}

/**
 * Created by matt on 16/04/15.
 */
sealed trait Action extends Named

sealed trait PersonAction extends Action {

  /**
   * Is possible as is.
   */
  def isPossible(person: Person): Boolean

  /**
   * Is possible for person with help from the itemContainer
   */
  def isPossible(person: Person, itemContainer: ItemContainer): (Boolean, collection.Set[RichToolInfo])

  def xpGain: Int = 1

  def isPermanent: Boolean = false
}

trait OnePersonResultAction extends PersonAction {

  def resultWhenPossible(person: Person): ActionResult

  def result(person: Person) = {
    if (isPossible(person))
      resultWhenPossible(person)
    else
      EmptyResult
  }
}

trait TwoPersonResultAction extends PersonAction {

  def resultWhenPossible(p1: Person, p2: Person): ActionResult

  def isPossible(p1: Person, p2: Person): Boolean

  def result(p1: Person, p2: Person) = {
    if (isPossible(p1, p2))
      resultWhenPossible(p1, p2)
    else
      EmptyResult
  }
}

trait CraftAction extends PersonAction {

  def resultWhenPossible(p1: Person, order: List[RichToolInfo]): ActionResult

  def isPossible(person: Person, order: List[RichToolInfo], itemContainer: ItemContainer): Boolean

  def result(person: Person, order: List[RichToolInfo], itemContainer: ItemContainer) = {
    if (isPossible(person, order, itemContainer))
      resultWhenPossible(person, order)
    else
      EmptyResult
  }
}

trait ToolRequirements {

  val toolUtils: ToolUtils
  val skill: SkillType

  protected def canMeetToolRequirement(person: Person, itemContainer: ItemContainer): (Boolean, collection.Set[RichToolInfo]) = {
    val toolsAvailable = itemContainer.availableTools(toolUtils.allToolInfo.core(skill))
    toolUtils.canMeetCoreRequirement(person, skill, toolsAvailable)
  }

  protected def meetsToolRequirement(person: Person): Boolean = toolUtils.coreRequirementSatisfied(person, skill)._1

  def isPossible(person: Person): Boolean = meetsToolRequirement(person)

  def isPossible(person: Person, itemContainer: ItemContainer): (Boolean, collection.Set[RichToolInfo]) = canMeetToolRequirement(person, itemContainer)
}

trait ActionRequirements {
  protected def meetsActionRequirements(person: Person): Boolean

  def isPossible(person: Person) = meetsActionRequirements(person)
}

trait ActionRequirementsForCrafters {
  protected def meetsActionRequirements(person: Person, order: List[RichToolInfo], itemContainer: ItemContainer): Boolean

  def isPossible(person: Person, order: List[RichToolInfo], itemContainer: ItemContainer) = meetsActionRequirements(person, order, itemContainer)
}

trait ToolAndActionRequirements extends ToolRequirements with ActionRequirements {

  override def isPossible(person: Person, itemContainer: ItemContainer): (Boolean, collection.Set[RichToolInfo]) = {
    val toolReq = canMeetToolRequirement(person, itemContainer)
    (toolReq._1 && meetsActionRequirements(person), toolReq._2)
  }

  override def isPossible(person: Person): Boolean = meetsToolRequirement(person) && meetsActionRequirements(person)
}

trait SkillLevelProvider {
  val toolUtils: ToolUtils
  val skill: SkillType

  /**
   * @return the total skill level for the person including tool bonuses
   */
  protected def skillLevel(person: Person) = Actions.skillLevel(person, skill, toolUtils)
}

//resources

trait ResourceProduceProvider {

  val produces: ResourceProducts

  def resourceProduct(person: Person, skillLevel: Int): ResourceProduct = {
    produces.produce(math.min(skillLevel, produces.maxLevel))
  }
}

trait BaseResourceGatheringOnePersonAction extends OnePersonResultAction with SkillLevelProvider with ResourceProduceProvider {

  def resourceProduct(person: Person): ResourceProduct = resourceProduct(person, skillLevel(person))

  // if skill level greater than final product then use the last one
  def resultWhenPossible(person: Person): ResourceResult = ResourceResult(resourceProduct(person), xpGain, skill)
}

abstract class ResourceGatheringPersonAction(val name: String, val skill: SkillType, val produces: ResourceProducts, val toolUtils: ToolUtils)
  extends BaseResourceGatheringOnePersonAction with ToolAndActionRequirements

abstract class ResourceGatheringPersonActionWithNoActionRequirements(val name: String, val skill: SkillType, val produces: ResourceProducts, val toolUtils: ToolUtils)
  extends BaseResourceGatheringOnePersonAction with ToolRequirements

// Harvest (slaughter)
abstract class HarvestAnimalPersonAction(val name: String, val skill: SkillType, val toolUtils: ToolUtils)
  extends BaseHarvestAction with SkillLevelProvider with ToolRequirements

// no action result
abstract class NoResultPersonAction(val name: String, val skill: SkillType, val toolUtils: ToolUtils)
  extends OnePersonResultAction with SkillLevelProvider with ToolRequirements {

  def resultWhenPossible(person: Person): ActionResult = EmptyResult
}

// LevelProduct Actions (attack, defense)

trait LevellingProvider {
  val produces: LevelProducts
  def levelProduct(person: Person, skillLevel: Int): Int = {
    produces.produce(math.min(skillLevel, produces.maxLevel))
  }
}

abstract class LevellingPersonAction(val name: String, val skill: SkillType, val produces: LevelProducts, val toolUtils: ToolUtils)
  extends OnePersonResultAction with SkillLevelProvider with LevellingProvider with ToolRequirements {

  def levelProduct(person: Person): Int = levelProduct(person, skillLevel(person))
}

abstract class LevellingTwoPersonAction(val name: String, val skill: SkillType, val produces: LevelProducts, val toolUtils: ToolUtils)
  extends TwoPersonResultAction with SkillLevelProvider with LevellingProvider with ToolRequirements {

  def levelProduct(person: Person): Int = levelProduct(person, skillLevel(person))
}


// WeatherProduct action

trait WeatherProvider {
  val produces: WeatherProductFormula
  def weatherProduct(person: Person, skillLevel: Int): WeatherResourceProduct = {
    produces.produce(math.min(skillLevel, produces.maxLevel))
  }
}

abstract class WeatherProductPersonAction(val name: String, val skill: SkillType, val produces: WeatherProductFormula, val toolUtils: ToolUtils)
  extends OnePersonResultAction with SkillLevelProvider with WeatherProvider with ToolAndActionRequirements {

  def weatherProduct(person: Person): WeatherResourceProduct = weatherProduct(person, skillLevel(person))

  def resultWhenPossible(person: Person): ArableResult = ArableResult(weatherProduct(person), xpGain, skill)
}

// hunting

trait AnimalProduceProvider {

  val produces: AnimalProductFormula

  def animalHuntProduct(person: Person, skillLevel: Int): List[Int] = {
    produces.produce(math.min(skillLevel, produces.maxLevel))
  }
}

abstract class AnimalHuntingPersonAction(val name: String, val skill: SkillType, val produces: AnimalProductFormula, val toolUtils: ToolUtils)
  extends OnePersonResultAction with SkillLevelProvider with ToolAndActionRequirements with AnimalProduceProvider {

  def animalHunted: AnimalInfo

  def animalHuntProduct(person: Person): List[Int] = animalHuntProduct(person, skillLevel(person))

  def resultWhenPossible(person: Person): HuntingResult = HuntingResult(animalHuntProduct(person), animalHunted, xpGain, skill)

  override protected def meetsActionRequirements(person: Person): Boolean =
    person.hex.exists(_.animalManager.exists(_.containers.exists { case (animalInfo, container) => animalInfo == animalHunted && container.size > 0 }))
}

// person producing

abstract class PersonProducingPersonAction(val name: String, val skill: SkillType, val produces: PersonProducts, val toolUtils: ToolUtils)
  extends TwoPersonResultAction with ToolAndActionRequirements {

  protected def skillLevel(person: Person) = Actions.skillLevel(person, skill, toolUtils)
}

// harvesting

trait BaseHarvestAction extends PersonAction {

  def resultWhenPossible(n: Int, animal: AnimalInfo): HarvestResult = {
    HarvestResult(n, animal)
  }
}

// crafting

trait ToolAndActionRequirementsForCrafters extends ToolRequirements with ActionRequirementsForCrafters {

  override def isPossible(person: Person, order: List[RichToolInfo], itemContainer: ItemContainer) =
    meetsToolRequirement(person) && meetsActionRequirements(person, order, itemContainer)
}

trait ToolProduceProvider {

  val produces: ToolProducts

  // the number of tools that can produce
  def toolProduct(person: Person, skillLevel: Int): Int =
    produces.produce(math.min(skillLevel, produces.maxLevel))
}

trait BaseCraftAction extends CraftAction with SkillLevelProvider with ToolProduceProvider {

  def toolProduct(person: Person): Int = toolProduct(person, skillLevel(person))

  // if skill level greater than final product then use the last one
  def resultWhenPossible(person: Person, order: List[RichToolInfo]): CraftingResult = {
    // slice cuts down order to a size which is possible
    // default quantity to produce for each is 1
    CraftingResult(order.slice(0, toolProduct(person)).map {
      tool => ToolProduct(tool, tool.production.produces.getOrElse(1))
    }, xpGain, skill)
  }
}

case class Craftables(possible: Set[RichToolInfo], cant: Set[RichToolInfo])

abstract class CraftToolsPersonAction(val name: String, val skill: SkillType, val produces: ToolProducts, val toolUtils: ToolUtils)
  extends BaseCraftAction with ToolAndActionRequirementsForCrafters {

  private def isToolBuildableForSkill(tool: RichToolInfo, skillLevel: Int, skillType: SkillType) = {
    tool.builders.exists(b => b.s == skillType && b.level.forall(_ <= skillLevel))
  }

  def getCraftables(person: Person, orderSoFar: List[RichToolInfo], itemContainer: ItemContainer, allTools: Tools): Craftables = {

    val skillLevel_ : Int = skillLevel(person)

    val toolsForThisSkill = allTools.builders.getOrElse(skill, Set())

    val orderContainsUnskilled = orderSoFar.exists {
      case tool => !tool.builders.exists(_.s == skill) && tool.builders.exists(_.s == Unskilled)
    }

    if (orderCapacity(person) >= orderSoFar.size) {
      Craftables(Set(), toolsForThisSkill)
    } else {

      val skillCraftables = craftables(allTools.builders.getOrElse(skill, Set()), skill, skillLevel_, itemContainer)

      if (!orderContainsUnskilled) {
        val unskilledCraftables = craftables(allTools.builders.getOrElse(Unskilled, Set()), Unskilled, skillLevel_, itemContainer)
        Craftables(skillCraftables.possible ++ unskilledCraftables.possible, skillCraftables.cant ++ unskilledCraftables.cant)
      } else {
        skillCraftables
      }
    }
  }

  private def craftables(tools: Set[RichToolInfo], skillType: SkillType, skillLevel: Int, itemContainer: ItemContainer): Craftables = {

    var possible: Set[RichToolInfo] = Set()
    var cant: Set[RichToolInfo] = Set()

    tools.foreach {
      case tool if isToolBuildableForSkill(tool, skillLevel, skillType) && tool.madeFromResources.forall {
        case (needsRes, need) => itemContainer.resources.exists {
          case resGroup => resGroup.r == needsRes && need <= resGroup.n
        }
      } => possible = possible + tool

      case tool => cant = cant + tool
    }
    Craftables(possible, cant)
  }

  def materialsNeeded(order: List[RichToolInfo]) = order.map(tool => (tool.production.resources, tool.madeFromTools))

  // for clarity
  def orderCapacity(person: Person) = toolProduct(person)

  override def meetsActionRequirements(person: Person, order: List[RichToolInfo], itemContainer: ItemContainer) = {

    def undo(processed: List[(List[ResourceGroup], collection.Map[RichToolInfo, Int])]): Unit = {
      processed.foreach({ case (resGroups, tools) =>
        resGroups.foreach(itemContainer.unassign)
        itemContainer.unassignForConstruction(tools)
      })
    }

    // one of the orders is permitted to be an "unskilled" tool, the rest must be this crafter's skill
    def orderConsistsOfItemsItCanBuild() = {
      val unskilledOrders = order.filter(_.builders.exists(_.s == Unskilled))
      if (unskilledOrders.size > 1 )
        false
      else {
        // remaining are all for this skill
        order.diff(unskilledOrders).forall(_.builders.exists(_.s == skill))
      }
    }

    def personCanBuildTheNumberOfItems() = {
      orderCapacity(person) >= order.size
    }

    if (personCanBuildTheNumberOfItems() && orderConsistsOfItemsItCanBuild()) {
      // try to process the order. If fails then rollback all changes.
      var canDoIt = true
      val itr = materialsNeeded(order).iterator
      var processed: List[(List[ResourceGroup], collection.Map[RichToolInfo, Int])] = Nil

      while (itr.hasNext && canDoIt) {
        itr.next() match {
          case (resourceGroups, tools) if itemContainer.assign(resourceGroups) =>
            if (!itemContainer.assignForConstruction(tools)) {
              // undo the resource assigning we just did
              resourceGroups.foreach(itemContainer.unassign)
              canDoIt = false
            } else
              processed = (resourceGroups, tools) :: processed

          case _ => canDoIt = false
        }
      }

      if (!canDoIt) undo(processed)

      canDoIt
    }
    else
      false
  }

}

trait HiddenHexType {
  def foundType(hexType: HexType): Unit
}

// All the skill based actions
class Actions(toolUtils: ToolUtils, animalInfos: List[AnimalInfo]) {

  case object Fish extends ResourceGatheringPersonAction("fish", Fisherman, SimpleProductFormula(Food, List(2, 2, 3, 4, 5, 6, 6, 7, 7, 8, 9)), toolUtils) {

    override protected def meetsActionRequirements(person: Person): Boolean =
      person.hex.exists(h => h.hexType == WATER || h.rivers.nonEmpty) || person.boat.isDefined
  }

  case object MineOre extends ResourceGatheringPersonAction("mine ore", Miner, SimpleProductFormula(IronOre, List(1, 1, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7)), toolUtils) {

    override protected def meetsActionRequirements(person: Person): Boolean = person.hex.exists(h => h.hexType == ORE)

    override def isPermanent: Boolean = true
  }

  case object MineClay extends ResourceGatheringPersonAction("mine clay", Miner, SimpleProductFormula(Clay, List(1, 1, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7)), toolUtils) {

    override protected def meetsActionRequirements(person: Person): Boolean = person.hex.exists(h => h.hexType == CLAY)

    override def isPermanent: Boolean = true
  }

  case object MineStone extends ResourceGatheringPersonAction("mine stone", Miner, SimpleProductFormula(Stone, List(1, 1, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7)), toolUtils) {

    override protected def meetsActionRequirements(person: Person): Boolean = person.hex.exists(h => h.hexType == STONE)

    override def isPermanent: Boolean = true
  }

  case object MineUnknown extends ResourceGatheringPersonAction("mine plains", Miner, DelegatingResourceProvider(), toolUtils) with HiddenHexType {

    override protected def meetsActionRequirements(person: Person): Boolean = person.hex.exists(h => h.hexType == PLAINS)

    def foundType(hexType: HexType): Unit = {
      this.produces match {
        case p: DelegatingResourceProvider => hexType match {
          case CLAY => p.producer = MineClay.produces
          case ORE => p.producer = MineOre.produces
          case STONE => p.producer = MineStone.produces
          case _ => //do nothing
        }
      }
    }

    override def isPermanent: Boolean = true
  }

  case object Reproduce extends PersonProducingPersonAction("reproduce", Reproducer, SimplePersonProductFormula(List(70, 72, 75, 78, 81, 84, 87, 91, 93, 96)), toolUtils) {

    // exists a neighbouring person on same team
    private def aPotentialMateExists(person: Person): Boolean = {
      person.hex.exists(_.neighboursAccessibleByFoot().exists {
        case (_, h) =>
          h.person.exists(_.playerColour == person.playerColour)
      })
    }

    private def canMate(person: Person, mate: Person): Boolean = {
      person.hex.exists(_.neighboursAccessibleByFoot().exists {
        case (_, h) => h.person.contains(mate)
      })
    }

    // if skill level greater than final product then use the last one
    override def resultWhenPossible(person: Person, mate: Person): PersonProductionResult = {
      PersonProductionResult(produces.produce(math.min(skillLevel(person), produces.produce.size - 1)).percent, mate, xpGain, skill)
    }

    protected def meetsActionRequirements(person: Person, mate: Option[Person]): Boolean = mate.fold(aPotentialMateExists(person))(canMate(person, _))

    override protected def meetsActionRequirements(person: Person): Boolean = meetsActionRequirements(person, None)

    def isPossible(person: Person, mate: Person) = meetsToolRequirement(person) && meetsActionRequirements(person, Option(mate))

    override def result(person: Person, mate: Person) = {
      if (isPossible(person, mate))
        resultWhenPossible(person, mate)
      else
        EmptyResult
    }
  }

  case object ChopWood extends ResourceGatheringPersonAction("chop wood", Lumberjack, SimpleProductFormula(Wood, List(1, 1, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7)), toolUtils) {
    override protected def meetsActionRequirements(person: Person): Boolean = person.hex.exists(_.hexType == WOODS)

    override def isPermanent: Boolean = true
  }

  case object FarmArable extends WeatherProductPersonAction("farm arable", ArableFarmer, WeatherProductFormula(Food,
    IndexedSeq(1, 1, 1, 2, 3, 4, 4, 5, 6,  7,  8,  10),
    IndexedSeq(1, 1, 2, 3, 4, 5, 6, 8, 9,  11, 13, 15),
    IndexedSeq(1, 1, 2, 3, 3, 4, 5, 6, 7,  8,  10, 12),
    IndexedSeq(0, 0, 1, 1, 2, 2, 3, 4, 5,  6,  7,  9)), toolUtils) {

    override protected def meetsActionRequirements(person: Person): Boolean = person.hex.exists(_.hexType == PLAINS)

    override def isPermanent: Boolean = true
  }

  case object Cook extends ResourceGatheringPersonActionWithNoActionRequirements("cook", Skills.Cook,
    DoubleProductFormula(Meal, GoodMeal, List(1, 1, 3, 5, 7, 10, 10, 11, 9, 8, 7, 6), List(0, 0, 0, 0, 0, 2, 4, 8, 9, 10, 11, 12)), toolUtils) {
  }

  //TODO Alternatively to throwing an exception, allow the absence of animals and therefore make the hunting action always impossible
  case object HuntHorse extends AnimalHuntingPersonAction("hunt horse", Attacker, AnimalProductFormula(12, List(50, 30)), toolUtils) {
    override def animalHunted: AnimalInfo = animalInfos.find(_.name == "horse").getOrElse(throw ResourceInvalidException("animals must contain a 'horse'"))
  }

  case object HuntBoar extends AnimalHuntingPersonAction("hunt boar", Attacker, AnimalProductFormula(12, List(55, 35)), toolUtils) {
    override def animalHunted: AnimalInfo = animalInfos.find(_.name == "boar").getOrElse(throw ResourceInvalidException("animals must contain a 'boar'"))
  }

  case object HuntChicken extends AnimalHuntingPersonAction("hunt chicken", Attacker, AnimalProductFormula(12, List(60, 50, 40, 30)), toolUtils) {
    override def animalHunted: AnimalInfo = animalInfos.find(_.name == "chicken").getOrElse(throw ResourceInvalidException("animals must contain a 'chicken'"))
  }

  case object HuntRabbit extends AnimalHuntingPersonAction("hunt rabbit", Attacker, AnimalProductFormula(12, List(70, 60, 50, 40)), toolUtils) {
    override def animalHunted: AnimalInfo = animalInfos.find(_.name == "rabbit").getOrElse(throw ResourceInvalidException("animals must contain a 'rabbit'"))
  }

  case object Masonry extends CraftToolsPersonAction("masonry", Mason, SimpleToolProductFormula(List(1, 2, 3, 4, 5, 5, 6, 6, 7, 7, 8, 9)), toolUtils)

  case object Carpentry extends CraftToolsPersonAction("carpentry", Carpenter, SimpleToolProductFormula(List(1, 2, 3, 4, 5, 5, 6, 6, 7, 7, 8, 9)), toolUtils)

  case object Ironmongery extends CraftToolsPersonAction("ironmongery", Ironmonger, SimpleToolProductFormula(List(1, 2, 3, 4, 5, 5, 6, 6, 7, 7, 8, 9)), toolUtils)

  case object Pottery extends CraftToolsPersonAction("pottery", Potter, SimpleToolProductFormula(List(1, 2, 3, 4, 5, 5, 6, 6, 7, 7, 8, 9)), toolUtils)

  case object Goldsmithing extends CraftToolsPersonAction("goldsmithing", Goldsmith, SimpleToolProductFormula(List(1, 2, 3, 4, 5, 5, 6, 6, 7, 7, 8, 9)), toolUtils)

  case object Tailoring extends CraftToolsPersonAction("tailoring", Tailor, SimpleToolProductFormula(List(1, 2, 3, 4, 5, 5, 6, 6, 7, 7, 8, 9)), toolUtils)

  case object Attack extends LevellingTwoPersonAction("attack", Attacker, LevelProducts(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)), toolUtils) with ToolAndActionRequirements {

    override protected def meetsActionRequirements(person: Person): Boolean =
      person.hex.exists(_.neighboursAccessibleByFoot().exists { case (side, hex) => hex.person.exists(_.playerColour != person.playerColour)})

    // volcano gives defense bonus
    override def resultWhenPossible(person: Person, defender: Person): AttackDamageResult = {
      AttackDamageResult(levelProduct(person) - defender.hex.count(_.volcano), defender, xpGain, skill)
    }

    override def xpGain = 2

    override def isPossible(p1: Person, p2: Person): Boolean = p1.playerColour != p2.playerColour
  }

  case object Defend extends NoResultPersonAction("defend", Defender, toolUtils) {
    override def resultWhenPossible(person: Person): ActionResult = DefendResult(xpGain, skill)
  }

  // increased number of slaughters allowed by level
  case object Slaughter extends HarvestAnimalPersonAction("slaughter", Attacker, toolUtils) with ToolAndActionRequirements {
    override protected def meetsActionRequirements(person: Person): Boolean =
      person.hex.exists(_.animalManager.exists(_.containers.exists( _._2.tame.nonEmpty))) || person.animalManager.exists(_.containers.exists( _._2.size > 0))
  }

  //TODO factor in animal specific skill when calculating capacity
  case object FarmAgriculture extends NoResultPersonAction("farm animals", AgricultureFarmer, toolUtils) with ToolAndActionRequirements {

    override protected def meetsActionRequirements(person: Person): Boolean =
      person.hex.exists(_.animalManager.exists(_.containers.exists( _._2.size > 0))) || person.animalManager.exists(_.containers.exists(_._2.size > 0))

    override def isPermanent: Boolean = true
  }

  val allActions = List(MineClay, MineOre, MineStone, MineUnknown, Fish, Reproduce, ChopWood, FarmArable, FarmAgriculture, Cook, HuntBoar,
    HuntChicken, HuntHorse, HuntRabbit, Masonry, Carpentry, Ironmongery, Pottery, Goldsmithing, Tailoring, Attack, Defend, Slaughter)

  val serializer = new NamedSetSerializer[Action](allActions.toSet, Some("actions"))
}


object Actions {

  def skillLevel(person: Person, skillType: SkillType, toolUtils: ToolUtils) =
    Skills.skillLevel(person, skillType) + toolUtils.getSkillBonus(person, skillType)
}