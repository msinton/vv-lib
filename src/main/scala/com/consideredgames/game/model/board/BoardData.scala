package com.consideredgames.game.model.board

import java.util.NoSuchElementException

import com.consideredgames.common.Utils
import com.consideredgames.game.model.animals.{Animal, AnimalInfo, AnimalManager}
import com.consideredgames.game.model.hex._
import com.typesafe.scalalogging.LazyLogging

import scala.collection._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * Sets up the game board and holds all the hexes, and rivers.
 *
 * Randomly arrange hexes according to a grid scheme consisting of columns. Not all 'slots' are filled.
 *
 * <pre> TODO fix scaladoc format to look as below
 * {@code
 * column:  0   1   2   3  ... totalColumns-1
 *             '0'    '0'          '0'
 *         '0'     '0'
 *             '1'    '1'
 *         '1'     '1'
 * 				.
 * 				.
 * 				.
 *             'N-1'
 * 	       'N-1'  'N-1'
 * 			   'N'
 * }
 * </pre>
 */

class BoardData(numberOfPlayers: Int, val random: Random, animalInfos: List[AnimalInfo]) extends LazyLogging {
  
  import scala.collection.JavaConversions._

  private val totals = initialiseBoardParameters

  private val rowLengthMax = 10
  private val totalHexes = totals._1
  /** This should be an odd number to work with <code>initialiseColumnEnd</code>. */
  private val totalColumns = totals._2
  private var largestRowIndex = 0
  /** Must use hexCount for new hex IDs since hexes may be deleted so hexes.size would not work. */
  private var hexCount = 0

  /** Keyed by hex id */
  val hexes: mutable.Map[Int, Hex] = new java.util.HashMap[Int, Hex]
  val positionsByHex: mutable.Map[Hex, HexPosition] = new java.util.TreeMap[Hex, HexPosition]
  val hexesByPosition: mutable.Map[HexPosition, Hex] = new java.util.HashMap[HexPosition, Hex]

  val animalManagers: ArrayBuffer[AnimalManager] = ArrayBuffer.empty
  val boats: mutable.Set[Boat] = mutable.Set.empty[Boat]
  val bridges: mutable.Set[Bridge] = mutable.TreeSet.empty[Bridge](Bridge.ordering)
  val volcanoes: mutable.Set[Hex] = new java.util.TreeSet[Hex]
  val walls: mutable.Set[Hex] = new java.util.TreeSet[Hex]

  val riverNetwork: RiverNetwork = init()

  /**
   * Initialise the board with random placement of hexes, according to defined weightings. Include a river network.
   */
  private def init() = {
    initialiseHexes()
    val hexesIterator: Iterator[(Int, Hex)] = hexes.iterator
    try {
      initialiseMiddleColumns(hexesIterator)
      // first column
      initialiseColumnEnd(0, 1, hexesIterator)
      // last column
      initialiseColumnEnd(totalColumns - 1, totalColumns - 2, hexesIterator)
    }
    catch {
      case e: NoSuchElementException =>
      // do nothing
    } finally {
      if (hexesIterator.hasNext) {
        val unusedHex = hexesIterator.next()
        for (id <- unusedHex._1 to hexCount) {
          hexes.remove(id)
        }
      }
    }
    BoardUtils.connectHexes(hexesByPosition)

    // debugging
    for (hex <- hexes.values) {
      logger.debug("HexId:" + hex.id)
      logger.debug("Hex neighbours:" + hex.neighbours)
    }

    PointInitialiser.setupPoints(hexes.values.iterator, new PointFactory)
    val riverNetwork = new RiverNetwork(random)
    riverNetwork.init(hexes.values, numberOfPlayers)
    riverNetwork.setupFlow()
    logger.debug("Board Creation Done")
    riverNetwork
  }

  def add(manager: AnimalManager) {
    animalManagers.add(manager)
  }

  def getHexPosition(hex: Hex): Option[HexPosition] = positionsByHex.get(hex)

  def getHexes: Iterable[Hex] = hexes.values

  /**
   * The number of rows.
   */
  def getRowLength: Int = largestRowIndex

  def getColumnLength: Int = this.totalColumns

  def getHex(hexId: Int): Option[Hex] = hexes.get(hexId)

  def add(bordersHex: BordersHex): Boolean = {
    bordersHex match {
      case b@Boat(_, _) =>
        boats.add(b)
        true
      case b@Bridge(_, _, _) =>
        bridges.add(b)
      case _ =>
        false
    }
  }

  def add(wall: Wall, h: Hex) {
    h.wall = Option.apply(wall)
    walls.add(h)
  }

  def remove(bordersHex: BordersHex) {
    bordersHex match {
      case b@Boat(_, _) =>
        boats -= b
      case b@Bridge(_, _, _) =>
        bridges -= b
    }
    bordersHex.removeFromHexes()
  }

  def removeWall(h: Hex) {
    walls.remove(h)
    h.wall = None
  }

  /**
   * Sets totalHexes and totalColumns according to the number of players.
   *
   * @return totalHexes, totalColumns
   */
  private def initialiseBoardParameters = {
    numberOfPlayers match {
      case 1 =>
        (50, 9)
      case 2 =>
        (70, 11)
      case 3 =>
        (100, 13)
      case 4 =>
        (125, 15)
      case 5 =>
        (140, 17)
      case _ =>
        (170, 19)
    }
  }

  /**
   * Initialise the <code>hexes</code> ArrayList with randomly assigned hex types.
   */
  private def initialiseHexes() {

    def getHexWeighting = { hexType: HexType => hexType.getWeighting.toInt }
    val hexTypesAsList: List[HexType] = HexType.values().toList

    for (index <- 0 until totalHexes) {

      val hexType = Utils.getRandomWeighted(hexTypesAsList, HexType.getTotalWeighting, getHexWeighting, random)
      val hex: Hex = Hex(hexCount, hexType)

      //animals
      val animalWeightingsTotal = animalInfos.foldLeft(0) { case (x: Int, info: AnimalInfo) => info.rarity + x }
      def getAnimalWeighting = { animalInfo: AnimalInfo => animalInfo.rarity }
      val animalType = Utils.getRandomWeighted(animalInfos, animalWeightingsTotal, getAnimalWeighting, random)
      for (i <- 0 until (random.nextInt(2) + 1)) {
        AnimalManager.addAnimalTo(hex, Animal(animalType, random.nextBoolean()), animalInfos)
      }

      hexes.put(hex.id, hex)
      hexCount += 1
    }

    // add volcanoes
    val soloPlayerAdjustment: Int = if (numberOfPlayers > 1) numberOfPlayers else 2
    var randomInt = random.nextInt(soloPlayerAdjustment * 2 - 1) + 3
    while (randomInt > 0) {
      val hex: Hex = Utils.getRandom(hexes.values, random)
      if (hex.hexType != HexType.PLAINS && !hex.volcano) {
        hex.volcano = true
        volcanoes.add(hex)
        randomInt -= 1
      }
    }
  }

  /**
   * By choosing a random start index and a random number of hexes, the middle columns are populated with
   * hexes from the hexesIterator.
   */
  private def initialiseMiddleColumns(hexesIterator: Iterator[(Int, Hex)]) {
    for (columnsIndex <- 1 until totalColumns - 1) {

      val length = random.nextInt(rowLengthMax - ((columnsIndex + 1) % 2) - 5) + 5
      val start = random.nextInt(rowLengthMax - ((columnsIndex + 1) % 2) - length)
      if ((length + start) > largestRowIndex) {
        largestRowIndex = length + start
      }

      for (positionIndex <- 0 until length) {
        setHexPosition(hexesIterator.next()._2, new HexPosition(columnsIndex, start + positionIndex))
      }
    }
  }

  /**
   * initialiseMiddleColumns must be performed prior to this. Initialises the column with valid Hex positions so that there are no
   * unattached hexes i.e. the game Board is a single connected structure.
   *
   * @param columnNumber The column which is being initialised.
   * @param columnAdjacent The column which will anchor the hexes of <code>columnNumber</code>.
   * @param hexesIterator Iterating over all the <code>hexes</code>.
   */
  private def initialiseColumnEnd(columnNumber: Int, columnAdjacent: Int, hexesIterator: Iterator[(Int, Hex)]) {

    // E.g. columnNumber = 0, columnAdjacent = 1
    // the possible values which column 0 can take, defined by column 1
    // (we don't want hexes which aren't attached to anything!)
    def getPossibleRowPositions = {
      val possiblePositions: mutable.Set[Int] = new java.util.HashSet[Int]
      var adjacentColumnRowEntries: List[Int] = List()

      for (row <- 1 until (rowLengthMax - 1)) {
        if (hexesByPosition.get(HexPosition(columnAdjacent, row)).nonEmpty)
          adjacentColumnRowEntries = row :: adjacentColumnRowEntries
      }

      // special cases for the first and last hexes in the column adjacent
      if (hexesByPosition.get(HexPosition(columnAdjacent, 0)).nonEmpty)
        possiblePositions += 0
      if (hexesByPosition.get(HexPosition(columnAdjacent, rowLengthMax - 1)).nonEmpty)
        possiblePositions += (rowLengthMax - 2)

      // except for the special cases, we add the row and the one below
      for (row <- adjacentColumnRowEntries) {
        possiblePositions += row
        possiblePositions += (row - 1)
      }
      possiblePositions
    }

    def randomFillColumn(possiblePositions: List[Int]) = {
      val minimumLength = 2
      val size = random.nextInt(possiblePositions.size - minimumLength) + minimumLength

      val shuffledIndices = random.shuffle(possiblePositions.indices.toList)

      shuffledIndices.take(size).foreach { index =>
        setHexPosition(hexesIterator.next()._2, HexPosition(columnNumber, possiblePositions(index)))
      }
    }

    randomFillColumn(getPossibleRowPositions.toList)
  }

  private def setHexPosition(h: Hex, position: HexPosition) {
    hexesByPosition.put(position, h)
    positionsByHex.put(h, position)
  }
}