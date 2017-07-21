package com.consideredgames.game.model.resources

import com.consideredgames.game.model.person.Person
import com.consideredgames.game.model.person.tools.{RichToolInfo, Tool, ToolUtils}
import com.consideredgames.game.model.resources.Resources.Resource

import scala.collection._
import scala.collection.mutable.ArrayBuffer

/**
 * Created by matt on 09/03/15.
 */
case class ItemContainer(toolUtils: ToolUtils) {

  import scala.collection.JavaConversions._

  private val resources_ = ArrayBuffer.empty[ResourceGroup]

  private val tools_ : collection.mutable.Map[RichToolInfo, ArrayBuffer[Tool]] = new java.util.TreeMap[RichToolInfo, ArrayBuffer[Tool]]()

  private val assignedTools_ = new mutable.AnyRefMap[(RichToolInfo, Int), Tool]()

  private val toolsAssignedAsConstructionMaterial : collection.mutable.Map[RichToolInfo, ArrayBuffer[Tool]] = new java.util.TreeMap[RichToolInfo, ArrayBuffer[Tool]]()

  private val assignedResources = ArrayBuffer.empty[ResourceGroup]

  private def add(r: ResourceGroup, buffer: collection.mutable.Buffer[ResourceGroup]): Unit = {

    buffer.indexWhere( _.canMerge(r)) match {
      case -1 => buffer += r
      case n => buffer.update(n, buffer(n).merge(r).get)
    }
  }

  def add(r: ResourceGroup): Unit = add(r, resources_)

  def add(richToolInfo: RichToolInfo, tool: Tool): Unit = tools_.getOrElseUpdate(richToolInfo, ArrayBuffer()) += tool

  def consume(resource: Resource, number: Int): Boolean = consume(resource, number, resources_)

  /**
   * Consumes resources from the supplied buffer if there are enough.
   * @return false if number of resources in buffer < "number"
   */
  private def consume(resource: Resource, number: Int, buffer: collection.mutable.Buffer[ResourceGroup]): Boolean = {
    buffer.find( _.r == resource) exists {

      res =>
        res.n - number match {
          case x if x >= 0 =>
            buffer -= res
            buffer += ResourceGroup(res.r, x)
            true

          case y if y < 0 => false
        }
    }
  }

  def consume(richToolInfo: RichToolInfo, person: Person): Boolean = {
    assignedTools_.get((richToolInfo, person.id)).exists { t =>
      val newLife = t.life - 1
      if (newLife > 0) {
        assignedTools_.update((richToolInfo, person.id), Tool(newLife))
      } else {
        assignedTools_.remove((richToolInfo, person.id))
      }
      true
    }
  }

  def take(richToolInfo: RichToolInfo, tool: Tool): Option[Tool] = {
    tools_.get(richToolInfo) match {
      case None => None
      case Some(xs) =>
        xs.indexWhere(_ == tool) match {
          case -1 => None
          case index => Option(xs.remove(index))
        }
    }
  }

  private def assign(resourceGroup: ResourceGroup, from: collection.mutable.Buffer[ResourceGroup], to: collection.mutable.Buffer[ResourceGroup]) = {
    from.indexWhere(_.canMerge(resourceGroup)) match {
      case -1 => false
      case x if resourceGroup.n <= from(x).n =>
        // assign the resources to "to" buffer
        add(resourceGroup, to)
        // remove the resources from "from" buffer
        consume(resourceGroup.r, resourceGroup.n.toInt, from)
        true
      case _ => false
    }
  }

  def assign(resourceGroup: ResourceGroup): Boolean = assign(resourceGroup, resources_, assignedResources)

  def unassign(resourceGroup: ResourceGroup): Boolean = assign(resourceGroup, assignedResources, resources_)

  /**
   * tries to assign all the res groups, and if fails to do so, rolls back the attempt.
   */
  def assign(resourceGroups: List[ResourceGroup]): Boolean = {

    var resourceGroups_ = resourceGroups
    var canAssignAll = true
    var allAssigned = false
    var assigned: List[ResourceGroup] = Nil

    while(!allAssigned && canAssignAll) {
      resourceGroups_ match {

        case Nil => allAssigned = true
          
        case x :: xs if assign(x) =>
          resourceGroups_ = xs
          assigned = x :: assigned
          
        case _ =>
          canAssignAll = false
          assigned.foreach(unassign)
      }
    }
    allAssigned
  }

  /**
   * Assigns the first matching tool of the specified type to the person
   * If the person already has a tool of that type assigned then <b>don't</b> swap it.
   * @return true if success, or already had the tool of that type.
   */
  def assign(toolInfo: RichToolInfo, person: Person): Boolean = {

    if (person.tools.contains(toolInfo)) return true

    tools_.get(toolInfo) match {
      case Some(xs) if xs.nonEmpty =>
        assignedTools_.put((toolInfo, person.id), xs.remove(0))
        person.tools += toolInfo
        true

      case _ => false
    }
  }

  def unassign(toolInfo: RichToolInfo, person: Person): Option[Tool] = {
    assignedTools_.remove(toolInfo, person.id) match {
      case x@Some(tool) => person.tools -= toolInfo
        x
      case _ => None
    }
  }

  def assignForConstruction(tools: Map[RichToolInfo, Int]): Boolean = {

    def assign(toolInfo: RichToolInfo, tool: Tool): Unit = {
      tools_(toolInfo) -= tool
      toolsAssignedAsConstructionMaterial.getOrElseUpdate(toolInfo, ArrayBuffer()) += tool
    }

    def unassign(toolWithInfo: (RichToolInfo, Tool)): Unit = {
      toolsAssignedAsConstructionMaterial(toolWithInfo._1) -= toolWithInfo._2
      tools_(toolWithInfo._1) += toolWithInfo._2
    }

    var toolsToAssign = tools.toList
    var assigned: List[(RichToolInfo, Tool)] = Nil
    var allAssigned = false
    var canAssignAll = true

    while (!allAssigned && canAssignAll) {
      toolsToAssign match {

        case Nil => allAssigned = true
        case (tool, n) :: xs if tools_.exists {case(t,buff) => t == tool && buff.size >= n} =>
          for (i <- 1 to n) {
            val taken = tools_(tool).sortBy(_.life).head
            assign(tool, taken)
            toolsToAssign = xs
            assigned = (tool, taken) :: assigned
          }
        case _ =>
          canAssignAll = false
          assigned.foreach(unassign)
      }
    }

    allAssigned
  }

  def unassignForConstruction(tools: Map[RichToolInfo, Int]): Unit = {

    tools.foreach { case (toolInfo, n) =>
      for (i <- 1 to n)
        toolsAssignedAsConstructionMaterial.get(toolInfo).collect {
          case toolBuffer if toolBuffer.nonEmpty =>
            tools_.getOrElseUpdate(toolInfo, ArrayBuffer()) += toolBuffer.head
            toolBuffer -= toolBuffer.head
        }
    }
  }

  def availableTools(toolsToFind: Traversable[RichToolInfo]) = {

    tools_.filter {case (toolInfo, buffer) => buffer.nonEmpty && toolsToFind.exists(_ == toolInfo)}.keySet
  }

  def resources: Seq[ResourceGroup] = resources_

  def tools: collection.Map[RichToolInfo, ArrayBuffer[Tool]] = tools_

  def assignedTools: collection.Map[(RichToolInfo, Int), Tool] = assignedTools_

}
