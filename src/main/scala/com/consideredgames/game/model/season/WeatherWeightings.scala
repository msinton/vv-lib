package com.consideredgames.game.model.season

import com.consideredgames.common.HasWeighting
import com.consideredgames.game.model.season.Seasons._

/**
 * Created by matt on 05/06/15.
 */
case class WeatherWeighting(weighting: Int, season: SeasonType) extends HasWeighting

case class WeatherWeightings(xs: List[WeatherWeighting]) {
  val total: Int = xs.foldLeft(0) { case (acc, w) => w.weighting + acc }
}

object WeatherWeightings {

  val defaultEasy = WeatherWeightings(List(
      WeatherWeighting(30, Spring),
      WeatherWeighting(30, Summer),
      WeatherWeighting(25, Autumn),
      WeatherWeighting(15, Winter)))

  val defaultHard = WeatherWeightings(List(
    WeatherWeighting(25, Spring),
    WeatherWeighting(25, Summer),
    WeatherWeighting(25, Autumn),
    WeatherWeighting(25, Winter)))
}
