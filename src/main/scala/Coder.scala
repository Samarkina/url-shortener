import java.nio.charset.StandardCharsets
import java.util.Base64

object Coder {
  def encodeData(data: String): String = {
    // encoding only short part of url (suffix)
    Base64.getEncoder.encodeToString(data.getBytes(StandardCharsets.UTF_8))
  }

  def decodeData(encodedData: Option[String]): String = {
    val decodedArray = Base64.getDecoder.decode(encodedData.get)
    new String(decodedArray, StandardCharsets.UTF_8)
  }

}
