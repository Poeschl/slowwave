package xyz.poeschl.slowwave

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import xyz.poeschl.kixelflut.PixelMatrix
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream


class ImageServer(host: String, port: Int, private val pixelMatrix: PixelMatrix) {

  companion object {
    private val LOGGER = KotlinLogging.logger {}

    private const val BYTE_UPDATE_INTERVAL: Long = 100
    private const val SLEEP_TIMER_DURATION: Long = 200
    private const val BOUNDARY_MARKER = "--boundary"
    private val IMAGE_UPDATE_HEAD_TEMPLATE = """
            
            $BOUNDARY_MARKER
            Content-Type: image/jpeg
            Content-Length: %s
            
        """.trimIndent()
  }

  private var imageData = ByteArray(0)

  @Suppress("ExtractKtorModule")
  private val webserver = embeddedServer(Netty, host = host, port = port) {
    routing {
      get("/") {
        handleWebCall(call)
      }
    }
  }

  fun start() {
    LOGGER.info { "Starting web server" }

    Timer().schedule(object : TimerTask() {
      override fun run() = updateImage()
    }, 0L, BYTE_UPDATE_INTERVAL)

    webserver.start(wait = true)
  }

  private fun updateImage() {
    val shadowBytes = imageToPngBytes(pixelMatrixToImage(pixelMatrix))
    imageData = shadowBytes
  }

  private fun pixelMatrixToImage(pixelMatrix: PixelMatrix): BufferedImage {
    val img = BufferedImage(pixelMatrix.width, pixelMatrix.height, BufferedImage.TYPE_INT_RGB)

    // background
    val canvas = img.graphics
    canvas.color = Color(0, 0, 0)
    canvas.fillRect(0, 0, img.width, img.height)

    // pixel drawing
    pixelMatrix.processData { point, color ->
      canvas.color = color
      canvas.fillRect(point.x, point.y, 1, 1)
    }

    canvas.dispose()
    return img
  }

  private fun imageToPngBytes(inputImage: BufferedImage): ByteArray {
    val bytesStream = ByteArrayOutputStream()
    val imageOutputStream = MemoryCacheImageOutputStream(bytesStream)

    val imageWriter = ImageIO.getImageWritersByFormatName("png").next()
    imageWriter.output = imageOutputStream
    imageWriter.write(null, IIOImage(inputImage, null, null), null)
    imageWriter.dispose()
    imageOutputStream.close()

    val bytes = bytesStream.toByteArray()
    bytesStream.close()
    return bytes
  }

  private suspend fun handleWebCall(call: ApplicationCall) {
    call.response.header("Cache-Control", "no-cache, private")
    call.response.status(HttpStatusCode.OK)
    call.respondBytes(imageData, ContentType("image", "png"))

//    call.respondOutputStream(
//      ContentType(
//        "multipart", "x-mixed-replace",
//        listOf(HeaderValueParam("boundary", BOUNDARY_MARKER))
//      )
//    ) {
//      try {
//
//        while (true) {
//          this.write(IMAGE_UPDATE_HEAD_TEMPLATE.format(imageData.size).toByteArray())
//          this.write(imageData)
//          this.flush()
//          Thread.sleep(SLEEP_TIMER_DURATION)
//        }
//      } catch (ex: ChannelWriteException) {
//        LOGGER.debug { "Write canceled. Client disconnected" }
//      }
//    }
  }
}




