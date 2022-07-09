import scala.math.abs

object UrlShortnerService {
  def hashCreate(data: String): String = {
    // return short path
    s"${abs(data.hashCode()).toString}"
  }


}
