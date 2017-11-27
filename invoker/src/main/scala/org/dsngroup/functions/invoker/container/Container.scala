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

package org.dsngroup.functions.invoker.container

import akka.actor.{Actor, ActorLogging, Props}
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.ContainerConfig
import org.dsngroup.functions.invoker.ActivationFrame

class Container(implicit val dockerApiClient: DefaultDockerClient) extends Actor with ActorLogging {

  override def receive: Receive = execCode

  def execCode: Receive = {
    case ActivationFrame(cr, cs) =>
      cr match {
        case "python" =>
          val container = dockerApiClient.createContainer(ContainerConfig.builder().image(cr)
              .exposedPorts("8080")
              .cmd("python", "-m", "http.server", "8080")
              .build())
          // Start container
          dockerApiClient.startContainer(container.id())
      }
  }
}

object Container {
  def apply(implicit defaultDockerClient: DefaultDockerClient) = new Container()
  def props(implicit defaultDockerClient: DefaultDockerClient): Props = Props(new Container())
}
