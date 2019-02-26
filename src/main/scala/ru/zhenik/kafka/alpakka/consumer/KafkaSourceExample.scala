package ru.zhenik.kafka.alpakka.consumer

import java.nio.file.{FileSystems, Files}
import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.scaladsl.Consumer.{DrainingControl, committableSource}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy, ThrottleMode}
import akka.stream.scaladsl.{FileIO, Flow, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import ru.zhenik.kafka.alpakka.consumer.FilesSinkExample.fs

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future}

object KafkaSourceExample extends App {

  val appConfig: ApplicationConfig = ApplicationConfig(ConfigFactory.load())
  val bootstrapServers = appConfig.Kafka.bootstrapServers
  val topic = appConfig.Kafka.topic

  implicit val system: ActorSystem = ActorSystem("kafka-source-example")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext
  val fs = FileSystems.getDefault

  println(system.settings.config.getConfig("akka.kafka.consumer"))

  val consumerSettings: ConsumerSettings[String, String] = {
    ConsumerSettings(
      system,
      new StringDeserializer,
      new StringDeserializer
    )
      .withBootstrapServers(appConfig.Kafka.bootstrapServers)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withGroupId("some-consumer-group-1")
      .withClientId("consumer-akka-id-1")
  }

  val committerSettings = CommitterSettings(system)
  val committerSink: Sink[ConsumerMessage.Committable, Future[Done]] = Committer.sink(committerSettings)

  val kafkaSourceCommittable: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] =
    committableSource(consumerSettings, Subscriptions.topics(topic))

  val flowFile: Flow[ConsumerMessage.CommittableMessage[String, String], ConsumerMessage.CommittableOffset, NotUsed] = Flow[ConsumerMessage.CommittableMessage[String, String]]
    .map(msg => {
      val path = Files.createTempFile(fs.getPath("/tmp"), s"${msg.record.key}-${Instant.now.toEpochMilli}", ".log")
      Source.single(ByteString(msg.record.value())).runWith(FileIO.toPath(path)).onComplete(done => {
        if(done.isSuccess) {
          println(s"Commit msg with key: ${msg.record.key()}")
          msg.committableOffset.commitScaladsl()
        }
        else {
          println(s"roll back || do not commit || stop stream || FAILED with msg : ${msg.record.key()}")
          system.terminate()
          //msg.committableOffset.commitScaladsl()
        }
      })
      msg.committableOffset
    })
    .buffer(5, OverflowStrategy.backpressure)
    .throttle(2, Duration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping)


  kafkaSourceCommittable.via(flowFile).runWith(Sink.ignore)

//  kafkaSourceCommittable
//    .via(flowFile)
//    .toMat(committerSink)(Keep.both)
//    .mapMaterializedValue(DrainingControl.apply)
//    .run()
}
