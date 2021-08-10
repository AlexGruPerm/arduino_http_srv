package webservice

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess, Response}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import org.slf4j.{Logger, LoggerFactory}

class EsClient(val elasticAddress: String, val serviceAddress: String, val elasticIndexName: String) {
  private val client = ElasticClient(JavaClient(ElasticProperties(elasticAddress)))
  private val indexName: String = elasticIndexName
  import com.sksamuel.elastic4s.ElasticDsl._
  val log: Logger = LoggerFactory.getLogger(getClass.getName)

  private def saveUser(userFio: String) :Response[IndexResponse] =
    client.execute {
      indexInto(indexName).fields( "user_fio" -> userFio).refresh(RefreshPolicy.Immediate)
    }.await

  private def searchUserById(searchId: String) :Response[SearchResponse] = client.execute {
    search(indexName).query(idsQuery(searchId))
  }.await

  def insertUser(user_fio: String): Option[String] = saveUser(user_fio) match {
    case failure: RequestFailure =>
      log.error(s"insertUser error = ${failure.error.toString}")
      None
    case results: RequestSuccess[IndexResponse] =>
      Some(serviceAddress + "/user?id=" + results.result.id)
  }

  def searchUser(id: String): Option[String] = searchUserById(id) match {
    case failure: RequestFailure => {
      log.error(s"searchUser error = ${failure.error}")
      None
    }
    case results: RequestSuccess[SearchResponse] => {
      results.result.hits.hits.toList.headOption match {
        case Some(s) => Some[String](s.sourceField("user_fio").toString)
        case _ => None
      }
    }
  }

}
