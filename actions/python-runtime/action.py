"""
The action head guard for invoking injected code base.
"""

from flask import Flask, request, jsonify
from importlib import import_module

class ActionRunner():
    def __init__(self, handler_name, code_name, code_seg):
        self.handler_name = handler_name + '.py'
        self.code_name = code_name
        self.code_seg = code_seg

        # Write code seg into file, need to investigate a better way.
        file = open(self.handler_name, 'w')
        file.write(code_seg)
        file.close()

        # Reflection
        self.activate_module = import_module(handler_name)
        self.activate_func = self.activate_module.main

    def run_program(self, params):
        return self.activate_func(params)

app = Flask(__name__)

@app.route('/runc', methods=['POST'])
def runc():
    try:
        obj = request.get_json(force=True, silent=True)
        actionRunner = ActionRunner(obj['handler-name'], 
            obj['code-name'], obj['code-seg'])
        return jsonify(actionRunner.run_program(obj['continuation']))
    except Exception as e:
        # ignore and not recover
        response = jsonify({'error': 'wrong format of input json'})
        response.status_code = 502
        return response

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8080)