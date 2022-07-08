import java.net.InetAddress
import scala.math.abs

trait ShortCodeService {
  def create(url: String): String
}

class DefaultShortCodeService extends ShortCodeService {

  override def create(url: String): String = {
    // return short path
    s"${abs(url.hashCode()).toString}"
  }
}

