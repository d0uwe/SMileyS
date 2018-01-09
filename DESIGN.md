# SMileys design document
Douwe van der Wal

## Activities

allConversationsActivity
* Contains a listview of all conversations currently in the database. When an element is pressed, this conversation is opened in the SingleConversationActivity
singleConversationActivity
* Contains a listview of all messages in this conversation. Sending a message in the conversation will add it to the database and send an SMS

addConversationActivity
* Allows the user to select a contact using the contacts app to chat with or create a groupchat which opens the addGroupActivity

addGroupActivity
* This activity allows the user to keep picking a contact to add to the group, or create the group. When the group is created, the activiation protocol is started, explained below.

settingsActivity
* Contains a listview with all settings, such as creating a backup, setting the image format for when you send an image or block users.

#### Optional acitities accessible from settings:
BlockListActivity
* contains a listview with blocked users, a longpress on the contact will remove them from the list, and a button is provided to select a contact to add to the list

SetImageFormatActivity
* Shows a fragment to set height and width to set the maximum height and width of images that are sent out to send SMS. 

CreatingBackupActivity
* Exports the database and asks the user where they want to share this file. 

![visual sketch](https://github.com/d0uwe/SMileyS/blob/master/docs/design.JPG?raw=true)
## Database structure
```
SQLite database:
  conversations:
    groups:
      group1:
        id: id
        admin: phonenumber
        name: groupname

        messages:
            message1 - date - sender
            message2 - date - sender
            ....
        members:
            member1
            member2
            ...
 
     individual:
         contact_name: name
         messages:
           message1 - date
           message2 - date

  unconfirmed groups
    id1
    id2
    id3
  my unconfirmed groups
    id - [list of members] - [list of responses]
  
 ```
 ## Protocol
 ### Activitation
 When a user creates a group, the id of the group consists of the creators phonenumber + amount of groups this person created
 In case his phone was reset, this number might have been used already with other users, so confirmation if this code is unused is asked.
 All phones reply whether the id was taken already or not.
 So:
 ```
 id = phoneNumber\amountConversations;
 amountConversations += 1;
 sendConfirmationRequest(allGroupMembers, id);
 ```
 When a confirmation is received the following code is executed
 ```
 For every non confirmed group id {
   If(allResponsesConfirmed(id)) {
      If(allPostive(responses.get(id))){
        sendidConfirmed(id)
      } else {
        id = phoneNumber\amountConversations;
        amountConversations += 1;
        sendConfirmation(allGroupMembers, id, [list of all members])
      }
   }
 }
 ```
 All messages or commands ought to be distributed to all groupmembers. 
 Commands the admin of the group can send to the groupmembers(commands send by a non-admin will be ignored)
 * [groupid, ADD, phonenumber]
 * [groupid, DEL, phonenumber]
 * [groupid, ADMIN, phonenumber]
 
A message in the group will look like this:
* [groupid] Hello i just created a message!

ID negotiating will look like this:
[ID group-request]
[ID accepted] or[ID denied]
[ID group-accepted [list of all members]]



### On SMS received
If an SMS is received without a header, it will be added to the right conversation, or a new individual conversation is created.

If an SMS with header is received, the appropriate reply is given, such as: ID accepted]
 
