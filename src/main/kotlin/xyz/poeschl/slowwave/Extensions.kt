package xyz.poeschl.slowwave

import java.awt.Color

fun String.hexToColor(): Color {
  val red = this.substring(0..1)
  val green = this.substring(2..3)
  val blue = this.substring(4..5)

  return Color(Integer.parseInt(red, 16), Integer.parseInt(green, 16), Integer.parseInt(blue, 16))
}

fun Color.toHex(): String {
  return String.format("%02X%02X%02X", this.red, this.green, this.blue)
}
