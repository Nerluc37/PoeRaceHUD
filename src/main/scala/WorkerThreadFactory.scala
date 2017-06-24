import java.util.concurrent.ThreadFactory

/**
  * Created by David on 24-Jun-17.
  */
case class WorkerThreadFactory(prefix: String) extends ThreadFactory {

  private var counter = 0

  override def newThread(runnable: Runnable): Thread = {
    counter = counter + 1
    new Thread(runnable, s"${prefix}-${counter}")
  }
}
