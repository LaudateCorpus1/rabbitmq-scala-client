package com.avast.clients.rabbitmq.api

import scala.language.higherKinds

trait RabbitMQConsumer[F[_]] extends FAutoCloseable[F]
