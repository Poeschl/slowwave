package xyz.poeschl.slowwave.commands

import xyz.poeschl.slowwave.Request

interface BaseCommand {

    val command: String

    /**
     * Handles a command. The full command is received by list which is the space separated request text.
     * The returned string will be the response of the tcp request.
     */
    suspend fun handleCommand(request: Request): String
}
