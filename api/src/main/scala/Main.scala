import translator.WSEndPoints

object Main {
  def main(args: Array[String]): Unit = {
    //todo: of course, we need to validate input parameters.
    val apiService = new WSEndPoints(args(0),args(1).toInt)
    val service = apiService.startService
  }
}