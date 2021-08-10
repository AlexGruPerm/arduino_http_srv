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
 *  1) Create
 *  POST  http://localhost:8080/user
 *  with body {"user_fio":"Sidorchuk"}
 *  Return:
 *  201(Created) and 'Location' in header like this: http://127.0.0.1:8080/user?id=Ov3zMXsBX9B3-hGXP6j3
 *  OR return 404 (NotFound) without 409 (Conflict)!
 *
 *  2) Read
 *  GET http://127.0.0.1:8080/user?id=3
 *  Return:
 *  200(OK) and response json
 *  {"id" : "3",
 * "fio" : "Sidorov"}
 *  OR return 404 (NotFound)
 *
 *
 * 3) Update
 *
 *
 * 4) Delete
 *
 *
*/
object WSEndPoints {
  val log: Logger = LoggerFactory.getLogger(getClass.getName)
  log.info("Service is starting ..........................")
  val (host,port) = ("127.0.0.1",8080)
  implicit val system: ActorSystem = ActorSystem("core")
  import scala.concurrent.duration._
  implicit val timeout: Timeout = Timeout(120 seconds)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  import akka.stream.scaladsl.Source
  val serviceSource: Source[Http.IncomingConnection, Future[ServerBinding]] = Http(system).newServerAt(host,port)
    .connectionSource()

  private def reqHandler(request: HttpRequest): Future[HttpResponse] = {
    request match {
      case request@HttpRequest(HttpMethods.POST, Uri.Path("/user"), _, _, _) =>
        routeCreate(Unmarshal(request.entity).to[String])
      case request@HttpRequest(HttpMethods.GET, Uri.Path("/user"), _, _, _) =>
        routeGet(request.uri.query().toList.headOption)
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