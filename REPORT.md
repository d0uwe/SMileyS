# Summary
This application is an advanced SMS app. It does not only allow conversations with individual people, but also decentralized groupchats over sms by using small headers in the messages.

todo: screenshot

# Technical design
### MainActivity
The activity starts showing the MainActivity. This contains a list view with all conversations currently in the database. This shows the name of the group or the contacts name. If the contact is not in your contacts, it will show the phone number of this person. It will also show the latest message in this conversation and the date. The list is also sorted based on when the last message was received, to have recent conversations on top. For this listview the ContactArrayAdapter is used, using ContactRow objects and filling contact_row layout files. When clicking on a row a user will be send to the ConversationAcitivity. There also is a settings menu which allows the user to start a new individual conversation or a new group conversation. Starting an individual conversation will immediately ask the user to select a contact, and then go to the ConversationActivity. If the user wants to make a new group, he is redirected to the SelectContactsActivity to select multiple users and specify a groupname. 

### ConversationActivity
The ConversationAcitivity shows a list of all the messages in a group / individual conversation. When the conversation is a group conversation the rows in the listview contain: the sender, the message and the date. When it is an individual conversation the sender is not shown. The list view always starts all the way scrolled down, so a user does not have to scroll down every time a user open the conversation. There also is a settings button when the conversation is a group conversation and this send the user to the GroupSettingsActivity. For the listview the ChatArrayAdapter has been used, using ChatMessages as objects which fills either a left.xml, left_group.xml or the right variant, depending on whether the message was incoming or not. A user can also type a new message here and press the send button to send it to all members of the group.

### SelectContactsActivity
The SelectContactsActivity allows a user to select as many contacts as he wants to add to the group, set a name for the new group and there is a button to indicate that the user is done. When this button is clicked, invitations are sent out to the selected numbers and the group is registered in the ChatDatabase. After this is all done, the activity is ended, returning the user to the mainActivity where the newly made group is now displayed in the list of conversations. 

### ChatDatabase
The ChatDatabase contains multiple tables, messages, groupNames, groups, blockedUsers and conversations. 

* The messages table contains all messages, with information such as: to which individual conversation or group conversation does this message belong, what was the date, who was the sender and was the message incoming or not. 
* The groupNames table contains groupID's and which name the group has.
* The groups table contains rows with: the users ID for a group conversation, the phone number of a member of this group and the id this phone number uses for the conversation.
* The conversations table contains the id of groups / phonenumber of individuals of which there is a conversation with, a boolean whether this conversation is a group and the date when there was last activity in this group. This is used for the sorting in the MainActivity.
* The blockedUsers table contains rows which only contains a phone number. When a number is in this table, messages will be blocked. 

The database has functions to insert users in groups, add messages, register a new group, block a user, check if a user is blocked, etc.

### GroupSettingsActivity
The GroupSettingsActivity shows an overview of the members of the group and has a button to add a new person to the group. By long pressing an member of the group, the person can be removed from the group. This will be broadcasted to all members of the group, on which they will no longer send messages to this number when they send a message in the group. The phone of the removed person will keep the conversation and be shown a message that he has been removed from the group. Sending a message in the group will do nothing, except for show it to the user. 

### SmsReceiver
The SmsReceiver file contains a function which listens to incoming SMS messages and calls the right functions to process them. It either executes a part of the protocol explained below, or puts the message in the database together with the date the message was received.

### BlockActivity
The BlockActivity allows a user to select or deselect user who the user would like to block. Blocking means that any personal messages send by a blocked person are ignored, just like groupinvites. Any other grouprelated texts are processed as normal, to prevent getting conversations where not all groupmembers are sending to the same people anymore. A user can add a phonenumber to the blocklist using the add button and remove one by longpressing the name in the list of blocked users.

## The protocol 
Individual conversations will work the same as normal sms messages. 

### Creating a group
When a user creates a new group, he first requests an identifier for this group from his database. This will be a number which increments every time the user creates a group or is invited to a group. This is myID. Then messages are send out to all selected contains which will look like this:
```
myID]INV]nameOfConversation
```
When the app of the receiver gets this message, they register this group and get a groupID for it too, theirID, and will reply:

```
myID]INVOK]theirID
```

The invitor will add the user to the database and loop through all users currently in the group doing the following:
```
newUserID]ADD]numberOfMemberAlreadyInGroup]existingUserID
existingUserID]ADD]numberOfNewUser]newUserID
```
This will update everyone who is currently in the group. 

### Sending a message
When a message is send in a group, the sender will loop through all members of the group and send them the following:
```
theirID]message
```

The app will read the ID, and put the message without the id in the database. 

### Removing a user
When a user removes someone his phone will loop through all members of the conversation and send them:
```
userID]REMOVE]phoneNumberOfUserToBeRemoved
```

To the user who will be removed the following is send: 
```
userID]REMOVE]0. 
```
If the phone sees a 0, it knows he is the one that is removed. This is done this way since it is hard to check which phone number a device has.

# Challenges
The first challenges was thinking of a way to handle the groupID's. I first wanted to do something where all members used the same idea, but then realised that this would require quite some negotiating. The way i currently implemented is, this is not needed. 

The second challenge was getting everything in the database, and also getting it out correctly. Especially the dates where quite a hastle, but I decided to throw away the millisecond precision of the epoch time, which makes it fit into an int again. This made sorting relatively easy, and by multiplying them with 1000, they can be used as normal epoch dates again. 

Another challenge were the layouts which had to scale properly when the keyboard came up. This has eventually been solved by using a linear layout and give elements a weight. 

# Defending design choices
Currently a group has no admin, who has to remove people and add people. Everyone in the group has the same rights. This be abused, but also doesn't force the admin to remove and add everyone, which costs a lot of text messages if the group is large. This way the costs can be spread out. 

The original idea was negotiating a mutual groupID between members. This has changed to everyone having his own idea, and this way the groupchat can already start without receiving a reply from everyone. This increases the speed of creating a groupchat and reduces the amount of text messages used.