package xyz.poeschl.slowwave.commands

import mu.KotlinLogging

class PixelRetrieve : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "PX"

  override fun handleCommand(request: List<String>): String {
    LOGGER.info { "Get a pixel" }
    // Add dummy color
    val coloredPixel = mutableListOf<String>()
    coloredPixel.addAll(request)
    coloredPixel.add("000000")
    return coloredPixel.joinToString(" ")
  }
}
