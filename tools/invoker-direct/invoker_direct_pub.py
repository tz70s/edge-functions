# The command utils for testing invoker
import paho.mqtt.client as mqtt
import sys
import json

def file_handler():
    codeSeg = ""
    for line in sys.stdin:
       codeSeg += line
    return codeSeg

broker_address = "140.112.42.89"
broker_port = 1883

testing_topic = "invoker-cli-test"

codeSeg = file_handler()
mqttc = mqtt.Client("direct-publish-client")
# bind the connect handler
mqttc.connect(broker_address, broker_port, 60)

command = {}
command["containerRuntime"] = "python"
command["codeSegment"] = codeSeg
command_str = json.dumps(command)
print "Create action!"

flags = mqttc.publish(testing_topic, command_str)
