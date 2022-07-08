import java.net.InetAddress

trait ShortCodeService {
  def create(url: String): String
}

class DefaultShortCodeService extends ShortCodeService {

  override def create(url: String): String = {
    // return short path
    s"${url.hashCode().toString}"
  }
}

