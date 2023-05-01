package xyz.poeschl.slowwave

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import xyz.poeschl.slowwave.commands.*

class SlowwaveTestApplication(host: String, listeningPort: Int) {

  companion object {

    private val LOGGER = KotlinLogging.logger {}
  }

  private val selectorManager = SelectorManager(Dispatchers.IO)
  private val serverSocket = aSocket(selectorManager).tcp().bind(host, listeningPort)

  private val helpCommand = Help()
  private val sizeCommand = Size()
  private val pixelDrawCommand = PixelDraw()
  private val pixelRetrieveCommand = PixelRetrieve()
  private val offsetCommand = Offset()

  fun run() {
    runBlocking {
      LOGGER.info { "Server is listening at ${serverSocket.localAddress}" }

      while (true) {
        val socket = serverSocket.accept()
        LOGGER.info { "Accepted connection from ${socket.remoteAddress}" }

        launch {

          val receiveChannel = socket.openReadChannel()
          val sendChannel = socket.openWriteChannel(autoFlush = true)

          try {
            while (socket.isActive) {
              val input = receiveChannel.readUTF8Line()
              if (input != null) {
                val parsedCmd = input.split(" ")
                val command = parsedCmd[0]

                val response =
                  when {
                    command == helpCommand.command -> helpCommand.handleCommand(parsedCmd)
                    command == sizeCommand.command -> sizeCommand.handleCommand(parsedCmd)
                    command == pixelDrawCommand.command && parsedCmd.size > 3 -> pixelDrawCommand.handleCommand(parsedCmd)
                    command == pixelRetrieveCommand.command && parsedCmd.size == 3 -> pixelRetrieveCommand.handleCommand(parsedCmd)
                    command == offsetCommand.command -> offsetCommand.handleCommand(parsedCmd)
                    else -> ""
                  }

                sendChannel.writeStringUtf8(response + "\n")
              } else {
                socket.close()
              }
            }
          } catch (e: Throwable) {
            socket.close()
          }
        }
      }
    }
  }
}

fun main(args: Array<String>) = mainBody {
  ArgParser(args).parseInto(::Args).run {
    SlowwaveTestApplication(this.host, this.port).run()
  }
}

class Args(parser: ArgParser) {
  val host by parser.storing("--host", help = "The listening ip of the server. (Default: 0.0.0.0)") { toString() }
    .default("0.0.0.0")
  val port by parser.storing("-p", "--port", help = "The listening port of the server. (Default: 1234)") { toInt() }
    .default(1234)

}
