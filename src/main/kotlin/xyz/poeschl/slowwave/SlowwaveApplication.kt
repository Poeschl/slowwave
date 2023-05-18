package xyz.poeschl.slowwave

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import xyz.poeschl.kixelflut.PixelMatrix
import xyz.poeschl.slowwave.commands.*
import xyz.poeschl.slowwave.filter.FilterManager
import java.util.concurrent.Executors

class SlowwaveApplication(host: String, listeningPort: Int,
                          width: Int, height: Int,
                          webport: Int,
                          tokenFlag: Boolean, useCountPerToken: Int) {

  companion object {
    private val LOGGER = KotlinLogging.logger {}
  }

  private val selectorManager = SelectorManager(Dispatchers.IO)
  private val serverSocket = aSocket(selectorManager).tcp().bind(host, listeningPort)

  private val pixelMatrix = PixelMatrix(width, height)
  private val statistics = Statistics()
  private val webServer = WebServer(host, webport, pixelMatrix, statistics)

  private val pxCommandFilters = FilterManager<PxRequest>()

  private val helpCommand = Help()
  private val sizeCommand = Size(pixelMatrix)
  private val pxCommand = Px(pxCommandFilters, pixelMatrix, statistics)
  private val offsetCommand = Offset()
  private val tokenCommand = Token(tokenFlag, useCountPerToken, statistics)

  @OptIn(ExperimentalCoroutinesApi::class)
  fun run() {
    pxCommandFilters.addFilter(offsetCommand.getFilter())
    pxCommandFilters.addFilter(tokenCommand.getFilter())

    var openConnections = 0

    runBlocking {
      LOGGER.info { "Server is listening at ${serverSocket.localAddress}" }

      launch {
        webServer.start()
      }

      while (true) {
        try {
          val socket = serverSocket.accept()
          LOGGER.info { "Accepted connection from ${socket.remoteAddress}" }

          launch(Executors.newCachedThreadPool().asCoroutineDispatcher()) {
            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)
            val remoteAddress = socket.remoteAddress.toString()

            openConnections++
            statistics.syncConnectionCount(openConnections)

            try {
              while (true) {
                val input = receiveChannel.readUTF8Line()
                if (input == null) {
                  break;
                }

                val request = Request(remoteAddress, input.split(" "))

                val response =
                    when (request.cmd[0]) {
                      pxCommand.command -> pxCommand.handleCommand(request)
                          tokenCommand.command -> tokenCommand.handleCommand(request)
                          sizeCommand.command -> sizeCommand.handleCommand(request)
                          offsetCommand.command -> offsetCommand.handleCommand(request)
                          helpCommand.command -> helpCommand.handleCommand(request)
                          else -> ""
                        }
                if (response.isNotBlank()) {
                  sendChannel.writeStringUtf8(response + "\n")
                }
              }
            } catch (e: Exception) {
              if (e.message?.contains("Broken pipe") == true) {
                LOGGER.debug { "Broken pipe" }
              } else if (e.message?.contains("Connection reset by peer") == true) {
                LOGGER.debug { "Connection reset by peer" }
              } else {
                LOGGER.error(e) { "Error on socket loop" }
              }
            } finally {
              tokenCommand.removeTokensForSocket(remoteAddress)
              offsetCommand.removeOffsetForSocket(remoteAddress)
              socket.close()
              openConnections--
            }
          }
        } catch (ex: Exception) {
          LOGGER.error(ex) { "General error" }
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

    SlowwaveApplication(host, port, width, height, webport, tokenFlag, useCountPerToken).run()
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

  val tokenFlag by parser.flagging("--token", help = "Enable the TOKEN command. (Default: false)").default(false)
  val useCountPerToken by parser.storing("--useCountPerToken", help = "How often a token can be used (Default: 100)") { toInt() }
      .default(100)
}
