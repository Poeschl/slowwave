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

class SlowwaveTestApplication(private val listeningPort: Int) {

  companion object {
    private val LOGGER = KotlinLogging.logger {}
  }

  fun run() {
    runBlocking {
      val selectorManager = SelectorManager(Dispatchers.IO)
      val serverSocket = aSocket(selectorManager).tcp().bind("localhost", listeningPort)

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
                LOGGER.info { "Echoing $input" }
                sendChannel.writeStringUtf8(input)
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
  ArgParser(args). parseInto(::Args).run {
    SlowwaveTestApplication(this.port).run()
  }
}

class Args(parser: ArgParser) {
  val port by parser.storing("-p", "--port", help = "The listening port of the server. (Default: 1234)") { toInt() }.default(1234)

}
