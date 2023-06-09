package xyz.poeschl.slowwave.commands

import mu.KotlinLogging
import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.kixelflut.Point
import xyz.poeschl.slowwave.*
import xyz.poeschl.slowwave.filter.FilterManager
import java.awt.Color

class Px(private val drawFilters: FilterManager<PxRequest>,
         private val pixelMatrix: PixelMatrix,
         private val statistics: Statistics) : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "PX"

  override suspend fun handleCommand(request: Request): String {
    return if (request.cmd.size > 3) {
      try {
        draw(PxRequest.fromRequest(request))
      } catch (ex: NumberFormatException) {
        "ERR Invalid number format"
      } catch (ex: IndexOutOfBoundsException) {
        "ERR Could not parse PX command"
      }
    } else {
      try {
        retrieve(request.cmd)
      } catch (ex: IndexOutOfBoundsException) {
        "ERR Could not parse PX command"
      }
    }
  }

  private fun draw(request: PxRequest): String {
    return try {
      val modifiedRequest = drawFilters.applyAllFilter(request)
      val pixel = modifiedRequest.pixel

      if (pointInMatrix(pixel.point)) {
        LOGGER.debug { "Drawing pixel (${pixel.point.x}, ${pixel.point.y}) -> #${pixel.color.toHex()}" }
        pixelMatrix.insert(pixel)
        statistics.increasePixelCount()
        ""
      } else {
        "ERR Out of canvas size"
      }
    } catch (filterEx: FilterException) {
      LOGGER.debug { "Draw exception: ${filterEx.message}" }
      "ERR ${filterEx.message ?: "(╯°□°）╯︵ ┻━┻"}"
    }
  }

  private fun pointInMatrix(point: Point): Boolean {
    return point.x >= 0 && point.x < pixelMatrix.width && point.y >= 0 && point.y < pixelMatrix.height
  }

  private fun retrieve(request: List<String>): String {
    val coordinate = Point(request[1].toInt(), request[2].toInt())
    val pixel = pixelMatrix.get(coordinate) ?: Pixel(coordinate, Color.BLACK)

    LOGGER.debug { "Get pixel (${pixel.point.x}, ${pixel.point.y}) -> ${pixel.color.toHex()}" }
    return "PX ${pixel.point.x} ${pixel.point.y} ${pixel.color.toHex()}"
  }
}
