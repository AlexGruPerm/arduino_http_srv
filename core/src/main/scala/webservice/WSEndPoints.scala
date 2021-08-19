package webservice

import akka.http.scaladsl.Http
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}
import webservice.WSRoutes._

import scala.language.postfixOps
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 *  CRUD:
 *
 *  host = http://127.0.0.1:8080
 *
 *  1) Create
 *  POST  host/user
 *  with body {"user_fio":"Sidorchuk"}
 *  Return:
 *  201 (Created) and 'Location' in header like this: host/user?id=Ov3zMXsBX9B3-hGXP6j3
 *  OR 404 (NotFound) without 409 (Conflict)!
 *
 *  2) Read
 *  GET host/user?id=3
 *  Return:
 *  200 (OK) and response json
 *  {"id" : "3",
 * "fio" : "Sidorov"}
 *  OR 404 (NotFound)
 *
 *  3) Update
 *  PUT host/user?id=2
 *  with body {"user_fio":"NewFio"}
 *  Return:
 *  201 (Created)
 *  OR 404 (NotFound)
 *
 *  4) Delete
 *  DELETE host/user?id=3
 *  Return:
 *  200 (OK)
 *  OR 404 (NotFound)
 *
*/
object WSEndPoints {
  private val log: Logger = LoggerFactory.getLogger(getClass.getName)
  log.info("Service is starting ..........................")
  val (host,port) = ("127.0.0.1",8080)
  implicit val system: ActorSystem = ActorSystem("core")
  import scala.concurrent.duration._
  implicit val timeout: Timeout = Timeout(120 seconds)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  import akka.stream.scaladsl.Source
  private val serviceSource: Source[Http.IncomingConnection, Future[ServerBinding]] = Http(system).newServerAt(host,port)
    .connectionSource()



  private def reqHandler(request: HttpRequest): Future[HttpResponse] = {
    request match {
      case request@HttpRequest(HttpMethods.POST, Uri.Path("/user"), _, _, _) =>
        log.info(s"CREATE request")
        routeCreate(Unmarshal(request.entity).to[String])
      case request@HttpRequest(HttpMethods.GET, Uri.Path("/user"), _, _, _) =>
        log.info(s"READ request")
        routeGet(request.uri.query().toList.headOption)
      case request@HttpRequest(HttpMethods.PUT, Uri.Path("/user"), _, _, _) =>
        log.info(s"UPDATE request")
        routeUpdate(request.uri.query().toList.headOption, Unmarshal(request.entity).to[String])
      case request@HttpRequest(HttpMethods.DELETE, Uri.Path("/user"), _, _, _) =>
        log.info(s"DELETE request")
        routeDelete(request.uri.query().toList.headOption)
      case _ @ HttpRequest(_, Uri.Path("/favicon.ico"), _, _, _) => routeGetFavicon
    }
  }

  val startService: Future[Done] = serviceSource.runForeach {
    conn =>
      conn.handleWithAsyncHandler(
        r => reqHandler(r)
      )
  }

}