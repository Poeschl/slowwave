package xyz.poeschl.slowwave

import mu.KotlinLogging
import java.util.*

class Statistics {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  private var pixelCount: Int = 0
  var pixelPerSecond: Int = 0
    private set

  init {
    Timer().schedule(object : TimerTask() {
      override fun run() {
        pixelPerSecond = pixelCount
        pixelCount = 0
      }
    }, 0L, 1000L)
  }

  fun increasePixelCount() {
    pixelCount++
  }
}
