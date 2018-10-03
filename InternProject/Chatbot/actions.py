from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from rasa_core_sdk import Action
from rasa_core_sdk.events import SlotSet

import requests


class ActionAskAbout(Action):
	""" Demande ce que l'utilisateur veut savoir sur l'arrêt/la ligne qu'il a entré """

	def name(self):
		return 'action_ask_about'
		
	def run(self, dispatcher, tracker, domain):
		if tracker.get_slot("info_type") == None:
			if tracker.get_slot("stop_name") != None :
				dispatcher.utter_message("Que voulez vous savoir sur {} ?".format(tracker.get_slot("stop_name"))) 
			elif tracker.get_slot("route_name") != None :
				dispatcher.utter_message("Que voulez vous savoir sur la {} ?".format(tracker.get_slot("route_name"))) 
		else:
			if tracker.get_slot("info_type") == "stop" :
				dispatcher.utter_message("Que voulez_vous savoir sur cet arrêt ?")
			elif tracker.get_slot("info_type") == "route" :
				dispatcher.utter_message("Que voulez-vous savoir sur cette ligne ? ")
			elif tracker.get_slot("info_type") == "town" :
				dispatcher.utter_message("Que voulez-vous savoir sur cette ville ? ")
			else:
				dispatcher.utter_message("Désolé, je n'ai pas compris...")
		return []

class ActionResetSlot(Action):
	""" Réinitialise tous les slots à leur valeur de départ """

	def name(self):
		return 'action_reset_slot'
		
	def run(self, dispatcher, tracker, domain):
		rasa_core.events.AllSlotsReset(timestamp=None)
		return []

class ActionFindDelayRoute(Action):
	""" Trouve le retard moyen sur une ligne de transports en commun (stockée dans route_name) """ 

	def name(self):
		return 'action_find_delay_route'
		
	def run(self, dispatcher, tracker, domain):
		#route_delay = requests.get()
		route_delay = [{"delay": 155000}] #en ms
		minutes = route_delay[0]["delay"] // 60000
		secunds = (route_delay[0]["delay"] / 1000) - (minutes * 60)
		dispatcher.utter_message("Le retard moyen sur la ligne {} est de {} minutes et {} secondes".format(tracker.get_slot("route_name"), minutes, secunds)) 
		dispatcher.utter_message("Que puis-je faire d'autre pour vous aider ? ") 
		return []

                        
class ActionFindDelayStop(Action):
	""" Trouve le retard moyen sur un arrêt de transports en commun (stocké dans stop_name) """ 

	def name(self):
		return 'action_find_delay_stop'
	
	def run(self, dispatcher, tracker, domain):

		if tracker.get_slot("stop_name") != None:
			stop_delay = [{"delay": 125000}] #en ms
			minutes = stop_delay[0]["delay"] // 60000
			secunds = (stop_delay[0]["delay"] // 1000) - (minutes * 60)
			dispatcher.utter_message("Le retard moyen à l'arrêt {} est de {} minutes et {} secondes".format(tracker.get_slot("stop_name"), minutes, secunds))
			dispatcher.utter_message("Puis-je faire quelque chose d'autre ?")

		else:
			dispatcher.utter_message("Vous voulez connaître le retard sur quelle station ?")

		return []

class ActionFindNbValdRoute(Action):
	""" Trouve le nombre de validations moyen sur une ligne de transports en commun (stockée dans route_name) """ 

	def name(self):
		return 'action_find_nb_vald_route'
		
	def run(self, dispatcher, tracker, domain):
		nb_vald = [{"nb_vald": 585250}]
		dispatcher.utter_message("Il y a en moyenne {} validations par jour sur la ligne {}".format(nb_vald[0]["nb_vald"], tracker.get_slot("route_name")))
		return []

class ActionFindNbValdStop(Action):
	""" Trouve le nombre de validations moyen sur un arrêt de transports en commun (stocké dans stop_name) """ 

	def name(self):
		return 'action_find_nb_vald_stop'
		
	def run(self, dispatcher, tracker, domain):
		nb_vald = [{"nb_vald": 585250}]
		if tracker.get_slot("stop_name") != None:
			dispatcher.utter_message("Il y a en moyenne {} validations par jour à l'arrêt {}".format(nb_vald[0]["nb_vald"], tracker.get_slot("stop_name")))
			dispatcher.utter_message("Que puis-je faire d'autre ?")
		
		else:
			dispatcher.utter_message("Sur quel arrêt voulez-vous connaître le nombre de validations ?")
		return []

class ActionFindStopsInTown(Action):
	""" Trouve tous les arrêts situés dans la ville entrée par l'utilisateur """

	def name(self):
		return 'action_find_stops_in_town'
		
	def run(self, dispatcher, tracker, domain):

		stops = [
				 {"stop_name": "Bastille", "latitude": 48.8530842, "longitude": 2.369251599999984},
				 {"stop_name" : "Richard Lenoir", "latitude": 48.8598089, "longitude": 2.3719327999999678}
				]
		respnse = "Les arrêts de cette ville sont "
		for s in stops:
			response += s["stop_name"] + "(lat: " + s["latitude"] + ", lon: " + s["longitude"] + "), "

		dispatcher.utter_message(response)
#		return [SlotSet('stops', stops)]
		return []

class ActionFindStopsRouteInTown(Action):
	""" Trouve tous les arrêts d'une ligne donnée situés dans la ville entrée par l'utilisateur """

	def name(self):
		return 'action_find_stops_route_in_town'
		
	def run(self, dispatcher, tracker, domain):
		response = """Les arrêts de la ligne X de cette ville sont bla, bla et bla, situés à na, na et na."""		
		dispatcher.utter_message(response)
		return []

class ActionFindNearestStop(Action):
	""" Trouve l'arrêt le plus proche de l'utilisateur """

	def name(self):
		return 'action_find_nearest_stop'
		
	def run(self, dispatcher, tracker, domain):

		nearest_stop = [{"stop_name": "Bastille", "latitude": 48.8530842, "longitude": 2.369251599999984}]
		dispatcher.utter_message("""L'arrêt le plus proche de vous est {} (lat : {}, lon : {}).""".format(nearest_stop[0]["stop_name"], nearest_stop[0]["lattitude"], nearest_stop[0]["longitude"]))
		return []

class ActionFindNearestStopRoute(Action):
	""" Trouve l'arrêt d'une ligne donnée le plus proche de l'utilisateur """

	def name(self):
		return 'action_find_nearest_stop_route'
		
	def run(self, dispatcher, tracker, domain):

		if tracker.get_slot("latitude") != None and tracker.get_slot("longitude") != None and tracker.get_slot("route_name") != None:
			nearest_stop = [{"stop_name": "Antony", "latitude": 48.80469043670592, "longitude": 2.070612019531268}]
			response = """L'arrêt de la ligne {} le plus proche de vous est {}, de latitude {} et de longitude {}.""".format(tracker.get_slot("route_name"), nearest_stop[0]["stop_name"], nearest_stop[0]["latitude"], nearest_stop[0]["longitude"])	
			dispatcher.utter_message(response)
			dispatcher.utter_message("Puis-je faire autre chose pour vous aider ? ")
		elif tracker.get_slot("route_name"):
			dispatcher.utter_message("C'est à propos de quelle ligne, déjà ? ")
		else:
			dispatcher.utter_message("Je suis désolé, je n'ai pas bien entré vos coordonnées GPS.")
#		return [SlotSet('action','action')]
		return []

class ActionFindForkStops(Action):
	""" Pour une ligne donnée, trouve tous les arrêts qui permettent de changer de ligne """

	def name(self):
		return 'action_find_fork_stops'
		
	def run(self, dispatcher, tracker, domain):

		if tracker.get_slot("route_name") != None:
			stops = [{"stop_name": "Antony"}, {"stop_name": "Châtelet-les-Halles"}, {"stop_name": "Denfert-Rochereau"}]
			response = """Vous pouvez changer aux arrêts """
			for stop in stops:
				response += stop["stop_name"] + ", " 
			dispatcher.utter_message(response)
		else:
			dispatcher.utter_message("Sur quelle ligne voulez-vous connaître les changements possibles ?")
		return []

class ActionFindNextTrain(Action):
	""" Pour un arrêt et une ligne donnés, donne l'horaire du prochain train (si on faisait la vraie requête, on renverrait les prochains trains pour chaque direction possible) """

	def name(self):
		return 'action_find_next_train'
		
	def run(self, dispatcher, tracker, domain):

		next_train_arrival = [{"hour": 15, "minutes": 23}]
		if tracker.get_slot("stop_name") != None and tracker.get_slot("route_name") != None :
			response = """Le prochain train de la ligne {} arrive à {} à {}h{}.""".format(tracker.get_slot("route_name"), tracker.get_slot("stop_name"), next_train_arrival[0]["hour"], next_train_arrival[0]["minutes"])
			dispatcher.utter_message(response)
			dispatcher.utter_message("Puis-je faire quelque chose d'autre pour vous ?")

		elif tracker.get_slot("stop_name") == None :
			dispatcher.utter_message("Sur quel arrêt voulez-vous l'horaire du prochain passage ? (évitez de répondre avec seulement le nom de l'arrêt, car souvent je ne comprends pas dans ce cas")
		return []

class ActionFindPossibleRoutes(Action):
	""" Trouve toutes les lignes qui passent à un arrêt donné """

	def name(self):
		return 'action_find_possible_routes'
		
	def run(self, dispatcher, tracker, domain):
		possible_routes = [{"route_name" : "B"}, {"route_name": "4"}, {"route_name": "6"}]

		if tracker.get_slot("stop_name") != None:
			response = """Les lignes accessibles depuis """	+ tracker.get_slot("stop_name") + " sont "
			for route in possible_routes:
				response += route["route_name"] + ", "
			dispatcher.utter_message(response)
			dispatcher.utter_message("Que puis-je faire d'autre pour vous aider ?")

		else:
			dispatcher.utter_message("C'est à propos de quel arrêt ?")

		return []

class ActionChooseRoute(Action):
	""" Demande à l'utilisateur de choisir (via des boutons) sur quelle ligne passant par un arrêt donné, il veut des informations """

	def name(self):
		return 'action_choose_route'
		
	def run(self, dispatcher, tracker, domain):
		if tracker.get_slot("stop_name") != None:
			possible_routes = [{"route_name" : "B"}, {"route_name": "4"}, {"route_name": "6"}]

			message = "Plusieurs lignes passent par l'arrêt " + tracker.get_slot("stop_name") + ", quelle est celle qui vous intéresse ? "

			buttons = []
			for route in possible_routes:
				title = route["route_name"]
				payload = '/inform{\"route_name\": "'+ route["route_name"] + '"}'
				buttons.append({ "title": title, "payload": payload })
			dispatcher.utter_button_message(message, buttons)
			print(buttons);

			
		else:
			dispatcher.utter_message("C'est à propos de quel arrêt ?")

		return []