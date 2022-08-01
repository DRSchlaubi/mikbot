package dev.nycode.imagecolor.kord

import dev.nycode.imagecolor.Image
import dev.nycode.imagecolor.ImageFormat
import dev.kord.rest.Image as KordImage

public fun KordImage.toImage(): Image = Image(data, format.asImageFormat())

public fun KordImage.Format.asImageFormat(): ImageFormat {
    return when (extension) {
        "jpeg" -> ImageFormat.JPEG
        "png" -> ImageFormat.PNG
        "webp" -> ImageFormat.WEBP
        "gif" -> ImageFormat.GIF
        else -> error("Invalid image format: $extension")
    }
}
