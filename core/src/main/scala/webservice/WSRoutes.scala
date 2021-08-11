package webservice

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import io.circe.parser.parse
import scala.concurrent.{Await, Future}
import Decoders._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.FileIO
import org.slf4j.{Logger, LoggerFactory}
import java.io.File

object WSRoutes {

  import io.circe.generic.auto._, io.circe.syntax._
  import scala.concurrent.duration._
  val log: Logger = LoggerFactory.getLogger(getClass.getName)

  val esClient: EsClient = new EsClient(
    "http://127.0.0.1:9200",
    "http://127.0.0.1:8080",
    "users")

  private val parseCreateUserData: Future[String] => Future[UserFio] = futString => {
    val strRequest: String  = Await.result(futString, 1 second)
    log.info(s"strRequest = $strRequest")
    parse(strRequest) match {
      case Left (failure) => Future.failed(
        ReqParseException("Error code[001] Invalid json in request", failure.getCause)
      )
      case Right (json) => json.as[UserFio].swap
      match {
        case   Left(userFio) => Future.successful(userFio)
        case   Right(failure) =>  Future.failed(
          ReqParseException("Error code[002] Invalid json in request", failure.getCause)
        )
      }
    }
  }


  import scala.concurrent.ExecutionContext.Implicits.global
  def routeCreate(reqEntity: Future[String]): Future[HttpResponse] = {
    val futureInsertFio: Future[HttpResponse] = parseCreateUserData(reqEntity).map{
      case UserFio(fio) => esClient.insertUser(fio)
      match {
        case Some(saveResult) => HttpResponse(status = StatusCodes.Created, headers = Seq(RawHeader("Location", saveResult)))
        case _ => HttpResponse(StatusCodes.NotFound)
      }
      case _ => HttpResponse(StatusCodes.NotFound)
    }
    val insertHttpResult: HttpResponse = Await.result(futureInsertFio, 1 second)
    Future.successful(insertHttpResult)
  }


  def routeGet(queryPair: Option[(String,String)]): Future[HttpResponse] = {
    val httpResponse = queryPair match {
      case Some((_, searchId)) =>
        esClient.searchUser(searchId) match {
          case Some(result) => HttpResponse(StatusCodes.OK, entity = User(searchId, result).asJson.toString())
          case _ => HttpResponse(StatusCodes.NotFound)
        }
      case _ => HttpResponse(StatusCodes.NotFound)
    }
    Future.successful(httpResponse)
  }

  def routeUpdate(queryPair: Option[(String,String)], reqEntity: Future[String]): Future[HttpResponse] = {
    val httpResponse: Future[HttpResponse] = queryPair match {
      case Some((_, updateId)) =>
        parseCreateUserData(reqEntity).map{
          case UserFio(newfio) => esClient.updateUser(updateId, newfio)
          match {
            case Some(saveResult) => HttpResponse(status = StatusCodes.Created, headers = Seq(RawHeader("Location", saveResult)))
            case _ => HttpResponse(StatusCodes.NotFound)
          }
          case _ => HttpResponse(StatusCodes.NotFound)
        }
      case _ => Future(HttpResponse(StatusCodes.NotFound))
    }

    val insertHttpResult: HttpResponse = Await.result(httpResponse, 1 second)
    Future.successful(insertHttpResult)
  }


  def routeDelete(queryPair: Option[(String,String)]): Future[HttpResponse] = {
    val httpResponse = queryPair match {
      case Some((_, searchId)) =>
        esClient.deleteUser(searchId) match {
          case Some(result) => HttpResponse(StatusCodes.OK)
          case _ => HttpResponse(StatusCodes.NotFound)
        }
      case _ => HttpResponse(StatusCodes.NotFound)
    }
    Future.successful(httpResponse)
  }

  def routeGetFavicon: Future[HttpResponse] = {
    //todo: fix it and take from resource folder
    val icoFile = new File("F:\\PROJECTS\\ws_ora\\src\\main\\resources\\favicon.png")
    Future.successful(
      HttpResponse(StatusCodes.OK, entity =
        HttpEntity(MediaTypes.`application/octet-stream`, icoFile.length, FileIO.fromPath(icoFile.toPath))
      )
    )
  }

}
