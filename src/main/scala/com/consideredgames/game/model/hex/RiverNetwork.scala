package com.consideredgames.game.model.hex

import com.consideredgames.common.Utils
import com.typesafe.scalalogging.LazyLogging

import scala.collection._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * use the edges of a hex - shared by 2 hexes randomly generated river network
 *
 * splits board into even number of hexes - as many sections as players
 */

class RiverNetwork(random: Random) extends LazyLogging {

  import scala.collection.JavaConversions._

  /** The entire collection of RiverSegments that make up the network. */
  private val rivers: ArrayBuffer[RiverSegment] = ArrayBuffer.empty
  /** Used for creating the network. It is the hexes which are available for adding to the groups. Set to null after completion. */
  private var hexesAvailable: mutable.Set[Hex] = new java.util.HashSet[Hex]()
  /** Used for creating the network. Each group represents a distinct section of the board. */
  private val groups: mutable.Buffer[mutable.Buffer[mutable.Set[Hex]]] = ArrayBuffer.empty
  private var flattenedGroups: List[collection.Set[Hex]] = Nil
  private var flowSetup: Boolean = false
  private var networkCreated: Boolean = false

  def getGroups: Iterable[Set[Hex]] = {
    flattenedGroups
  }

  /**
   * Creates the river network, using many helper methods - if the network has not already been created.
   */
  def init(hexes: Iterable[Hex], numberOfPlayers: Int) {
    if (!networkCreated) {
      while (!creationMain(hexes, numberOfPlayers)) {
      }
      for (group <- groups) {
        for (layer <- group) {
          if (groups.indexOf(group) != (groups.size - 1)) {
            for (hex <- layer) {
              val hexNeighboursNotInGroup: Set[Hex] = neighboursNotInGroup(hex, group)
              for (borderHex <- hexNeighboursNotInGroup) {
                addRiver(hex, hex.getSide(borderHex).get)
              }
            }
          }
        }
      }
      flattenedGroups = groups.map { _.reduce((a, b) => a ++ b) }.toList
      networkCreated = true
//      hexesAvailable.clear()
      for (seg <- rivers) {
        logger.debug(seg.toString)
      }
    }
  }

  /**
   * Get the set of hexes of the neighbours that aren't in the group.
   *
   * @return The set of hexes of the neighbours that aren't in the group
   */
  private def neighboursNotInGroup(hex: Hex, group: Iterable[Set[Hex]]): Set[Hex] = {
    var hexesNotInGroup = hex.neighbours.values.toSet
    for (layer <- group) {
      hexesNotInGroup = hexesNotInGroup.diff(layer)
    }
    hexesNotInGroup
  }

  /**
   * Create a network by separating the board into different groups. Form the groups from layers, that is layer zero contains a random hex, layer
   * one contains neighbours of that hex, layer two has neighbours of those hexes and so on. Once a group cannot grow anymore, if the size is deemed
   * to be below a certain threshold then we return false. Otherwise continue until all hexes allocated to a group.
   *
   * @return true if successful
   */
  private def creationMain(hexes: Iterable[Hex], numberOfPlayers: Int): Boolean = {
    hexesAvailable = new java.util.HashSet[Hex](hexes)
    var success: Boolean = true
    initialiseGroups(numberOfPlayers)
    if (!initialiseLayerZeroAndOne) {
      return false
    }
    var layerIndex: Int = 1
    val currentLayers: mutable.Buffer[mutable.Set[Hex]] = new ArrayBuffer(groups.size)
    val groupIndexes = new ArrayBuffer[Int](groups.size)
    while (hexesAvailable.nonEmpty && success) {
      setUpGroupIndexesAndLayers(currentLayers, groupIndexes, layerIndex)
      success = fillNextLayers(currentLayers, groupIndexes, layerIndex)
      layerIndex += 1
    }
    success
  }

  /**
   * When a layer is empty and the group is finished then we don't add the group index to groupIndexes and .
   *
   * @param currentLayers
   * @param groupIndexes
   * @param layerIndex
   */
  private def setUpGroupIndexesAndLayers(currentLayers: mutable.Buffer[mutable.Set[Hex]], groupIndexes: mutable.Buffer[Int], layerIndex: Int) {
    var groupIndex: Int = 0
    if (groupIndexes != null) {
      groupIndexes.clear()
    }
    for (group <- groups) {
      groupIndex = groups.indexOf(group)
      if (group.size > layerIndex) {
        if (group.get(layerIndex).nonEmpty) {
          currentLayers.insert(groupIndex, new java.util.HashSet[Hex](group.get(layerIndex)))
          group.add(new java.util.HashSet[Hex])
          groupIndexes.add(groupIndex)
          logger.debug("GroupIndex: " + groupIndex + " layers: " + currentLayers)
        }
        else {
          currentLayers.insert(groupIndex, new java.util.HashSet[Hex](0))
        }
      }
    }
  }

  /**
   * Initialise the structures which are then used for creating a network. groups -> layers -> set of hexes. A group for each player, a layer for
   * each iteration, in each layer is a set of hexes. Determines the number of branches in the river according to the number of players.
   */
  private def initialiseGroups(numberOfPlayers: Int) {
    if (groups != null) {
      groups.clear()
    }
    var totalGroups: Int = 0
    if (numberOfPlayers == 0) {
      throw new IllegalArgumentException("When creating the river network - number of players cannot be zero!")
    }
    else if (numberOfPlayers == 1) {
      totalGroups = 2
    }
    else {
      totalGroups = numberOfPlayers
    }

    var groupNumber: Int = 0
    while (groupNumber < totalGroups) {
      {
        groups.add(ArrayBuffer.empty)
      }
      groupNumber += 1
    }

  }

  /**
   * Layer Zero of each group is initialised with one Hex and Layer one is initialised with as many neighbours of that hex as possible.
   */
  private def initialiseLayerZeroAndOne: Boolean = {
    var tempHex: Hex = null
    for (group <- groups) {
      group.add(new java.util.HashSet[Hex](1))
      tempHex = Utils.getRandom(hexesAvailable, random)
      group.get(0).add(tempHex)
      hexesAvailable.remove(tempHex)
    }
    for (group <- groups) {
      group.add(new java.util.HashSet[Hex](5))
      for (hex <- group.get(0)) {
        addHexesToLayer(group.get(1), hex, 5)
      }
      if (group.get(1).isEmpty) {
        return false
      }
    }
    true
  }

  /**
   * Cycles through the indexes until layers are emptied or failure has occurred. Emptying the groupIndexes and the currentLayers. Check any
   * finished groups to ensure they meet the minimum size requirements.
   *
   * @param currentLayers
   * @param groupIndexes
   * @param layerIndex
   * @return true if successful, false if not.
   * @throws IllegalArgumentException
   */
  private def fillNextLayers(currentLayers: mutable.Buffer[mutable.Set[Hex]], groupIndexes: mutable.Buffer[Int], layerIndex: Int): Boolean = {
    var currentlayer: mutable.Set[Hex] = null
    var nextlayer: mutable.Set[Hex] = null
    var success: Boolean = true
    var tempHex: Hex = null
    val minimumSize: Int = 10
    if (currentLayers != null && groupIndexes != null) {
      while (groupIndexes.nonEmpty && success) {

        for (groupIndex <- groupIndexes) {
          currentlayer = currentLayers.get(groupIndex)
          nextlayer = groups.get(groupIndex).get(layerIndex + 1)
          if (currentlayer.nonEmpty) {
            tempHex = Utils.getRandom(currentlayer, random)
            if (addHexesToLayer(nextlayer, tempHex, 1) == 0) {
              currentlayer.remove(tempHex)
            }
          }
          else {
            if (nextlayer.isEmpty) {
              success = isLargeEnough(groups.get(groupIndex), minimumSize)
            }
            groupIndexes -= groupIndex
          }
        }
      }
    }
    else {
      throw new IllegalArgumentException
    }
    success
  }

  /**
   * Checks if the total number of hexes meet the required size.
   *
   * @param layers
   * @param requiredSize
   * @return true if required size met.
   */
  private def isLargeEnough(layers: Iterable[collection.Set[Hex]], requiredSize: Int): Boolean = {
    var groupSum = 0
    for (layer <- layers) {
      groupSum += layer.size
    }
    if (groupSum < requiredSize) {
      false
    } else {
      true
    }
  }

  /**
   * Adds neighbouring hexes to the provided layer according to the specified amount, and removes from <code>this.hexesAvailable</code> the added
   * hexes
   *
   * @param layer The layer to add to.
   * @param hex The hex to get neighbours from.
   * @param maxToAdd The maximum neighbouring hexes to add from the hex, this is at most 6.
   * @return The number of hexes which are still available to add.
   */
  private def addHexesToLayer(layer: mutable.Set[Hex], hex: Hex, maxToAdd: Int): Int = {
    val hexesToAdd: mutable.Set[Hex] = new java.util.HashSet(hex.neighbours.values)
    hexesToAdd.retainAll(hexesAvailable)
    var added: Int = 0
    while (hexesToAdd.nonEmpty && (added < maxToAdd)) {
      val tempHex = Utils.getRandom(hexesToAdd, random)
      layer.add(tempHex)
      added += 1
      hexesToAdd.remove(tempHex)
      hexesAvailable.remove(tempHex)
    }
    hexesToAdd.size
  }

  /**
   * Enables the flow to be setup, does nothing if already setup.
   */
  final def setupFlow() {
    if (!flowSetup) {
      val flowInitialiser = new FlowInitialiser()
      flowInitialiser.setup(rivers, random)
      flowSetup = true
    }
  }

  final def removeRiver(river: RiverSegment) {
    rivers -= river
    river.removeFromHexes()
  }

  final def addRiver(hexA: Hex, sideA: Side): Option[RiverSegment] = {
    RiverSegment.create(rivers, hexA, sideA).map { r =>
      rivers.add(r)
      r
    }
  }

  final def getRivers: List[RiverSegment] = rivers.toList

}