## Introduction 00
* greet
  - utter_greet
* inform{"info_type": "route"}
  - utter_ask_precisions
* inform{"route_name": "6"}
  - action_ask_about
> ask_route_question

## Introduction 01
* greet
  - utter_greet
* inform{"info_type": "route", "route_name": "B"}
  - action_ask_about
> ask_route_question

## Introduction 02
* inform{"info_type": "route"}
  - utter_ask_precisions
* inform{"route_name": "85"}
  - action_ask_about
> ask_route_question

## Introduction 03
* inform{"info_type": "route", "route_name": "TER"}
  - action_ask_about
> ask_route_question

## Introduction 04
* inform{"route_name": "48"}
  - action_ask_about
> ask_route_question 

## Introduction 05
* greet
  - utter_greet
> say_hello

## Introduction 10
* greet
  - utter_greet
* inform{"info_type": "stop"}
  - utter_ask_precisions
* inform{"stop_name": "Luxembourg"}
  - action_ask_about
> ask_stop_question

## Introduction 11
* greet
  - utter_greet
* inform{"info_type": "stop", "stop_name": "Bastille"}
  - action_ask_about
> ask_stop_question

## Introduction 12
* inform{"info_type": "stop"}
  - utter_ask_precisions
* inform{"stop_name": "Mairie des Lilas"}
  - action_ask_about
> ask_stop_question

## Introduction 13
* inform{"info_type": "stop", "stop_name": "Châtelet les Halles"}
  - action_ask_about
> ask_stop_question

## Introduction 14
* inform{"stop_name": "Jules Joffrin"}
  - action_ask_about
> ask_route_question 

## Infinite hello
> say_hello
* greet 
  - utter_say_hello
>say_hello


## Route - Ask about delay 00
> ask_stop_question
* inform{"aim": "delay"}
  - utter_on_it
  - action_find_delay_route
> ask_any_other_question

## Route - Ask about delay 01
* inform{"route_name": "T3B", "aim": "delay"} <!-- utile d'ajouter l'info_type ? --> 
  - utter_on_it
  - action_find_delay_route
> ask_any_other_question

## Route - Ask about traffic 00
> ask_specific_question
* inform{"aim": "traffic"}
  - utter_on_it
  - action_find_nb_vald_route
> ask_any_other_question

## Route - Ask about traffic 01
* inform{"route_name": "95-48", "aim": "traffic"} <!-- utile d'ajouter l'info_type ? --> 
  - utter_on_it
  - action_find_nb_vald_route
> ask_any_other_question

## Route - Ask about route change 00
> ask_route_question
* inform{"aim": "change", "route_change": "4"}
  - utter_on_it
  - action_find_fork_stops
> ask_any_other_question

## Route - Ask about route change 01
* inform{"route_name": "A", "aim": "change", "route_change": "4"} <!-- utile d'ajouter l'info_type ? --> 
  - utter_on_it
  - action_find_fork_stops
> ask_any_other_question

## Route - Ask about route change 02
> ask_route_question
* inform{"aim": "change"}
  - utter_ask_route_change
* inform {"route_change": "5"}
  - utter_on_it
  - action_find_fork_stops
> ask_any_other_question

## Route - Ask about route change 03
* inform{"route_name": "GO Paris", "aim": "change"}
  - utter_ask_route_change
* inform {"route_change": "27"}
  - utter_on_it
  - action_find_fork_stops
> ask_any_other_question

## Route - Ask about next train 00
> ask_route_question
* inform{"aim": "next_train", "stop_name": "Luxembourg"} <!-- - utter_ask_what_route * inform{"route_name": "B"} --> 
  - utter_on_it
  - action_choose_route
* inform {"route_name": "5"}
  - action_find_next_train
> ask_any_other_question

## Route - Ask about next train 01
> ask_route_question
* inform{"aim": "next_train"} <!-- - utter_ask_what_route * inform{"route_name": "B"} --> 
  - utter_ask_stop_in_route
* inform{"stop_name": "Châtelet les Halles"}
  - utter_on_it
  - action_choose_route
* inform {"route_name": "T3"}
  - action_find_next_train
> ask_any_other_question

## Route - Ask about nearest stop 00
> ask_route_question
* inform {"aim": "nearest_stop"}
  - utter_ask_location
* inform{"gps_coord_known": false}
  - utter_sorry
> ask_any_other_question

## Route - Ask about nearest stop 01
> ask_route_question
* inform {"aim": "nearest_stop"}
  - utter_ask_location
* inform{"gps_coord_known": true}
  - utter_ask_latitude
* inform {"lattitude" : "48.8534100"}
  - utter_ask_longitude
* inform {"longitude": "2.3488000"}
  - utter_on_it
  - action_find_nearest_stop_route
> ask_any_other_question

## Stop - Ask about delay 00
> ask_stop_question
* inform{"aim": "delay"}
  - utter_on_it
  - action_find_delay_stop
  
> ask_any_other_question

## Stop - Ask about delay 01
* inform{"aim": "delay", "stop_name": "Goussainville"}
  - utter_on_it
  - action_find_delay_stop
  
> ask_any_other_question

## Stop - Ask about delay 02
* inform{"aim": "delay"}
  - utter_ask_what_stop
* inform {"stop_name": "Issou Porcheville"}
  - utter_on_it
  - action_find_delay_stop
  
> ask_any_other_question

## Stop - Ask about traffic 00
> ask_stop_question
* inform{"aim": "traffic"}
  - utter_on_it
  - action_find_nb_vald_stop
  
> ask_any_other_question

## Stop - Ask about traffic 01
* inform{"stop_name": "Anatole-France", "aim": "traffic"}
  - utter_on_it
  - action_find_nb_vald_stop
  
> ask_any_other_question

## Stop - Ask about traffic 02
* inform {"aim": "traffic"}
  - utter_ask_what_stop
* inform {"stop_name": "Issy"}
  - utter_on_it
  - action_find_nb_vald_stop
  
> ask_any_other_question

## Stop - Ask about next train 00
> ask_stop_question
* inform{"aim": "next_train"}
  - utter_on_it
  - action_choose_route
* inform {"route_name": "95-48"}
  - action_find_next_train
  
> ask_any_other_question

## Stop - Ask about next train 01
* inform{"aim": "next_train"}
  - utter_ask_what_stop
* inform {"stop_name": "Belvedere"}
  - utter_on_it
  - action_choose_route
* inform {"route_name": "1"}
  - action_find_next_train
  
> ask_any_other_question

## Stop - Ask about possible routes 00
> ask_stop_question
* inform {"aim": "change"}
  - utter_on_it
  - action_find_possible_routes
>ask_any_other_question

## Stop - Ask about possible routes 01
* inform {"aim": "change"}
  - utter_ask_what_stop
* inform {"stop_name": "Arcueil-Cachan"}
  - utter_on_it
  - action_find_possible_routes
>ask_any_other_question

## Stop - Ask about possible routes 02
* inform {"aim": "change", "stop_name": "Besancourt"}
  - utter_on_it
  - action_find_possible_routes
>ask_any_other_question


## Say something useless 00
>ask_any_other_question
* inform
  - utter_dont_understand
  - utter_ask_howcanhelp
>say_something_useless

## Say something useless 01
>say_something_useless
* inform
  - utter_dont_understand
  - utter_ask_howcanhelp
>say_something_useless

## Say something useless 02
>say_something_useless
* inform
  - utter_dont_understand
  - utter_ask_howcanhelp
>ask_any_other_question


## Any other question - Route 00
> ask_any_other_question
* inform {"info_type": "route"}
 - utter_ask_precisions
* inform {"route_name": "T5"}
  - action_ask_about
> ask_route_question

## Any other question - Route 10
> say_something_useless
* inform {"info_type": "route"}
 - utter_ask_precisions
* inform {"route_name": "T5"}
  - action_ask_about
> ask_route_question

## Any other question - Route 01
> ask_any_other_question
* inform {"info_type": "route", "route_name": "T3"}
  - action_ask_about
> ask_route_question

## Any other question - Route 11
> say_something_useless
* inform {"info_type": "route", "route_name": "T3"}
  - action_ask_about
> ask_route_question

## Any other question - Stop 00
> ask_any_other_question
* inform {"info_type": "stop"}
 - utter_ask_precisions
* inform {"stop_name": "Acheres-Ville"}
  - action_ask_about
> ask_stop_question

## Any other question - Stop 10
> say_something_useless
* inform {"info_type": "stop"}
 - utter_ask_precisions
* inform {"stop_name": "Acheres-Ville"}
  - action_ask_about
> ask_stop_question

## Any other question - Stop 01
> ask_any_other_question
* inform {"info_type": "stop", "stop_name": "Avenue-Foch"}
  - action_ask_about
> ask_stop_question

## Any other question - Stop 11
> say_something_useless
* inform {"info_type": "stop", "stop_name": "Avenue-Foch"}
  - action_ask_about
> ask_stop_question

## Any other question - Stop 02
> ask_any_other_question
> ask_stop_question


## Ask what stop 00
>ask_what_stop
* inform
    - utter_dont_understand
    - utter_ask_what_stop
>ask_what_stop

## Ask what stop 01
>ask_what_stop
* inform {"stop_name": "Gentilly"}


## Any other question - Conclusion 00
> ask_any_other_question
* goodbye
 - utter_goodbye

## Any other question - Conclusion 01
> ask_any_other_question
* deny
 - utter_goodbye

## Thank you 00
>thankyou
* thankyou
    - utter_thankyou
* deny
    - utter_goodbye

## Thank you 01
>thankyou
* thankyou
    - utter_thankyou
>ask_any_other_question


## Conclusion 00
* goodbye
  - utter_goodbye
* goodbye
  - action_listen

## Conclusion 01
> ask_any_other_question
* goodbye
  - utter_goodbye
* goodbye
  - action_listen


## Generated Story -2411927814888930574
* inform{"aim": "next_train"}
    - utter_ask_what_stop
* inform{"stop_name": "Bagneux"}
    - utter_on_it
    - action_find_delay_stop
> ask_any_other_question

## Generated Story -7671379509969370176
* inform{"stop_name": "bastille"}
    - slot{"stop_name": "bastille"}
    - action_ask_about
* inform{"aim": "traffic"}
    - slot{"aim": "traffic"}
    - utter_on_it
    - action_find_nb_vald_stop
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_on_it
    - action_find_delay_stop
* deny
    - utter_goodbye

## Generated Story 5212668889955630918
* greet
    - utter_greet
* inform{"aim": "traffic", "stop_name": "fontenay"}
    - slot{"aim": "traffic"}
    - slot{"stop_name": "fontenay"}
    - action_find_nb_vald_stop
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_on_it
    - action_find_delay_stop
* deny
    - utter_goodbye

## Generated Story 4875818444061459192
* inform{"stop_name": "Chatenay-malabry"}
    - slot{"stop_name": "Chatenay-malabry"}
    - action_ask_about
* inform{"aim": "traffic"}
    - slot{"aim": "traffic"}
    - utter_on_it
    - action_find_nb_vald_stop
* inform{"stop_name": "Denfert-Rochereau"}
    - slot{"stop_name": "Denfert-Rochereau"}
    - action_ask_about
* inform{"aim": "traffic"}
    - slot{"aim": "traffic"}
    - utter_on_it
    - action_find_nb_vald_stop
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_on_it
    - action_find_delay_stop
* deny
    - utter_goodbye

## Generated Story 6471835444400316121
* inform{"stop_name": "Denfert-Rochereau"}
    - slot{"stop_name": "Denfert-Rochereau"}
    - action_ask_about
* inform{"aim": "change"}
    - slot{"aim": "change"}
    - utter_on_it
    - action_find_possible_routes
* inform{"aim": "change", "stop_name": "arcueil - cachan"}
    - slot{"aim": "change"}
    - slot{"stop_name": "arcueil - cachan"}
    - utter_on_it
    - action_find_possible_routes
* goodbye
    - utter_goodbye

## Generated Story -5768271186091863375

* inform{"aim": "next_train", "transport_type": "train", "stop_name": "bagneux"}
    - slot{"aim": "next_train"}
    - slot{"stop_name": "bagneux"}
    - slot{"transport_type": "train"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "9"}
    - action_find_next_train
* inform{"aim": "next_train", "transport_type": "train", "stop_name": "bastille"}
    - slot{"aim": "next_train"}
    - slot{"stop_name": "bastille"}
    - slot{"transport_type": "train"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "B"}
    - action_find_next_train
* inform{"info_type": "stop", "stop_name": "luxembourg"}
    - slot{"info_type": "stop"}
    - slot{"stop_name": "luxembourg"}
    - action_ask_about
* inform {"aim": "next_train"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "7"}
    - action_find_next_train
* goodbye
    - utter_goodbye

## Generated Story 2488576957182159640
* greet
    - utter_greet
* inform{"transport_type": "train", "aim": "next_train"}
    - slot{"aim": "next_train"}
    - slot{"transport_type": "train"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "A"}
    - action_find_next_train
* deny
    - utter_goodbye

## Generated Story 2029116183510997502
* greet
    - utter_greet
* inform{"aim": "next_train", "transport_type": "train", "stop_name": "bagneux"}
    - slot{"aim": "next_train"}
    - slot{"stop_name": "bagneux"}
    - slot{"transport_type": "train"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "D"}
    - action_find_next_train
* inform{"stop_name": "bourg-la-reine"}
    - slot{"stop_name": "bourg-la-reine"}
    - action_ask_about
* inform{"aim": "traffic"}
    - slot{"aim": "traffic"}
    - utter_on_it
    - action_find_nb_vald_stop

## Generated Story -1090814822400962741
* greet
    - utter_greet
* inform
    - utter_dont_understand
    - utter_ask_howcanhelp
* inform{"aim": "delay", "transport_type": "train"}
    - slot{"aim": "delay"}
    - slot{"transport_type": "train"}
    - utter_ask_what_stop
> ask_what_stop


## Generated Story -1641278179308342852
* inform{"aim": "next_train", "transport_type": "train"}
    - slot{"aim": "next_train"}
    - slot{"transport_type": "train"}
    - utter_ask_what_stop
* inform{"stop_name": "robinson"}
    - slot{"stop_name": "robinson"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "2"}
    - action_find_next_train
* inform{"stop_name": "Antony"}
    - slot{"stop_name": "Antony"}
    - action_ask_about
* inform{"aim": "next_train", "transport_type": "train"}
    - slot{"aim": "next_train"}
    - slot{"transport_type": "train"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "T5"}
    - action_find_next_train
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_ask_what_stop
* inform{"stop_name": "mitry_claye"}
    - slot{"stop_name": "mitry_claye"}
    - utter_on_it
    - action_find_delay_stop
* deny
    - utter_goodbye

## Generated Story 2261642166700300196
* greet
    - utter_greet
* greet
    - utter_say_hello
* greet
    - utter_say_hello
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_ask_what_stop
* inform{"stop_name": "Charles de Gaulle - Etoile"}
    - utter_on_it
    - action_find_delay_stop


## Generated Story 2961031269917636688
* inform{"info_type": "route", "route_name": "b"}
    - slot{"info_type": "route"}
    - slot{"route_name": "b"}
    - action_ask_about
* inform{"aim": "traffic"}
    - slot{"aim": "traffic"}
    - action_find_nb_vald_route
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_on_it
    - action_find_delay_route

## Generated Story 969984767350287781
* inform{"aim": "traffic", "info_type": "route", "route_name": "4"}
    - slot{"aim": "traffic"}
    - slot{"info_type": "route"}
    - slot{"route_name": "4"}
    - action_find_nb_vald_route
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_on_it
    - action_find_delay_route
* inform{"info_type": "stop"}
    - slot{"info_type": "stop"}
    - utter_ask_precisions
* inform{"stop_name": "Antony"}
    - slot{"stop_name": "Antony"}
    - action_ask_about
* inform{"aim": "next_train", "transport_type": "tramway"}
    - slot{"aim": "next_train"}
    - slot{"transport_type": "tramway"}
    - utter_on_it
    - action_choose_route
* inform {"route_name": "T3"}
    - action_find_next_train

## Generated Story -5941785321340198470
* inform{"aim": "next_train", "transport_type": "train", "stop_name": "Antony"}
    - slot{"aim": "next_train"}
    - slot{"stop_name": "Antony"}
    - slot{"transport_type": "train"}
    - utter_on_it
    - action_choose_route

## Generated Story -4545837198059966614
* inform{"aim": "change", "info_type": "stop", "stop_name": "bastille"}
    - slot{"aim": "change"}
    - slot{"info_type": "stop"}
    - slot{"stop_name": "bastille"}
    - action_find_possible_routes
* inform{"info_type": "stop", "stop_name": "Richard Lenoir"}
    - slot{"info_type": "stop"}
    - slot{"stop_name": "Richard Lenoir"}
    - action_ask_about

## Generated Story 1936544859873267750
* inform{"aim": "change", "stop_name": "Antony"}
    - slot{"aim": "change"}
    - slot{"stop_name": "Antony"}
    - utter_on_it
    - action_find_possible_routes
* inform{"aim": "change", "info_type": "route", "route_name": "b"}
    - slot{"aim": "change"}
    - slot{"info_type": "route"}
    - slot{"route_name": "b"}
    - action_find_fork_stops
>thankyou

## Generated Story -7017604615271226184
* inform{"aim": "next_train", "transport_type": "train"}
    - slot{"aim": "next_train"}
    - slot{"transport_type": "train"}
    - utter_ask_what_stop
* inform{"stop_name": "anvers"}
    - slot{"stop_name": "anvers"}
    - utter_on_it
    - action_choose_route
* inform{"route_name": "B"}
    - action_find_next_train
* thankyou
    - utter_thankyou
* deny
    - utter_goodbye

## Generated Story 8674728679353753696
* greet
    - utter_greet
* inform{"aim": "traffic", "stop_name": "Antony"}
    - slot{"aim": "traffic"}
    - slot{"stop_name": "Antony"}
    - utter_on_it
    - action_find_nb_vald_stop
* inform{"info_type": "route", "route_name": "b", "aim": "traffic"}
    - slot{"aim": "traffic"}
    - slot{"info_type": "route"}
    - slot{"route_name": "b"}
    - action_find_nb_vald_route
* deny
    - utter_goodbye

## Generated Story -2101746168487202278
* inform{"aim": "traffic", "info_type": "route", "route_name": "b"}
    - slot{"aim": "traffic"}
    - slot{"info_type": "route"}
    - slot{"route_name": "b"}
    - utter_on_it
    - action_find_nb_vald_route
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_on_it
    - action_find_delay_route
* inform{"aim": "nearest_stop", "info_type": "route", "route_name": "b"}
    - slot{"aim": "nearest_stop"}
    - slot{"info_type": "route"}
    - slot{"route_name": "b"}
    - utter_ask_location
* dont_know OR deny
    - utter_sorry
>ask_any_other_question


## Generated Story -3121104148374763008
* inform{"aim": "nearest_stop", "route_name": "4"}
    - slot{"aim": "nearest_stop"}
    - slot{"route_name": "4"}
    - utter_on_it
    - utter_ask_location


## Generated Story -7634813676870333739
* inform{"aim": "nearest_stop", "route_name": "4"}
    - slot{"aim": "nearest_stop"}
    - slot{"route_name": "4"}
    - utter_ask_location
* inform{"gps_coord_known": true}
    - utter_ask_latitude
* inform{"latitude": "48.80469043670592"}
    - slot{"latitude": "48.80469043670592"}
    - utter_ask_longitude
* inform{"longitude": "2.070612019531268"}
    - slot{"longitude": "2.070612019531268"}
    - action_find_nearest_stop_route

## Generated Story 8010652999583811976
* inform{"aim": "nearest_stop", "info_type": "route", "route_name": "c"}
    - slot{"aim": "nearest_stop"}
    - slot{"info_type": "route"}
    - slot{"route_name": "c"}
    - utter_ask_location
* inform{"gps_coord_known": true}
    - utter_ask_latitude
* inform{"latitude": "48.72557587453395"}
    - slot{"latitude": "48.72557587453395"}
    - utter_ask_longitude
* inform{"longitude": "1.7863407792968928"}
    - slot{"longitude": "1.7863407792968928"}
    - action_find_nearest_stop_route

## Generated Story 796755460904577588
* inform{"stop_name": "Antony"}
    - slot{"stop_name": "Antony"}
    - action_ask_about
* inform{"aim": "traffic"}
    - slot{"aim": "traffic"}
    - utter_on_it
    - action_find_nb_vald_stop
* inform{"aim": "delay", "info_type": "stop"}
    - slot{"aim": "delay"}
    - slot{"info_type": "stop"}
    - action_find_delay_stop
* inform{"aim": "next_train", "transport_type": "train"}
    - slot{"aim": "next_train"}
    - slot{"transport_type": "train"}
    - utter_on_it
    - action_choose_route
* inform{"route_name": "B"}
    - action_find_next_train

## Generated Story 1043103837104169712
* inform{"stop_name": "bourg-la-reine"}
    - slot{"stop_name": "bourg-la-reine"}
    - action_ask_about
* inform{"aim": "traffic"}
    - slot{"aim": "traffic"}
    - utter_on_it
    - action_find_nb_vald_stop
* inform{"aim": "delay"}
    - slot{"aim": "delay"}
    - utter_on_it
    - action_find_delay_stop
* inform{"aim": "next_train", "transport_type": "train"}
    - slot{"aim": "next_train"}
    - slot{"transport_type": "train"}
    - action_choose_route
* inform{"route_name": "4"}
    - slot{"route_name": "4"}
    - action_find_next_train
* inform{"aim": "change", "stop_name": "bourg-la-reine"}
    - slot{"aim": "change"}
    - slot{"stop_name": "bourg-la-reine"}
    - action_find_possible_routes

## Generated Story 8775941396835861230
* inform{"aim": "nearest_stop", "info_type": "route"}
    - slot{"aim": "nearest_stop"}
    - slot{"info_type": "route"}
    - utter_ask_location
* inform{"gps_coord_known": false}
    - utter_sorry
* inform
    - utter_ask_howcanhelp
* inform{"info_type": "stop", "stop_name": "bourg-la-reine", "aim": "traffic"}
    - slot{"aim": "traffic"}
    - slot{"info_type": "stop"}
    - slot{"stop_name": "bourg-la-reine"}
    - utter_on_it
    - action_find_nb_vald_stop
* inform{"aim": "change"}
    - slot{"aim": "change"}
    - action_find_possible_routes
* deny
    - utter_goodbye

## Generated Story 513310103679105398
* greet
    - utter_greet
* deny
    - utter_goodbye
* goodbye
* greet
    - utter_say_hello

