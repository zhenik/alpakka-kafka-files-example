package ru.zhenik.kafka.alpakka.consumer

import java.nio.file._
import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.scaladsl.Consumer._
import akka.stream.alpakka.file.scaladsl.LogRotatorSink
import akka.stream.scaladsl.{FileIO, Flow, Keep, Source}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContextExecutor

object Application extends App {

  val appConfig: ApplicationConfig = ApplicationConfig(ConfigFactory.load())
  val bootstrapServers = appConfig.Kafka.bootstrapServers
  val topic = appConfig.Kafka.topic

  implicit val system: ActorSystem = ActorSystem("kafka-files-integration")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext

  val consumerSettings: ConsumerSettings[String, String] = {
    ConsumerSettings(
      system.settings.config.getConfig("akka.kafka.consumer"),
      new StringDeserializer,
      new StringDeserializer
    )
      .withBootstrapServers(appConfig.Kafka.bootstrapServers)
      .withGroupId("some-consumer-group-1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }

  val committerSettings = CommitterSettings(system)

  val kafkaSource =
    committableSource(consumerSettings, Subscriptions.topics(topic))
      .map(msg => {
        Source.single(msg)
          .map(value => ByteString(value.record.value()))
          .runWith(LogRotatorSink(fileSizeRotationFunction))
        msg.committableOffset
      })
      .toMat(Committer.sink(committerSettings))(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()


  val fileSizeRotationFunction = () => {
    val max = 10 * 1024 * 1024
    var size: Long = max
    (element: ByteString) => {
      if (size + element.size > max) {
        val path = Files.createTempFile("out-", ".log")
        size = element.size
        Some(path)
      } else {
        size += element.size
        None
      }
    }
  }



}
