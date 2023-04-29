package xyz.poeschl.slowwave.commands

import mu.KotlinLogging

class PixelDraw : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "PX"

  override fun handleCommand(request: List<String>): String {
    LOGGER.info { "Drawing a pixel" }
    return ""
  }
}
