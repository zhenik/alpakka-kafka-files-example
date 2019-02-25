package ru.zhenik.kafka.alpakka.consumer

import com.typesafe.config.ConfigFactory

object Application extends App {
  val config = ApplicationConfig(ConfigFactory.load())
  println(config)

}
