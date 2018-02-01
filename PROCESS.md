# 15 januari
Na de meeting ben ik begonnen met het zoeken naar een goed voorbeeld van de room API. Ik heb iets gevonden dat werkt, maar het is erg complex en specifiek voor dat ene voorbeeld. Uiteindelijk eind van de dag toch besloten om maar de SQ-lite database zoals in de restaurant app te maken. Nu worden alle berichten daarin opgeslagen en is er een lijst met unieke nummers waarvan een bericht is ontvangen. Dit maakt listviews mogelijk. 
# 16 januari
Er is nu een mainscreen waarin alle conversaties getoond worden, op dit moment alleen met individuele nummers, en als je daar op klikt zie je de bijbehorende berichten van dat contact, en kan er een nieuw bericht verzonden worden. Wanneer er een sms ontvangen wordt stuurt de SMS-ontvang functie een local broadcast uit, om de listviews te verversen, er zijn immers nieuwe berichten ontvangen. Dit was lastig om te vinden wat ik nodig had. 
Verder beginnen de listviews helemaal naar onderen gescrolled, zodat het nieuwste bericht zichtbaar is.  Een nieuw probleem is nu, dat de constraint layout helemaal naar boven schuift als het toetsenbord omhoog komt, wat alles een beetje raar maakt. Ik ga morgen kijken of dit met een linear layout beter is. 
# 17 januari
Vandaag heb ik toegevoegd dat uitgaande berichten ook worden opgeslagen in de database en dat berichten een boolean waarde hebben of ze ingaand of uitgaand zijn. Dit is een stap richting een mooie layout van een conversatie.
Verder is het scherm gemaakt waarin meerdere contacten geselecteerd kunnen worden voor een groepsconversatie of individuele conversatie. Via een settings knop, waarvan het maken erg tegenviel. Alle nummers worden genormaliseerd voordat er iets de database ingaat, zodat de manier waarop een nummer in je contacten staat niet uit maakt voor de database (+31 ervoor of niet).
# 18 januari
Vandaag heb ik mijn tweede telefoon klaar gemaakt, met api level 16, dus dat was nog wat gedoe. 
 Vandaag heb ik ook  wat kleine stijlaanpassingen gedaan en een groot begin gemaakt aan een custom layouts voor de listview. Dit zorgt ervoor dat berichtjes mooi rechts en links komen zoals bij een normale chatapp. 
# 19 januari
Custom row layouts voor de listviews werken nu, geleend van (https://trinitytuts.com/simple-chat-application-using-listview-in-android/). Dit zal nog vaker gebruikt worden en aangepast worden, aangezien groepchats andere berichten vereisen. Vandaag heb ik ook een presentatie gehouden en het PROCESS.md bestand van google drive naar deze GitHub verplaatst. 

# 21 januari
Ook de listviews met alle conversaties hebben nu een custom row layout, met daarin de naam van het contact en het laatste ontvangen bericht. Alleen het tijdstip moet nog toegevoegd worden in de database. 

# 22 januari
Vandaag heb ik de database aangepast voor het gebruik van groepen. Wanneer er een invite gestuurd moet worden, wordt er een nieuwe identifier aangemaakt voor dit gesprek. 

# 23 januari
Wegens ziekte is er vandaag niets productiefs gebeurd.

# 24 januari
Wanneer er een invite ontvangen wordt, wordt er een identifier gekozen en teruggestuurd. Wanneer zo'n antwoord op een invite wordt ontvangen wordt dit opgeslagen in de database. 

# 25 januari 
ziek :C

# 26 januari
Wegens griep is er nog steeds niet zo heel veel gebeurd, maar berichten kunnen nu worden verzonden binnen een groepchat en gaan naar alle deelnemers. Ook heb ik vandaag een presentatie gehouden. 

# 27 januari
De gesprekken lijst wordt nu gesorteerd op basis van laatst verzonden / ontvangen bericht en in een groepchat kun je nu zien wie een bericht heeft verzonden. 

# 28 januari 
Code opgeruimd en een helpers file aangemaakt voor functies die in verschillende activities worden gebruikt

# 29 januari
Datums worden nu opgeslagen bij berichten en weergegeven naast de berichtjes. Ook is het nu mogelijk om een nieuw lid toe te voegen aan een conversatie en een overzicht te zien van mensen die er nu in zitten. Ook in de contacten lijst wordt de laatste datum nu weergegeven.

# 30 januari
De smsjes om iemand te verwijderen uit de groep werken nu, ze moeten alleen nog geinterpreteerd worden als ze ontvangen worden. 
Verder is de layout voor conversaties nu mooi geworden.

# 31 januari
Mensen kunnen nu echt verwijderd worden uit een group en krijgen daar een bericht van. Verder heb ik veel gecomment en code net gemaakt. Ook is een groot deel van het final report geschreven. Ook is het nu mogelijk om gebruikers te blokkeren.

# 1 februari
Enkele layout veranderingen een een hoop opschoning.