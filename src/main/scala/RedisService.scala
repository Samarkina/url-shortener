import com.redis.RedisClient

import scala.concurrent.duration.Duration

object RedisService {
  def setDataToRedis(key: String, redisClient: RedisClient, EXPIRE_REDIS_TIME: Duration, keyPrefix: String): Unit = {
    val value = Coder.encodeData(key)
    redisClient.set(key=s"$keyPrefix:$key", value=s"$value", expire=EXPIRE_REDIS_TIME)
  }

  def getEncodedDataFromRedis(key: String, redisClient: RedisClient, keyPrefix: String): Option[String] = {
    val encodedUrl = Coder.encodeData(key)
    redisClient.get(s"$keyPrefix:$encodedUrl")
  }

}
