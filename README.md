Smart-Helmet Android Application
==============================

Members
----------

* Vishnu G T

* Aditya A Prasad

* Nagasai Boppudi

* Kartheshwar


Overview
--------

This is the application which will work with the smart helmet being made to showcase in our college's technical
festival [Sangam at Pragyan][1]. 



Features
--------

It has the following features :

* The application connects to the smart helmet's speaker via BlueTooth automatically 

* It gives turn-by-turn directions to a specified location from a specified location.

* It alerts the emergency numbers when/if the helmet reports an accident has occurred. 

* It reads any received SMS out loud through the BlueTooth speaker, along with the sender's
  contact name.

* It alerts the user with a incoming caller's contact name.

* It keeps track of fuel consumption and then alerts the user in case of impending shortage.

* The python script is used to fetch revolutions every 5 seconds and checks for over-speeding, it notifies
  the user through a notification(Audio though speaker and/or SMS to parents :P)

* If accident occurs, SMS is send to specified number with location (lat and log) (And extra details such as blood group, 	medical history etc. We were thinking of sending it to nearest hospital)

* The revolutions are fetched by the application and a graph is plotted.

* It calculates total distance travelled from the revolutions and alerts user in case bike needs servicing.

* Using fuel consumption and distance travelled we calculate the Actual mileage of the bike ( We can plot this in a graph 	and use this information to keep track of our driving and minimize fuel costs )

* It checks if the fuel we have is enough for any journey we want to go for.


[1]: https://www.facebook.com/sangam.pragyan