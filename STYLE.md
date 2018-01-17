Hieronder worden 10 regels gegeven die samen een korte styleguide vormen. 

* Voor en na rekentekens hoort een spatie, net als na comment tekens.
* De accolades bij control statements staan als volgend:
```
function test() {
  do things;
}
```

* Classnames beginnen met een hoofdletter, en voor variabelen wordt camelCase gebruikt.
* Variabelen moeten een nuttige naam hebben, behalve de variabelen die gebruikt worden in een loop, zoals i en j.
* Hou functies kort, als een functie groter wordt dan 40 regels, is het misschien handig deze op te delen. 
* Regellengte is maximaal 100 karakters
* Een try catch moet maar 1 error afvangen, in plaats van 1 generieke error voor veel ingewikkelde acties.
* In functies worden comments die beginnen met // gebruikt om korte stukjes uit te leggen en boven iedere functie staat een comment als deze:
```
    /**
     * Get messages send to and from this number and set them in a listview.
     * @param phoneNumber phoneNumber to show conversation with
     */
```
* Gebruik geen magic numbers, zoals i += 40. Het getal moet dan in een variabele naam gestopt worden die uitlegd wat het doet en eventueel een comment erbij.
* De scope van variabelen moet zo klein mogelijk zijn, om verwarring te voorkomen. 
