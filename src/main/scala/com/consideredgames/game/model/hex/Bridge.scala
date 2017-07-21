package com.consideredgames.game.model.hex

/**
 * Created by matt on 16/03/15.
 */
case class Bridge(hexA: Hex, sideA: Side, bridgeType: BridgeType) extends BordersHex

object Bridge {

  def ordering: Ordering[Bridge] = Ordering.by(e => e.bridgeType)
}