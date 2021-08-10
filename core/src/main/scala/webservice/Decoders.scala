package webservice

import io.circe._
import io.circe.parser._

object Decoders {

  implicit val decoderUserFio: Decoder[UserFio] = Decoder.instance { h =>
    for {
      fio <- h.get[String]("user_fio")
    } yield UserFio(fio)
  }

  implicit val decoderUser: Decoder[User] = Decoder.instance { h =>
    for {
      userId <- h.get[String]("user_id")
      userFio <- h.get[String]("user_fio")
    } yield User(userId,userFio)
  }





}
