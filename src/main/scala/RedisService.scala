import Main.EXPIRE_REDIS_TIME
import com.redis.RedisClient

import scala.concurrent.duration.Duration

case class RedisService(redisClient: RedisClient) {

  def setValue(key: String, value: String, keyPrefix: String): Unit = {
    val encodedKey = Coder.encodeData(key)
    redisClient.set(key=s"$keyPrefix:$encodedKey", value=value, expire=EXPIRE_REDIS_TIME)
  }

  def getValue(key: String, keyPrefix: String): Option[String] = {
    val encodedUrl = Coder.encodeData(key)
    redisClient.get(s"$keyPrefix:$encodedUrl")
  }

}
