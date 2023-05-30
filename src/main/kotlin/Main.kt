package imageWatermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private fun String.reply() = println(this).run { readln() }
private fun File.itIfExistsOrNull() = if (this.exists()) this else null

private fun createBufferedImage(name: String, type: String): BufferedImage {

    val imageFile = File(name).itIfExistsOrNull() ?: throw InExistentFileException(name)
    val image = ImageIO.read(imageFile)
    if (image.colorModel.numColorComponents != 3) throw InvalidImageColorComponentsException(type)
    if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) throw InvalidImageBitsPerPixelException(type)
    return image
}

private fun BufferedImage.addGridWatermark(
    watermarkImage: BufferedImage,
    outputImageType: Int,
    watermarkImageTransparency: Int,
    watermarkTransparencyPercentage: Int,
    transparencyColor: Color? = null
): BufferedImage {
    val outputImage = BufferedImage(this.width, this.height, outputImageType)
    for (x in 0 until this.width) {
        for (y in 0 until this.height) {

            val i = Color(this.getRGB(x, y))
            val w = Color(watermarkImage.getRGB(x % watermarkImage.width, y % watermarkImage.height), watermarkImageTransparency == Transparency.TRANSLUCENT)
            val color = when {
                w.alpha == 0 || w == transparencyColor -> i
                w.alpha == 255 -> Color(
                    (watermarkTransparencyPercentage * w.red + (100 - watermarkTransparencyPercentage) * i.red) / 100,
                    (watermarkTransparencyPercentage * w.green + (100 - watermarkTransparencyPercentage) * i.green) / 100,
                    (watermarkTransparencyPercentage * w.blue + (100 - watermarkTransparencyPercentage) * i.blue) / 100
                )
                else -> throw InvalidWatermarkImageException()
            }
            outputImage.setRGB(x, y, color.rgb)
        }
    }
    return outputImage
}

private fun BufferedImage.addSingleWatermark(
    watermarkImage: BufferedImage,
    outputImageType: Int,
    watermarkImageTransparency: Int,
    watermarkTransparencyPercentage: Int,
    transparencyColor: Color? = null,
    xPos: Int,
    yPos: Int
): BufferedImage {
    val outputImage = this
    for (x in 0 until watermarkImage.width) {
        for (y in 0 until watermarkImage.height) {

            val i = Color(this.getRGB(x + xPos, y + yPos))
            val w = Color(watermarkImage.getRGB(x, y), watermarkImageTransparency == Transparency.TRANSLUCENT)
            val color = when {
                w.alpha == 0 || w == transparencyColor -> i
                w.alpha == 255 -> Color(
                    (watermarkTransparencyPercentage * w.red + (100 - watermarkTransparencyPercentage) * i.red) / 100,
                    (watermarkTransparencyPercentage * w.green + (100 - watermarkTransparencyPercentage) * i.green) / 100,
                    (watermarkTransparencyPercentage * w.blue + (100 - watermarkTransparencyPercentage) * i.blue) / 100
                )
                else -> throw InvalidWatermarkImageException()
            }
            outputImage.setRGB(x + xPos, y + yPos, color.rgb)
        }
    }
    return outputImage
}

fun main() {

    try {
        // Create the buffered images
        val image = createBufferedImage("Input the image filename:".reply(), "image")
        val watermarkImage = createBufferedImage("Input the watermark image filename:".reply(), "watermark")

        // Compare the dimensions of the two images
        if (image.width < watermarkImage.width || image.height < watermarkImage.height) throw DifferentImagesDimensionsException()

        // Set the watermark's alpha channel and the transparency color
        var watermarkImageTransparency = watermarkImage.transparency
        var transparencyColor: Color? = null
        if (watermarkImageTransparency == Transparency.TRANSLUCENT)
            watermarkImageTransparency = if ("Do you want to use the watermark's Alpha channel?".reply().lowercase() == "yes") Transparency.TRANSLUCENT else Transparency.OPAQUE
        else if ("Do you want to set a transparency color?".reply() == "yes") {
            val transparencyColorInput = "Input a transparency color ([Red] [Green] [Blue]):".reply()
            if (!transparencyColorInput.matches(Regex("\\d+ \\d+ \\d+"))) throw InvalidTransparencyColorException()
            val colors = transparencyColorInput.split(" ").map { it.toInt() }
            if (colors.any { it !in 0..255 }) throw InvalidTransparencyColorException()
            transparencyColor = Color(colors[0], colors[1], colors[2])
        }

        // Set the watermark transparency percentage
        val watermarkTransparencyPercentage = "Input the watermark transparency percentage (Integer 0-100):".reply()
            .toIntOrNull() ?: throw NumberFormatException("The transparency percentage isn't an integer number.")
        if (watermarkTransparencyPercentage !in 0..100) throw TransparencyPercentageOutOfRangeException()

        // Blend the two images and set the output image name
        val outputImage: BufferedImage = when ("Choose the position method (single, grid):".reply()) {
            "single" -> {
                val diffX = image.width - watermarkImage.width
                val diffY = image.height - watermarkImage.height
                val userXY = "Input the watermark position ([x 0-$diffX] [y 0-$diffY]):".reply().split(" ").map { it.toIntOrNull() }
                if (userXY.size != 2 || userXY[0] == null || userXY[1] == null) throw InvalidPositionInputException()
                if (userXY[0] !in 0..diffX || userXY[1] !in 0..diffY) throw PositionInputOutOfRangeException()
                image.addSingleWatermark(watermarkImage, BufferedImage.TYPE_INT_RGB, watermarkImageTransparency, watermarkTransparencyPercentage, transparencyColor, userXY[0]!!.toInt(), userXY[1]!!.toInt())
            }
            "grid" -> image.addGridWatermark(watermarkImage, BufferedImage.TYPE_INT_RGB, watermarkImageTransparency, watermarkTransparencyPercentage, transparencyColor)
            else -> throw InvalidPositionMethodException()
        }
        val outputImageFilename = "Input the output image filename (jpg or png extension):".reply()
        if (!outputImageFilename.matches(Regex(".+\\.(jpg|png)"))) throw InvalidFileNameException()
        val outputImageFile = File(outputImageFilename)

        // Save the result image
        ImageIO.write(outputImage, outputImageFilename.takeLast(3), outputImageFile)
        println("The watermarked image $outputImageFilename has been created.")

    } catch (e: Exception) {
        println(e.message)
    }
}