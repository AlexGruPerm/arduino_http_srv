package webservice

import akka.http.scaladsl.Http
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}
import webservice.WSRoutes._

import scala.language.postfixOps
import scala.concurrent.{ExecutionContextExecutor, Future}

object WSEndPoints {
  private val log: Logger = LoggerFactory.getLogger(getClass.getName)
  log.info("Service is starting ..........................")
  val (host,port) = ("192.168.1.2",8081) //ip in local network + FW + Router ports transfer
  implicit val system: ActorSystem = ActorSystem("core")
  import scala.concurrent.duration._
  implicit val timeout: Timeout = Timeout(60 seconds)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  import akka.stream.scaladsl.Source
  private val serviceSource: Source[Http.IncomingConnection, Future[ServerBinding]] = Http(system).newServerAt(host,port)
    .connectionSource()



  private def reqHandler(request: HttpRequest): Future[HttpResponse] = {
    request match {

      case request@HttpRequest(HttpMethods.GET, Uri.Path("/bh1750"), _, _, _) =>
        log.info(s"READ request")
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