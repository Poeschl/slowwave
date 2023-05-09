package xyz.poeschl.slowwave.commands

import mu.KotlinLogging
import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.kixelflut.Point
import xyz.poeschl.slowwave.Statistics
import xyz.poeschl.slowwave.hexToColor
import xyz.poeschl.slowwave.toHex
import java.awt.Color

class Px(private val pixelMatrix: PixelMatrix, private val statistics: Statistics) : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "PX"

  override fun handleCommand(request: List<String>): String {
    return if (request.size > 3) {
      draw(request)
    } else {
      retrieve(request)
    }
  }

  private fun draw(request: List<String>): String {
    val pixel = Pixel(Point(request[1].toInt(), request[2].toInt()), request[3].hexToColor())

    LOGGER.debug { "Drawing pixel (${pixel.point.x}, ${pixel.point.y}) -> #${pixel.color.toHex()}" }
    pixelMatrix.insert(pixel)
    statistics.increasePixelCount()
    return ""
  }

  private fun retrieve(request: List<String>): String {
    val coordinate = Point(request[1].toInt(), request[2].toInt())
    val pixel = pixelMatrix.get(coordinate) ?: Pixel(coordinate, Color.BLACK)

    LOGGER.debug { "Get pixel (${pixel.point.x}, ${pixel.point.y}) -> ${pixel.color.toHex()}" }
    return "PX ${pixel.point.x} ${pixel.point.y} ${pixel.color.toHex()}"
  }
}
