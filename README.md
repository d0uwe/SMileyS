# SMileyS
An app which enables groupchatting over SMS by placing headers in the SMS messages. This allows group-chatting when internet is not available or when battery life is important (internet can be turned off.)

by Douwe van der Wal


## Problem statement
Many people have unlimited SMS included in their cellular plan nowadays, but it usually remains unused. This is due to the fact that people often want to chat with multiple people, which is impossible using SMS (One can not see if other people received the same SMS). By creating an app which uses headers in an SMS this is possible. SMS also uses less battery than always having mobile data turned on, and can be convienent when you're at a festival for example.

## Design
The design of the app will be like any generic chat app, such as for example Whatsapp. 
There is a main screen in which all conversations are shown, ranked based on te date of the last received message, and clicking on them will show the entire conversation and give the possibility of sending a message. 

Besides that there will be a hamburger button which provides acces to the settings or creating a new (group)chat.

When a new groupchat is made, an identifier for this chat is distributed amongst all members of the new group, which consists of the creators phone number + the amount of groupchats this person has made so far. This will be followed by all members of the group, and any changes, such as adding a person, will be distributed aswell. 

All the SMS messages in a groupchat will contain a header with the identifier, so the app can place the message in the right conversation.

Sending images over SMS will be done by first simplyfining the colorspace of the image and resizing it to a smaller size. Then the image will be converted to a string and chopped up in pieces that fit in an SMS. 

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
