/*
 * Copyright (c) 2017 Dependable Network and System Lab, National Taiwan University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dsngroup.functions.invoker

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.sandinh.paho.akka._

import scala.concurrent.duration._

object Router {
  def apply()(implicit actorSystem: ActorSystem, invokerContext: InvokerContext) = new Router()
}

class Router()(implicit val actorSystem: ActorSystem, val invokerContext: InvokerContext) {

  private[invoker] val pubsub = actorSystem.actorOf(Props(classOf[MqttPubSub], PSConfig(
    brokerUrl = s"tcp://${invokerContext.brokerAddress}",
    stashTimeToLive = 1.minute,
    stashCapacity = 8000,
    reconnectDelayMin = 10.millis,
    reconnectDelayMax = 30.seconds
  )))

}

object SubscribeActor {
  def props(activator: ActorRef, topic: String)(implicit router: Router): Props =
    Props(new SubscribeActor(activator, topic))
}

class SubscribeActor(val activator: ActorRef, val topic: String)(implicit val router: Router)
  extends Actor with ActorLogging {

  import context._

  router.pubsub ! Subscribe(topic, self)

  override def receive: Receive = {
    case SubscribeAck(Subscribe(topic, self, _), fail) =>
      if (fail.isEmpty) {
        become(ready)
      } else {
        log.error(fail.get, s"can't subscribe to $topic")
      }
  }

  def ready: Receive = {
    case msg: Message =>
      activator ! new String(msg.payload)
  }
}

