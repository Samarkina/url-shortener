import Main.EXPIRE_REDIS_TIME
import akka.http.scaladsl.model.Uri
import com.redis.RedisClient
import util.Coder

case class RedisService(redisClient: RedisClient) {

  def setValue(key: String, value: String, keyPrefix: String): Unit = {
    val encodedKey = Coder.encodeData(key)
    redisClient.set(key=s"$keyPrefix:$encodedKey", value=value, expire=EXPIRE_REDIS_TIME)
  }

  def getValue(key: String, keyPrefix: String): String = {
    val encodedUrl = Coder.encodeData(key)
    redisClient.get(s"$keyPrefix:$encodedUrl")  match {
      case None =>
        // user called not existing url
        throw new IllegalArgumentException("Requested short URL doesn't exist.")
      case Some(value) => value
    }
  }

}
