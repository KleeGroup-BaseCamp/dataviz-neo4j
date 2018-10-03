from rasa_nlu.training_data import load_data
from rasa_nlu import config
from rasa_nlu.model import Trainer
from rasa_nlu.model import Metadata, Interpreter
import json

def train_nlu(data, configs, model_dir):
	training_data = load_data(data)
	trainer = Trainer(config.load(configs))
	trainer.train(training_data)
	model_directory = trainer.persist(model_dir, fixed_model_name = 'current')
	
def run_nlu():
    interpreter = Interpreter.load('./models/nlu/default/current')
    print("J'aimerais des informations sur une ligne de métro. ")
    print(json.dumps(interpreter.parse(u"J'aimerais des informations sur une ligne de métro. ")['entities'],indent=2))
    print("Je cherche l'arret Luxembourg.")
    print(json.dumps(interpreter.parse(u"Je cherche l'arret Luxembourg.")['entities'],indent=2))
    print("JE cherche Bastille ")
    print(json.dumps(interpreter.parse(u"JE cherche Bastille ")['entities'],indent=2))
    print("Est-ce que Antony n'est pas trop bondé ? ")
    print(json.dumps(interpreter.parse(u"Est-ce que Bourg-la-Reine n'est pas trop bondé ? ")['entities'],indent=2))
    print("Est-ce qu'il y aura du monde ?")
    print(json.dumps(interpreter.parse(u"Est-ce qu'il y aura du monde ?")['entities'],indent=2))
    print("Est-ce qu'il y aura du monde à Antony ?")
    print(json.dumps(interpreter.parse(u"Est-ce qu'il y aura du monde à Antony ?")['entities'],indent=2))
    print("Dans combien de temps est le prochain train?")
    print(json.dumps(interpreter.parse(u"Dans combien de temps est le prochain train?")['entities'],indent=2)) 
    print("Quand arrive le train à Antony?")
    print(json.dumps(interpreter.parse(u"Quand arrive le train à Antony")['entities'],indent=2))  
    print("Où est-ce que je peux changer de train, sur la ligne B ?")
    print(json.dumps(interpreter.parse(u"Où est-ce que je peux changer de train, sur la ligne B ?")['entities'],indent=2))  	
    print("Comment je fais pour aller sur la ligne B ?")
    print(json.dumps(interpreter.parse(u"Comment je fais pour aller sur la ligne B ?")['entities'],indent=2))    
    print("Je ne sais pas comment prendre la 5")
    print(json.dumps(interpreter.parse(u"Je ne sais pas comment prendre la 5")['entities'],indent=2))  
    print("48.81952186378453")
    print(json.dumps(interpreter.parse(u"48.81952186378453")['entities'],indent=2))  
    print("2,445520466796893")
    print(json.dumps(interpreter.parse(u"2,445520466796893")['entities'],indent=2))  

if __name__ == '__main__':
	train_nlu('./data/data.json', 'config_spacy.json', './models/nlu')
	run_nlu()