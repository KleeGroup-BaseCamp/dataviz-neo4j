from rasa_core.channels.slack import SlackInput
from rasa_core.channels import OutputChannel
from rasa_core.agent import Agent
from rasa_core.interpreter import RasaNLUInterpreter
import yaml
from rasa_core.utils import EndpointConfig

import warnings

nlu_interpreter = RasaNLUInterpreter('./models/nlu/default/current')
action_endpoint = EndpointConfig(url="http://localhost:5055/webhook")
agent = Agent.load('./models/dialogue', interpreter = nlu_interpreter, action_endpoint = action_endpoint)

input_channel = SlackInput('slack token' #the bot user authentication token
                           )

agent.handle_channels([input_channel], 5004, serve_forever=True)