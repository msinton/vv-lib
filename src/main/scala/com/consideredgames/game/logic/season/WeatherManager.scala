package com.consideredgames.game.logic.season

import com.consideredgames.common.Utils
import com.consideredgames.game.model.season.Seasons.SeasonType
import com.consideredgames.game.model.season.WeatherWeightings

import scala.util.Random

/**
 * Created by matt on 16/07/15.
 */
class WeatherManager(weatherWeightings: WeatherWeightings, random: Random) {

  private var current_ = next()

  def current(): SeasonType = current_

  def next(): SeasonType = {
    current_ = Utils.getRandomWeighted(weatherWeightings.xs, weatherWeightings.total, random).season
    current_
  }

}
