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

import akka.actor.{Actor, ActorLogging}
import spray.json._

import scala.util.{Failure, Success, Try}
import org.dsngroup.functions.invoker.container.Container


/**
  * The activator actor is respond for parsing and handling activation frame,
  * and create the child container handling actor.
  */
class Activator extends Actor with ActorLogging {

  import org.dsngroup.functions.invoker.container.Containers.dockerApiClient

  override def receive = activate

  /**
    * The activate behavior will parse the incoming json message into activation frame,
    * then, create a child container actor to deal with container related processing.
    * @return Receive
    */
  def activate: Receive = {
    // Received an activation frame for detail handling.
    case message: String =>
      println(s"Get message ! $message")
      ActivationFrame.parse(message) match {
        case Success(v) =>
          val activateContainer = context.actorOf(Container.props, "child-container")
          activateContainer ! v
        case Failure(e) =>
          // ignore this message.
          log.error("The message's json format is not matched.")
      }
  }
}

/** Used for represented as data frame of each control data*/
case class ActivationFrame(containerRuntime: String, codeSegment: String)

object ActivationFrameProtocol extends DefaultJsonProtocol {
  implicit val activationFrameFormat = jsonFormat2(ActivationFrame.apply)
}

object ActivationFrame {

  import ActivationFrameProtocol._

  def parse(rawMessage: String) = {
    Try(rawMessage.parseJson.convertTo[ActivationFrame])
  }
}
