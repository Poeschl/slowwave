package xyz.poeschl.slowwave

import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.Point

data class Request(val remote: String, val cmd: List<String>)

data class PxRequest(val remote: String, val pixel: Pixel) {
  companion object {
    fun fromRequest(request: Request): PxRequest {
      val pixel = Pixel(Point(request.cmd[1].toInt(), request.cmd[2].toInt()), request.cmd[3].hexToColor())
      return PxRequest(request.remote, pixel)
    }
  }
}
