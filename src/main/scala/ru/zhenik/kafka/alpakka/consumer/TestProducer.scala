package ru.zhenik.kafka.alpakka.consumer

import java.time.Instant

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

object TestProducer extends App {
  val appConfig: ApplicationConfig = ApplicationConfig(ConfigFactory.load())
  var properties: java.util.Properties = new java.util.Properties()

  properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  properties.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-id-" + Instant.now.toEpochMilli)
  properties.put(ProducerConfig.ACKS_CONFIG, "all")
  properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
  properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

  val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](properties)

  for (i <- 100 until 200)
    producer.send(new ProducerRecord[String, String](appConfig.Kafka.topic, s"key-$i", s"some-value-$i")).get
}
