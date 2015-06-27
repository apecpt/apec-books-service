package pt.org.apec.services.books.test

import org.scalatest.Suite
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterAll
import tugboat._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * @author ragb
 */
trait PostgresqlDockerContainer extends BeforeAndAfterAll{ 
  self : Suite =>
    
    def databaseName = "test"
    def databaseUser = "test"
def databasePassword = "test"
def databaseHostName = "localhost"


    private val dockerClient = Docker()

     private var containerId : String = null
     
    
    override def beforeAll() {
      val (_, completedPull) = dockerClient.images.pull("sameersbn/postgresql")
      .tag("9.4")
      .stream {
        case Pull.Progress(msg, _, details) => {
          println(msg)
          details foreach println
        }
        case Pull.Error(msg, _) => println(msg)
        case Pull.Status(msg) => println(msg)
      }
      Await.result(completedPull, 10 minutes)
      val createResponseFuture = dockerClient.containers.create("sameersbn/postgresql:9.4")
      .env("DB_NAME" -> databaseName,
          "DB_USER" -> databaseUser,
          "db_PASS" -> databasePassword)()
          this.containerId = Await.result(createResponseFuture, 10 seconds).id
          val startFuture = dockerClient.containers.get(containerId)
          .start
          .portBind(Port.Tcp(5432), PortBinding.local(5432))()
          
          Await.result(startFuture, 10 seconds)
    }
  
    override def afterAll() {
      val container = dockerClient.containers.get(containerId)
      val stopFuture = container.stop(5 seconds)()
      Await.result(stopFuture, 5 seconds)
      val deleteFuture = container.delete()
      Await.result(deleteFuture, 5 seconds)
   }
}