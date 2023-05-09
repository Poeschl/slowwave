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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.slowwave.commands.Help
import xyz.poeschl.slowwave.commands.Offset
import xyz.poeschl.slowwave.commands.Px
import xyz.poeschl.slowwave.commands.Size

class SlowwaveApplication(host: String, listeningPort: Int, width: Int, height: Int, webport: Int) {

  companion object {
    private val LOGGER = KotlinLogging.logger {}
  }

  private val selectorManager = SelectorManager(Dispatchers.IO)
  private val serverSocket = aSocket(selectorManager).tcp().bind(host, listeningPort)

  private val pixelMatrix = PixelMatrix(width, height)
  private val statistics = Statistics()
  private val webServer = WebServer(host, webport, pixelMatrix, statistics)

  private val helpCommand = Help()
  private val sizeCommand = Size(pixelMatrix)
  private val pxCommand = Px(pixelMatrix, statistics)
  private val offsetCommand = Offset()

  @OptIn(ExperimentalCoroutinesApi::class)
  fun run() {
    runBlocking {
      LOGGER.info { "Server is listening at ${serverSocket.localAddress}" }

      launch {
        webServer.start()
      }

      while (true) {
        val socket = serverSocket.accept()
        LOGGER.info { "Accepted connection from ${socket.remoteAddress}" }
        launch(Dispatchers.IO) {
          val receiveChannel = socket.openReadChannel()
          val sendChannel = socket.openWriteChannel(autoFlush = true)

          try {
            while (receiveChannel.availableForRead > 0) {
              val input = receiveChannel.readUTF8Line()
              if (input != null) {
                val parsedCmd = input.split(" ")

                val response =
                    when (parsedCmd[0]) {
                      helpCommand.command -> helpCommand.handleCommand(parsedCmd)
                      sizeCommand.command -> sizeCommand.handleCommand(parsedCmd)
                      pxCommand.command -> pxCommand.handleCommand(parsedCmd)
                      offsetCommand.command -> offsetCommand.handleCommand(parsedCmd)
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

    SlowwaveApplication(host, port, width, height, webport).run()
  }
}

class Args(parser: ArgParser) {
  val debug by parser.flagging("--debug", help = "Enable debug output").default(false)
  val host by parser.storing("--host", help = "The listening ip of the server. (Default: 0.0.0.0)") { toString() }
      .default("0.0.0.0")
  val port by parser.storing("--port", help = "The listening port of the server. (Default: 1234)") { toInt() }
      .default(1234)
  val webport by parser.storing("--web-port", help = "The listening port of the web server. (Default: 8080)") { toInt() }
      .default(8080)
  val width by parser.storing("--width", help = "The width of the pixelflut screen. (Default: 100)") { toInt() }
      .default(100)
  val height by parser.storing("--height", help = "The height of the pixelflut screen. (Default: 100)") { toInt() }
      .default(100)

}
