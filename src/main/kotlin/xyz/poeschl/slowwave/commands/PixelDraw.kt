package xyz.poeschl.slowwave.commands

import mu.KotlinLogging
import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.kixelflut.Point
import xyz.poeschl.slowwave.hexToColor
import xyz.poeschl.slowwave.toHex

class PixelDraw(private val pixelMatrix: PixelMatrix) : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "PX"

  override fun handleCommand(request: List<String>): String {
    val pixel = Pixel(Point(request[1].toInt(), request[2].toInt()), request[3].hexToColor())

    LOGGER.debug { "Drawing pixel (${pixel.point.x}, ${pixel.point.y}) -> #${pixel.color.toHex()}" }
    pixelMatrix.insert(pixel)
    return ""
  }
}
