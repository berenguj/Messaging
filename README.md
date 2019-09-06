# Messaging Program
****Messaging Program**** This is a personal project that I started in July 2019. I wanted to learn more about socket communication 
in Java, so I followed a basic tutorial and then implemented my own additional features from there. I hope to keep building on this
project with more essential features of typical messaging apps and a functioning GUI. 

## How To Run the Program and How It Works
To run the program, first run the server file. Then, run the client file. If you haven't used this program already, you'll have to 
create an account by typing in 'SIGN UP'. Special phrases are needed for the program to work so when you see something like 
'Would you like to sign up or login [SIGN UP | LOGIN]' the [SIGN UP | LOGIN] notation is indicating to you the correct phrase you'll
need to type for either option. Once you've created a username and password, you'll be able to 'ADD' friends to your friends list. Once
you've added friends to your friends list, you're able to 'CHAT' with your friends. Once you select the option to chat with your
friends, your friends list will pop up and tell you who is online. You'll only be able to chat with friends that are online. Also, if you try to chat with someone that's not in 
your list, it'll give you the option to either add them as a friend or chat with someone else. Once you've selected a friend you want to chat with, the server will send your friend a notification telling them they would like to chat with you. They either reply yes or no, and if yes, you two can start chatting, but if no you have the option to send them messages that they can view later. If you are the client recieving a notification from a friend saying that someone would like to chat with you, you can either reply yes or no. If yes, you'll start talking to that friend, and if no, you'll be able to talk to anyone else in your friends list. Also, you're able to logout anytime by typing 'LOGOUT'. At that point, the socket connection will be terminated for that client/user and it
will alert the friend/other client you were chatting with that you have logged out and that they will need to log back in again in
order to chat with someone else. 

## Functionalities
- Clients Online Simultaneously
- Username and Password
- Friends List
- Online Status
- Valid Response Checks
- Notifications
- Unread Messages

## Future Things I Will Implement
Some more features that I plan on implementing is first, encrypting the passwords and also finding a better way to store all of the data, and a functioning GUI.
