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

import com.spotify.docker.client.DefaultDockerClient

object Containers {

  implicit val dockerApiClient = DefaultDockerClient.fromEnv().build()

  def listCurrentImages() = {
    dockerApiClient.listImages().toString
  }
}

sealed trait ContainerRuntime

case class NodeJsV8(containerRuntimeName: String = "nodejs-v8") extends ContainerRuntime
case class NodeJsV6(containerRuntimeName: String = "nodejs-v6") extends ContainerRuntime
case class PythonV3(containerRuntimeName: String = "python-v3") extends ContainerRuntime
case class PythonV2(containerRuntimeName: String = "python-v2") extends ContainerRuntime

