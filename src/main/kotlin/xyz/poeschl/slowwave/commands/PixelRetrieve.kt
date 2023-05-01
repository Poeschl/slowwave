package xyz.poeschl.slowwave.commands

import mu.KotlinLogging
import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.kixelflut.Point
import xyz.poeschl.slowwave.toHex
import java.awt.Color

class PixelRetrieve(private val pixelMatrix: PixelMatrix) : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "PX"

  override fun handleCommand(request: List<String>): String {
    val coordinate = Point(request[1].toInt(), request[2].toInt())
    val pixel = pixelMatrix.get(coordinate) ?: Pixel(coordinate, Color.BLACK)

    LOGGER.debug { "Get pixel (${pixel.point.x}, ${pixel.point.y}) -> ${pixel.color.toHex()}" }
    return "PX ${pixel.point.x} ${pixel.point.y} ${pixel.color.toHex()}"
  }
}
