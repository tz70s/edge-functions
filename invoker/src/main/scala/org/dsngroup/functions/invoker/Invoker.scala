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

import java.util.Properties

import akka.actor.{ActorSystem, Props}

import scala.util.{Failure, Success, Try}

object Invoker extends App {

  implicit val system = ActorSystem("invoker-system")
  implicit val invokerContext = InvokerContext()
  implicit val router = Router()

  val activatorRef = system.actorOf(Props[Activator], "default-activator")
  val subscribeActorRef = system.actorOf(SubscribeActor.props(activatorRef, "invoker-cli-test"), "default-subscriber")
}

class InvokerContext {

  private val props = {
    val _props = new Properties()
    val resource = Try(this.getClass.getClassLoader.getResourceAsStream("invoker.properties"))
    resource match {
      case Success(v) =>
        _props.load(v)
        v.close()
      case Failure(e) =>
        println("Error opening invoker config, " + e.getMessage)
        // TODO: Load default configuration
        _props.setProperty("broker-address", "127.0.0.1:1883")
    }
    _props
  }

  val brokerAddress = props.getProperty("broker-address", "127.0.0.1:1883")
}

object InvokerContext {
  def apply(): InvokerContext = new InvokerContext()
}
