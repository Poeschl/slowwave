package xyz.poeschl.slowwave.commands

import mu.KotlinLogging
import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.Point
import xyz.poeschl.slowwave.PxRequest
import xyz.poeschl.slowwave.Request
import xyz.poeschl.slowwave.filter.BaseFilter

class Offset : BaseCommand {

  private val offsetMap = mutableMapOf<String, Point>()

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "OFFSET"

  override fun handleCommand(request: Request): String {
    val offset = Point(request.cmd[1].toInt(), request.cmd[2].toInt())
    LOGGER.debug { "Set the offset $offset for ${request.remote}" }

    offsetMap[request.remote] = offset

    return "OFFSET ${offset.x} ${offset.y}"
  }

  fun getOffsetForSocket(socket: String): Point {
    return offsetMap.getOrDefault(socket, Point(0, 0))
  }

  fun removeOffsetForSocket(socket: String) {
    offsetMap.remove(socket)
  }

  fun getFilter(): OffsetFilter {
    return OffsetFilter(this)
  }

  class OffsetFilter(private val offsetCommand: Offset) : BaseFilter<PxRequest> {
    override fun applyFilter(input: PxRequest): PxRequest {
      val offset = offsetCommand.getOffsetForSocket(input.remote)
      return input.withNewPixel(Pixel(input.pixel.point.plus(offset), input.pixel.color))
    }
  }
}
