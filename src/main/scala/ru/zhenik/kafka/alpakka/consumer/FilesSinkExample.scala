package ru.zhenik.kafka.alpakka.consumer

import java.nio.file.{FileSystems, Files}
import java.time.Instant

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult, Materializer}
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}

object FilesSinkExample extends App {
  implicit val system: ActorSystem = ActorSystem("files-sink-example")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext

  val fs = FileSystems.getDefault

  // !!! be aware of blocking-io-dispatcher
  // https://doc.akka.io/docs/akka/current/stream/stream-io.html#streaming-file-io
  val collections = Seq(("key-1", "value-1"), ("key-2", "value-2"), ("key-3", "value-3")).toList


  val source: Source[(String, String), NotUsed] = Source(collections)

  val flow: Flow[(String, String), Unit, NotUsed] = Flow[(String, String)]
    .map(tuple => {
      //todo: handle exceptions
      val path = Files.createTempFile(fs.getPath("/tmp"), s"${tuple._1}-${Instant.now.toEpochMilli}", ".log")

      val complete = Source.single(ByteString(tuple._2)).runWith(FileIO.toPath(path))

      complete.onComplete(done => {
          if (done.isSuccess) println("Commit")
          else println("roll back || do not commit || stop stream")
        })

    })
    .log("DEBUG")


  val complete: Future[Done] = source.via(flow).runWith(Sink.ignore)
  complete.onComplete( done => {
    system.terminate()
  })


  //    .runWith(Sink.ignore)
}
