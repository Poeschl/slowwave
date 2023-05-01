package xyz.poeschl.slowwave.commands

import mu.KotlinLogging

class Offset : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "OFFSET"

  override fun handleCommand(request: List<String>): String {
    LOGGER.debug { "Set a offset" }
    return "NOT IMPLEMENTED"
  }
}
