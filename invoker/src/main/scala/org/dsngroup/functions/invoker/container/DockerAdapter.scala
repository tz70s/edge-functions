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

class DockerAdapter {
  def run(portForward: (Int, Int) = (0, 0), image: String) = {

    val forwardString = portForward match {
      case (0, 0) =>
        ""
      case (_, _) =>
        s"-p ${portForward._1}:${portForward._2}"
    }

    import sys.process._

    s"docker run -d $forwardString $image" !
  }
}

object DockerAdapter {
  implicit val docker = new DockerAdapter
}
