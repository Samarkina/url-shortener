package route

import akka.http.scaladsl.server.{Directives, Route}
import util.HtmlTemplates

case class MainPageRoute()
  extends Directives
  with HttpConfig {

  val routes: Route = pathEndOrSingleSlash {
    complete(HtmlTemplates.createMainPage())
  }
}
