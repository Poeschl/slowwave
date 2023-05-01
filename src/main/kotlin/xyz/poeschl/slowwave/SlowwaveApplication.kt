package xyz.poeschl.slowwave

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
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
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.slowwave.commands.*

class SlowwaveApplication(host: String, listeningPort: Int, width: Int, height: Int) {

  companion object {
    private val LOGGER = KotlinLogging.logger {}
  }

  private val selectorManager = SelectorManager(Dispatchers.IO)
  private val serverSocket = aSocket(selectorManager).tcp().bind(host, listeningPort)

  private val pixelMatrix = PixelMatrix(width, height)

  private val helpCommand = Help()
  private val sizeCommand = Size(pixelMatrix)
  private val pixelDrawCommand = PixelDraw(pixelMatrix)
  private val pixelRetrieveCommand = PixelRetrieve(pixelMatrix)
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
    (KotlinLogging.logger(Logger.ROOT_LOGGER_NAME).underlyingLogger as Logger).level =
      if (debug) {
        Level.DEBUG
      } else {
        Level.INFO
      }

    SlowwaveApplication(host, port, width, height).run()
  }
}

class Args(parser: ArgParser) {
  val debug by parser.flagging("--debug", help = "Enable debug output").default(false)
  val host by parser.storing("--host", help = "The listening ip of the server. (Default: 0.0.0.0)") { toString() }
    .default("0.0.0.0")
  val port by parser.storing("-p", "--port", help = "The listening port of the server. (Default: 1234)") { toInt() }
    .default(1234)
  val width by parser.storing("--width", help = "The width of the pixelflut screen. (Default: 100)") { toInt() }
    .default(100)
  val height by parser.storing("--height", help = "The height of the pixelflut screen. (Default: 100)") { toInt() }
    .default(100)

}
