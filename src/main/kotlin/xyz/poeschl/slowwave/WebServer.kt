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


class WebServer(host: String, port: Int, private val pixelMatrix: PixelMatrix, private val statistics: Statistics) {

  companion object {
    private val LOGGER = KotlinLogging.logger {}

    private const val BYTE_UPDATE_INTERVAL: Long = 100
    private const val WEBPAGE_IMAGE_UPDATE_INTERVAL = 250
  }

  private var imageData = ByteArray(0)

  @Suppress("ExtractKtorModule")
  private val webserver = embeddedServer(Netty, host = host, port = port) {
    routing {
      get("") {
        handleWebpageCall(call)
      }
      get("/image") {
        handleImageWebCall(call)
      }
      get("/stats") {
        call.respondText(ContentType("text", "json"), HttpStatusCode.OK) {
          """
                        { "pixel_per_second": ${statistics.pixelPerSecond} }
                    """.trimIndent()
        }
      }
    }
  }

  fun start() {
    LOGGER.info { "Starting web server" }

    Timer().schedule(object : TimerTask() {
      override fun run() = updateImage()
    }, 0L, BYTE_UPDATE_INTERVAL)


    webserver.start(wait = false)
  }

  private suspend fun handleWebpageCall(call: ApplicationCall) {
    call.response.header("Cache-Control", "no-cache, private")
    call.respondText(ContentType("text", "html"), HttpStatusCode.OK) {
      """
        <!DOCTYPE html>
        <html lang="en">
          <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
        
            <title>Slowwave</title>
          </head>
        
          <body>
            <img id = "image"
                width='${pixelMatrix.width}' height='${pixelMatrix.height}' 
                src='/image' alt="Current pixelflut canvas"/>
            <div id = "stats"></div>
            <script type='application/javascript'>
              window.setInterval(() => {
                document.getElementById("image").src = "/image" + "?t=" + new Date().getTime()
                fetch("/stats")
                  .then(response => response.text()
                    .then(text => document.getElementById("stats").textContent = text))
              }, ${WEBPAGE_IMAGE_UPDATE_INTERVAL});
            </script>
          </body>
        </html>
      """.trimIndent()
    }
  }

  private suspend fun handleImageWebCall(call: ApplicationCall) {
    call.response.header("Cache-Control", "no-cache, private")
    call.response.status(HttpStatusCode.OK)
    call.respondBytes(imageData, ContentType("image", "png"))
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
}
