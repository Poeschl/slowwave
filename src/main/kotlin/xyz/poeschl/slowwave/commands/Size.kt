package xyz.poeschl.slowwave.commands

import mu.KotlinLogging

class Size : BaseCommand {

  companion object {
    private val LOGGER = KotlinLogging.logger { }
  }

  override val command = "SIZE"

  override fun handleCommand(request: List<String>): String {
    LOGGER.info { "Retrieve playground size" }
    return "SIZE 10 10"
  }
}
