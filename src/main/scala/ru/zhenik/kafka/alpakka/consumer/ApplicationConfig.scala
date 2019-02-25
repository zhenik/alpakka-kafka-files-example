package ru.zhenik.kafka.alpakka.consumer

import com.typesafe.config.Config

final case class ApplicationConfig(config: Config) {
  object Kafka {
    val bootstrapServers: String = config.getString("stream-graph.kafka-source.bootstrap-servers")
    val schemaRegistryUrl: String = config.getString("stream-graph.kafka-source.schema-registry-url")
    val topic: String = config.getString("stream-graph.kafka-source.topic-name")
  }
  object FileSystem
}
