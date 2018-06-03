/*
 * Copyright (c) 2017 Tzu-Chiao Yeh.
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
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.{ContainerConfig, ContainerState, HostConfig, PortBinding}
import org.dsngroup.functions.invoker.ActivationFrame
import spray.json.DefaultJsonProtocol
import spray.json._
import scala.util.Try

class Container(implicit val dockerApiClient: DefaultDockerClient, val docker: DockerAdapter)
  extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  override def receive: Receive = execCode

  private val http = Http(context.system)

  def execCode: Receive = {

    case ActivationFrame(cr, cs) =>
      cr match {
        case "python" =>

          // TODO: Consider to clean up docker api, used native bash commands or custom http client instead.
          // val container = dockerApiClient.createContainer(ContainerConfig.builder().image("tz70s/edge-func-action-py3")
          //    .build())

          // Start container
          // dockerApiClient.startContainer(container.id())
          // val containerIpAddress = dockerApiClient.inspectContainer(container.id()).networkSettings().ipAddress()

          docker.run((8080, 8080), "tz70s/edge-func-action-py3")

          import ContainerActionEntryProtocol._

          val entry = ContainerActionEntry("handler", "handler.py", cs, "{\"hello\": \"world\"}")
            .toJson(ContainerActionEntryJsonFormat)

          // Handle request to container
          val execObjectEntity = HttpEntity(ContentTypes.`application/json`, entry.toString())
          http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = s"http://0.0.0.0:8080/runc",
            entity = execObjectEntity)).pipeTo(self)
          // Bump to receive msg behavior
          context become(recvMsg)
      }
  }

  def recvMsg: Receive = {
    // TODO: Close actor after receiving responses?
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach {
        body =>
          log.info("Got response! body: \n" + body.utf8String)
      }
      context stop self

    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach {
        body =>
          log.info("Error" + body.utf8String)
      }
      context stop self
  }
}

object Container {
  def apply(implicit defaultDockerClient: DefaultDockerClient, dockerAdapter: DockerAdapter) = new Container()
  def props(implicit defaultDockerClient: DefaultDockerClient, dockerAdapter: DockerAdapter): Props = Props(new Container())
}

/** Used for represented as data frame of each control data*/
class ContainerActionEntry(val handlerName: String, val codeName: String, val codeSegment: String,
                           val continuation: String)

object ContainerActionEntryProtocol extends DefaultJsonProtocol {

  // implicit val activationFrameFormat = jsonFormat4(ContainerActionEntry.apply)

  implicit object ContainerActionEntryJsonFormat extends RootJsonFormat[ContainerActionEntry] {
    def write(c: ContainerActionEntry) =
      JsObject(Map(
        "handler-name" -> JsString(c.handlerName),
        "code-name" -> JsString(c.codeName),
        "code-seg" -> JsString(c.codeSegment),
        "continuation" -> JsString(c.continuation)
      ))

    // TODO: Reader implementation
    override def read(json: JsValue): ContainerActionEntry = ???
  }
}

object ContainerActionEntry {

  def apply(handlerName: String, codeName: String, codeSegment: String, continuation: String) =
    new ContainerActionEntry(handlerName, codeName, codeSegment, continuation)

  import ContainerActionEntryProtocol._

  def parse(rawMessage: String) = {
    Try(rawMessage.parseJson.convertTo[ContainerActionEntry])
  }
}