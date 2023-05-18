package xyz.poeschl.slowwave

import mu.KotlinLogging
import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.Point
import java.awt.Color

data class Request(val remote: String, val cmd: List<String>)

data class PxRequest(val remote: String, val sourceCmd: List<String>, val pixel: Pixel) {
  companion object {

    private val LOGGER = KotlinLogging.logger { }
    fun fromRequest(request: Request): PxRequest {
      val pixel = try {
        Pixel(Point(request.cmd[1].toInt(), request.cmd[2].toInt()), request.cmd[3].hexToColor())
      } catch (ex: NumberFormatException) {
        LOGGER.info { "Format of pixel malformed: '${request.cmd}'" }
        Pixel(Point(0, 0), Color.BLACK)
      }
      return PxRequest(request.remote, request.cmd, pixel)
    }
  }

  fun withNewPixel(newPixel: Pixel): PxRequest = PxRequest(remote, sourceCmd, newPixel)
}

class FilterException(override val message: String?) : Exception()
