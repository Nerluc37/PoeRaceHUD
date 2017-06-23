/**
  * Created by David on 23-Jun-17.
  */
case class APIResponseException(message:String) extends Exception {
  println(message)
}

