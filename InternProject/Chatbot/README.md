# TransportBot

Ce robot peut répondre à diverses questions concernant les arrêts de transport en commun de la région parisienne : 
 - Quel est le retard moyen à cet arrêt ?
 - Quelle est le nombre de validations à cette arrêt ? (en moyenne, par jour)
 - Quelles sont les lignes reliées à cette arrêt ? (encore en cours)
 - Quand passe le prochain train ? (soit ok et on renvoie les horaires du prochain train pour chaque ligne passant par l'arrêt, soit on demande à l'utilisateur de préciser la ligne, et on ne lui renvoie que l'horaire correspondant, dans les différentes direction)

## Apprendre au robot

Pour lancer l'apprentissage du modèle NLU : 
``` python nlu_model.py ```

Pour lancer l'apprentissage du modèle Rasa Core : 
 - lancer le serveur des actions
 ``` python -m rasa_core_sdk.endpoint --actions actions ```  
 - lancer le modèle 
``` python dialogue_management_model.py ```  
 - parler au robot

Pour lancer l'apprentissage du modèle Rasa Core en ligne :
 - lancer le serveur des actions
 ``` python -m rasa_core_sdk.endpoint --actions actions ```  
 - lancer le modèle en ligne
``` python train_online.py ```  
  - parler au robot et le corriger


### Communiquer avec le robot sur slack

1. Lancer le serveur des actions
``` python -m rasa_core_sdk.endpoint --actions actions ```  

2. Lancer le robot
```python run_app.py```

3. Lancer ngrok sur le port 5004
``` ngrok http 5004 ```

4. Recopier l'url https donnée par ngrok sur slack, et y ajouter "/webhooks/slack/webhook"
Pour cela, aller dans l'onglet "Event Subscription" de slack, dans le TransportBot, et recopier l'url dans la zone de texte "Request URL"

5. Parler au robot dans https://dataviz-neo4j.slack.com/messages

Latest code update: 08/09/2018

Latest compatible Rasa NLU version: 0.13.2

Latest compatible Rasa Core version: 0.11.3




