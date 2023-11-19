package webservice

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import io.circe.parser.parse
import scala.concurrent.Future
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.FileIO
import org.slf4j.{Logger, LoggerFactory}
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global

object WSRoutes {

  private val log: Logger = LoggerFactory.getLogger(getClass.getName)

  def routeGet(queryPair: Option[(String, String)]): Future[HttpResponse] =
    Future.successful(queryPair).flatMap{
      case Some((_, lux)) =>
        println(s"lux = $lux")
        Future(HttpResponse(StatusCodes.OK, entity = s"lux = $lux"))
      case _ => Future(HttpResponse(StatusCodes.NotFound))
    }

  def routeGetFavicon: Future[HttpResponse] = {
    //todo: fix it and take from resource folder
    val icoFile = new File("F:\\PROJECTS\\web_serv_api_core\\core\\src\\main\\resources\\favicon.png")
    Future.successful(
      HttpResponse(StatusCodes.OK, entity =
        HttpEntity(MediaTypes.`application/octet-stream`, icoFile.length, FileIO.fromPath(icoFile.toPath))
      )
    )
  }

}
