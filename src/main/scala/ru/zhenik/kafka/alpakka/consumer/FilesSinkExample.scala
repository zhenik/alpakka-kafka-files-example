package ru.zhenik.kafka.alpakka.consumer

import java.nio.file.{FileSystems, Files}
import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString

import scala.concurrent.ExecutionContextExecutor

object FilesSinkExample extends App {
  implicit val system: ActorSystem = ActorSystem("files-sink-example")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext

  val fs = FileSystems.getDefault

  // !!! be aware of blocking-io-dispatcher
  // https://doc.akka.io/docs/akka/current/stream/stream-io.html#streaming-file-io
  val textKeyValue = Source(Seq(("test1", "test2"), ("test3", "test4"), ("test5", "test6")).toList)
  val result = textKeyValue
    .map(tuple => {
      //todo: handle exceptions
      val path = Files.createTempFile(fs.getPath("/tmp"), s"${tuple._1}-${Instant.now.toEpochMilli}", ".log")
      Source.single(ByteString(tuple._2)).runWith(FileIO.toPath(path)).onComplete(done => {
        if (done.isSuccess) println("Commit")
        else println("roll back || do not commit || stop stream")
      })
    })
    .log("DEBUG")
    .runWith(Sink.ignore)
}
