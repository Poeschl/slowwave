package xyz.poeschl.slowwave.commands

import mu.KotlinLogging
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.slowwave.Request

class Size(private val pixelMatrix: PixelMatrix) : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "SIZE"

  override fun handleCommand(request: Request): String {
    LOGGER.debug { "Retrieve playground size" }
    return "SIZE ${pixelMatrix.width} ${pixelMatrix.height}"
  }
}
