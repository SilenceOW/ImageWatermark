package imageWatermark

class InExistentFileException(fileName: String) : Exception("The file $fileName doesn't exist.")
class DifferentImagesDimensionsException : Exception("The watermark's dimensions are larger.")
class TransparencyPercentageOutOfRangeException : Exception("The transparency percentage is out of range.")
class InvalidImageBitsPerPixelException(imageType: String) : Exception("The $imageType isn't 24 or 32-bit.")
class InvalidImageColorComponentsException(imageType: String) : Exception("The number of $imageType color components isn't 3.")
class InvalidFileNameException : Exception("The output file extension isn't \"jpg\" or \"png\".")
class InvalidWatermarkImageException : Exception("The watermark image has some pixels with alpha channel not equal to 0 or 255")
class InvalidTransparencyColorException : Exception("The transparency color input is invalid.")
class InvalidPositionMethodException : Exception("The position method input is invalid.")
class InvalidPositionInputException : Exception("The position input is invalid.")
class PositionInputOutOfRangeException : Exception("The position input is out of range.")