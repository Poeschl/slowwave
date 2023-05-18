package xyz.poeschl.slowwave.commands

import xyz.poeschl.slowwave.Request

class Help : BaseCommand {

    override val command = "HELP"
    override suspend fun handleCommand(request: Request): String = """
               _.====.._
             ,:._       ~-_
                 `\        ~-_
                   |          `.
                 ,/             ~-_
        -..__..-''                ~~--..__...-
        Slowwave Pixelflut Server
        (https://github.com/Poeschl/slowwave)
        
        Available commands:
        HELP: Show this help
        PX x y rrggbb: Color the pixel (x,y) with the given hexadecimal color
        PX x y: Get the color value of the pixel, Example reply: `PX 1 1 ff0033`
        SIZE: Get the size of the drawing surface, Example reply: `SIZE 1920 1080`
        OFFSET x y: Apply offset (x,y) to all your future commands
    """.trimIndent()
}
