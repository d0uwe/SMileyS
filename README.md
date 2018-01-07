# SMileyS

## Problem statement
Many people have unlimited SMS included in their cellular plan nowadays, but it usually remains unused. This is due to the fact that people often want to chat with multiple people, which is impossible using SMS (One can not see if other people received the same SMS). By creating an app which uses headers in an SMS this is possible. SMS also uses less battery than always having mobile data turned on, and can be convienent when you're at a festival for example.

## Design

#### Main features (minimum viable problem)
* Chat with individuals 
* Create a groupchat
* Add or remove people from a groupchat

#### Optional features
* Send images over SMS
* Be able to create or import a backup
* Make a webinterface if the phone is connected to the internet

## External libraries
* Save messages in SQLite
* SMS manager and reading contacts
* Own protocol for the headers in the SMS
* Own compression method for sending images over SMS

## Similar apps
* GroupMe (https://groupme.com/en-US/sms) is an app which allows group SMS chats by sending an SMS to their central phone numbers, which then distributes the message to other people. Sadly this service only works in the US.
* ChompSMS (https://play.google.com/store/apps/details?id=com.p1.chompsms&hl=nl) is an app which allows normal texting and grouptexting, but when sending a message to multiple people it changes into an MMS, which is functional, but no longer supported by all carriers.
* Handcent Next SMS (https://play.google.com/store/apps/details?id=com.handcent.app.nextsms&rdid=com.handcent.app.nextsms) allows bulk SMS (users don't see who received the message) or group SMS using MMS. 

## Hardest part
Making a database which allows saving all the required information about a groupchat, such as who the group-admin is, which phone-numbers are in the group, and altering this in a way that it works properly when someone wants to leave the group.
