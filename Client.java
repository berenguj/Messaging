import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Vector;
import java.util.*;

public class Client {

    //variables
    private static boolean existingUser = false;
    private  static boolean correctPassword = false;
    private  static boolean validResponse = false;
    private  static boolean initialLogin = false;
    private static boolean firstOnlineUser = false;
    private  static Object validResponseObject;
    private  static Object recipientObject;
    private  static Object response1Object;
    private  static String response1; //String addChat;
    private  static String response2; //String secondAddChat;
    private static String signupORlogin;
    private  static String tryORcreate = "";
    private  static String name;
    private  static String recipient;
    private  static String[] chatReturn;
    private  static Vector<Object> responseHandlerReturn = new Vector<>();
    private  static Vector<String> alreadychatting = new Vector<String>();
    private  static Vector<String> onlineusers = new Vector<>();

    public static void main(String[] args) throws IOException{

        try{
            Scanner scanner = new Scanner(System.in);

            //get localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            //establish connection with server port 5056
            Socket socket = new Socket(ip, 5056);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());


            //login process
            Client client = new Client();

            //ask for username from client and add them to the user list
            Scanner scan = new Scanner(System.in);
            System.out.println("Would you like to sign up for an account or login to an existing account?:  [SIGN UP | LOGIN]");
            signupORlogin = scan.next();

            while(!validResponse){
                if(signupORlogin.equals("SIGN UP")){
                    System.out.println("Please type in the username you would like: ");
                    name = scan.next();
                    dataOutputStream.writeUTF(name); //gives the name to the server
                    validResponse = true;
                }
                else if(signupORlogin.equals("LOGIN")){
                    System.out.println("Please type in your username: ");
                    name = scan.next();
                    validResponse = true;
                }
                else{
                    System.out.println("Please enter a valid response [SIGN UP | LOGIN]");
                    signupORlogin = scan.next();
                }
            }
            //reset for next while loop
            validResponse = false;

            //check if they are an existing user
            BufferedReader reader1 = new BufferedReader(new FileReader("users.txt"));
            String line = null;
            line = reader1.readLine();
            while ((line = reader1.readLine()) != null) {
                if (name.equals(line)) {
                    existingUser = true;
                }
            }
            line = null;
            //System.out.println("existing user:" +existingUser);

            //login an existing user
            while(!initialLogin) {

                if ((existingUser && signupORlogin.equals("LOGIN"))) {

                    //check if someone is trying to login twice
                    BufferedReader onlinereader1 = new BufferedReader(new FileReader("onlineusers.txt")); //password will be the very first line of their text file followed by their friends list
                    line = onlinereader1.readLine();
                    while(line != null){
                        if(name.equals(line)){
                            System.out.println("Sorry you are already logged on! Please use the other window instead.");
                            System.exit(10);
                        }
                        line = onlinereader1.readLine();
                    }

                    //get password
                    System.out.println("Welcome back " + name + "!" + " Please type in your password: ");
                    String password = scan.next();

                    //verify password
                    BufferedReader reader2 = new BufferedReader(new FileReader(name + ".txt")); //password will be the very first line of their text file followed by their friends list
                    for (int i = 0; i < 2; i++) {
                        line = reader2.readLine();
                        if (password.equals(line)) {
                            correctPassword = true;
                        } else {
                            correctPassword = false;
                        }
                    }

                    while (!correctPassword) {
                        System.out.println("Incorrect password. Please try again: ");
                        password = scan.next();
                        BufferedReader reader3 = new BufferedReader(new FileReader(name + ".txt"));
                        for (int i = 0; i < 2; i++) {
                            line = reader3.readLine();
                            if (password.equals(line)) {
                                correctPassword = true;
                            } else {
                                correctPassword = false;
                            }
                        }
                    }

                    //successful login
                    initialLogin = true;
                    System.out.println("You are now logged in! You can logout anytime during chatting by typing 'LOGOUT'. Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");

                    //give name to server
                    dataOutputStream.writeUTF(name); //gives the name to the server
                    //add them to onlineusers list
                    BufferedWriter writer1 = new BufferedWriter(new FileWriter("onlineUsers.txt", true));
                    writer1.write(name);
                    writer1.newLine();
                    writer1.flush();
                    writer1.close();

                    //first online user check
                    BufferedReader onlinereader2 = new BufferedReader(new FileReader("onlineusers.txt")); //password will be the very first line of their text file followed by their friends list
                    int onlinecount = 0;
                    String onlineline = "";
                    boolean firstlinenull = false;
                    while(onlineline != null){
                        onlineline = onlinereader2.readLine();
                        onlinecount++;
                        if("".equals(onlineline) && onlinecount == 1){
                            firstlinenull = true;
                        }
                        if(firstlinenull){
                            if(name.equals(onlineline) && onlinecount == 2){
                                firstOnlineUser = true;
                            }
                        }
                        else{
                            if(name.equals(onlineline) && onlinecount == 1){
                                firstOnlineUser = true;
                            }
                        }
                    }

                } else if (!existingUser && signupORlogin.equals("LOGIN")) {
                    System.out.println("Sorry that username does not exist. Please type in the correct username.");
                    name = scan.next();
                    System.out.println("name: " +name);

                    //check if they are an existing user
                    BufferedReader reader2 = new BufferedReader(new FileReader("users.txt"));
                    String line2 = null;
                    line2 = reader2.readLine();
                    while ((line2 = reader2.readLine()) != null) {
                        if (name.equals(line2)) {
                            existingUser = true;
                        }
                    }
                    line2 = null;

                } else if (!existingUser && signupORlogin.equals("SIGN UP")) {
                    BufferedWriter writer1 = new BufferedWriter(new FileWriter("users.txt", true));
                    writer1.write(name);
                    writer1.newLine();
                    writer1.flush();
                    writer1.close();
                    System.out.println("Your username is set! Please type in a password: ");
                    String password = scan.next();

                    PrintWriter writer2 = new PrintWriter(new FileWriter(name + ".txt", true));
                    writer2.write("\n");
                    writer2.write(password);
                    writer2.flush();
                    writer2.close();
                    initialLogin = true;
                    System.out.println("You are now signed up! You can logout anytime while you're chatting by typing 'LOGOUT'. Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");
                }
            }

            //based on response from user, ask if they would like to add a friend or chat with someone
            //consider all possible cases like adding then chatting, or invalid responses, etc
            response1 = scan.next();
            //responseHandlerReturn = server.ResponseHandler(dataOutputStream, dataInputStream, name, server, response1);
            while (!validResponse) {

                responseHandlerReturn = client.ResponseHandler1(dataOutputStream, dataInputStream, name, client, response1, alreadychatting, scan, firstOnlineUser, socket);
                validResponseObject = responseHandlerReturn.get(2);
                validResponse = (boolean) validResponseObject;
                recipientObject = responseHandlerReturn.get(0);
                recipient = (String) recipientObject;
                response1Object = responseHandlerReturn.get(1);
                response1 = (String) response1Object;

            }
            validResponse = false;


            //create sendMessage thread
            Thread sendMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    String msg = "";
                    while(!msg.equals("LOGOUT")){
                        //read message
                        msg = scanner.nextLine();
                        try{
                            if(msg.equals("LOGOUT")){
                                dataOutputStream.writeUTF("Your friend has left the chat. Please login again and choose another friend to chat with!");
                            }
                            else{
                                //write message
                                dataOutputStream.writeUTF(msg);
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    try{
                        dataOutputStream.writeUTF("Your friend has left the chat. Please login again and choose another friend to chat with!");
                        System.out.println("you are now logged out!");
                        dataInputStream.close();
                        dataOutputStream.close();
                        socket.close();
                    }
                    catch(Exception e){
                        System.out.println("you are now logged out! 2");
                    }

                    //close the connection
                    /*try{
                        dataInputStream.close();
                        dataOutputStream.close();
                        socket.close();
                    }
                    catch(IOException i){
                        System.out.println(i);
                    }*/
                }
            });

            //create readMessage thread
            Thread readMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try{
                            //read message
                            String msg = dataInputStream.readUTF();
                            System.out.println(msg);
                        }
                        catch (Exception e){
                            System.out.println(e);
                            System.exit(0);
                        }
                    }
                }
            });

            //start the threads
            sendMessage.start();
            readMessage.start();

        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    public String ADD(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Scanner scan) throws IOException {
        boolean validUser = false;
        String response1 = null;
        System.out.println("Please type the friend you would like to add");
        String friend = scan.next();

        BufferedReader reader = new BufferedReader(new FileReader("users.txt"));
        String line = reader.readLine();
        while (line != null) {
            if(friend.equals(line)){
                validUser = true;
                PrintWriter writer = new PrintWriter(new FileWriter(name + ".txt", true));
                writer.write("\n");
                writer.write(friend);
                writer.flush();
                writer.close();
                System.out.println("Okay! " + friend + " was added! Would you like to add another friend or start chatting with current friends? [ADD | CHAT]");
                response1 = scan.next();
                break;
            }
            line = reader.readLine();
        }
        if(!validUser){
            System.out.println("Sorry, they don't have an account. Would you like to try adding again or start chatting? [ADD | CHAT]");
            response1 = scan.next();
        }
        /*PrintWriter writer = new PrintWriter(new FileWriter(name + ".txt", true));
        writer.write("\n");
        writer.write(friend);
        writer.flush();
        writer.close();
        dataOutputStream.writeUTF("Okay! " + friend + " was added! Would you like to add another friend or start chatting with current friends? [ADD | CHAT]");
        response1 = dataInputStream.readUTF();*/

        return response1;
    }

    public String[] CHAT(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Vector alreadychatting, Scanner scan, boolean firstOnlineUser, Client client, Socket socket) throws IOException, ParseException {
        String[] array = new String[2];
        String recipient = ""; //0 in array
        String response1 = "CHAT"; //1 in array
        String filename = name + ".txt";
        boolean validResponse = false;
        int chattingnowcount = 0;
        int counter1 = 0;
        int counter2 = 0;
        Scanner scanner1 = new Scanner(System.in);
        Scanner scanner2 = new Scanner(System.in);
        //alreadychatting vector:
        //set up: have pairs of people talking to each other right next to each other
        //ie: Jada Andler Nanu Ina means Jada and Andler are chatting and Nanu and Ina are chatting

        //check notifications
        String notification;
        String friendResponse = " ";
        boolean validFriendResponse = false;
        boolean overallResponse = false;
        boolean skipNotifications = false;
        while(!overallResponse){
            if(!skipNotifications){
                System.out.println("First lets see if there's any notifications for you:");
                //check if have any notifications
                boolean haveNotification = false;
                BufferedReader notifreader = new BufferedReader(new FileReader("usersWithNotifications.txt")); //password will be the very first line of their text file followed by their friends list
                String notifline = notifreader.readLine();
                while(notifline != null){
                    if(name.equals(notifline)){
                        haveNotification = true;
                    }
                    notifline = notifreader.readLine();
                }
                //check if have any unread messages
                boolean haveUnreadMessage = false;
                BufferedReader unreadreader = new BufferedReader(new FileReader("usersWithUnreadMsgs.txt")); //password will be the very first line of their text file followed by their friends list
                String unreadline = unreadreader.readLine();
                while(unreadline != null){
                    if(name.equals(unreadline)){
                        haveUnreadMessage = true;
                    }
                    unreadline = notifreader.readLine();
                }
                if(haveNotification){
                    //now check for notifications
                    //System.out.println("data input stream: " + dataInputStream.readUTF());
                    notification = dataInputStream.readUTF();
                    if(notification != null) {
                        if (notification.contains("would like to chat with you")) {
                            //need to send something to the server indicating that this client got a notification
                            dataOutputStream.writeUTF("notification");
                            //print out notification
                            System.out.println(notification);
                            friendResponse = scan.next();
                            recipient = notification.substring(0, notification.indexOf(" "));
                            //send the name of the person who sent the notification to the server
                            dataOutputStream.writeUTF(recipient);
                            while (!validFriendResponse) {
                                if (friendResponse.equals("YES")) {
                                    counter2 = 3;
                                    validFriendResponse = true;
                                } else if (friendResponse.equals("NO")) {
                                    validFriendResponse = true;
                                } else {
                                    System.out.println("Please enter a valid response [YES | NO]");
                                    friendResponse = scan.next();
                                }
                            }
                            //remove from userswithnotifications txt file
                            File onlineUsers = new File("usersWithNotifications.txt");
                            File tempFile = new File("tempFile.txt");

                            BufferedReader reader = new BufferedReader(new FileReader(onlineUsers));
                            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                            String userToRemove = name;
                            String currentLine;

                            while((currentLine = reader.readLine()) != null) {
                                // trim newline when comparing with lineToRemove
                                String trimmedLine = currentLine.trim();
                                if(trimmedLine.equals(userToRemove)) continue;
                                writer.write(currentLine + System.getProperty("line.separator"));
                            }
                            writer.close();
                            reader.close();
                            boolean successful = tempFile.renameTo(onlineUsers);
                        }
                    }
                }
                else if(haveUnreadMessage){
                    String sender = "";
                    //check the dates & times to get latest messages
                    BufferedReader unreadmsgreader = new BufferedReader(new FileReader(name + "unread.txt")); //password will be the very first line of their text file followed by their friends list
                    String unreadmsgline = unreadmsgreader.readLine();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    Date currdate;
                    Date latestdate = sdf.parse("01/01/2019 00:00:00");
                    while(unreadmsgline != null){
                        if(unreadmsgline.charAt(0) == '0' || unreadmsgline.charAt(0) == '1'){ //hit a line with a date and time
                            currdate = sdf.parse(unreadmsgline);
                            if(currdate.compareTo(latestdate) > 0){
                                //currdate is after latestdate
                                latestdate = currdate;
                                //get the name of the sender
                                sender = unreadmsgreader.readLine();
                            }
                        }
                        unreadmsgline = notifreader.readLine();
                    }
                    String strDate = sdf.format(latestdate);

                    //get the sender's name
                    System.out.println("You have unread messages from " + sender +": ");
                    //print the messages
                    BufferedReader unreadmsgreader2 = new BufferedReader(new FileReader(name + "unread.txt")); //password will be the very first line of their text file followed by their friends list
                    String unreadmsgline2 = unreadmsgreader2.readLine();
                    while(unreadmsgline2 != null){
                        if(unreadmsgline2.equals(strDate)){
                            //print the messages after the sender's name
                            unreadmsgreader2.readLine(); //skip sender's name line
                            unreadmsgline2 = unreadmsgreader2.readLine();
                            while(!unreadmsgline2.equals("LOGOUT")){
                                System.out.println(unreadmsgline2);
                                unreadmsgline2 = unreadmsgreader2.readLine();
                            }
                            break;
                        }
                    }
                    counter2 = 4;

                    //mark messages as READ
                    //maybe don't need to do this because they always read the latest messages..

                    //delete them from users with unreadmessages txt file
                    File unreadUsers = new File("usersWithUnreadMsgs.txt");
                    File tempFile = new File("tempFile.txt");

                    BufferedReader reader = new BufferedReader(new FileReader(unreadUsers));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                    String userToRemove = name;
                    String currentLine;

                    while((currentLine = reader.readLine()) != null) {
                        // trim newline when comparing with lineToRemove
                        String trimmedLine = currentLine.trim();
                        if (trimmedLine.equals(userToRemove)) continue;
                        writer.write(currentLine + System.getProperty("line.separator"));
                    }
                    writer.close();
                    reader.close();
                    boolean successful = tempFile.renameTo(unreadUsers);
                }
                else{
                    System.out.println("No notifications. You can now choose who you'd like to chat with.");
                }
            }

            if(counter2 != 3) {
                System.out.println("hehe");
                //displaying friend's list with online status
                BufferedReader reader1 = new BufferedReader(new FileReader(filename));
                String line = reader1.readLine(); //skip two lines so it doesn't show the password
                line = reader1.readLine();
                Vector<String> onlineFriends = new Vector<>();
                Vector<String> offlineFriends = new Vector<>();
                String friend = "";
                while ((line = reader1.readLine()) != null) {
                    friend = line;
                    BufferedReader reader2 = new BufferedReader(new FileReader("onlineUsers.txt"));
                    String line2;
                    while ((line2 = reader2.readLine()) != null) {
                        if (line2.equals(friend)) {
                            onlineFriends.add(friend);
                        }
                    }
                    Iterator onlinevalue = onlineFriends.iterator();
                    int friendcount = 0;
                    while (onlinevalue.hasNext()) {
                        if (onlinevalue.next().equals(friend)) {
                            friendcount++;
                        }
                    }
                    if (friendcount == 0) {
                        offlineFriends.add(friend);
                    }
                }
                friend = recipient;
                if (!skipNotifications) {
                    //System.out.println("onlinefriends: " +onlineFriends);
                    //System.out.println("offilnefriends: " +offlineFriends);
                    Iterator onlinevalue = onlineFriends.iterator();
                    Iterator offlinevalue = offlineFriends.iterator();
                    System.out.println("Here is your friends list and who is online: \n");
                    while (onlinevalue.hasNext()) {
                        System.out.println(onlinevalue.next() + ": online");
                    }
                    while (offlinevalue.hasNext()) {
                        System.out.println(offlinevalue.next() + ": offline");
                    }

                    //getting user input
                    System.out.println("already chatting before: " + alreadychatting);
                    System.out.println("already chtting size: " + alreadychatting.size());
                    System.out.println("Who would you like to chat with?");
                    recipient = scan.next();
                }

                //check if they are friends with them
                BufferedReader reader2 = new BufferedReader(new FileReader(filename));
                line = reader2.readLine();
                while (line != null) {
                    line = line.replaceAll("\\s+", "");
                    if (recipient.equals(line)) { //friend is in the list
                        counter1++;
                    }
                    line = reader2.readLine();
                }
                while (counter1 == 0) {
                    System.out.println("Sorry you're not friends with them! Please choose someone you are friends with: ");
                    Iterator anotheronlinevalue = onlineFriends.iterator();
                    Iterator anotherofflinevalue = offlineFriends.iterator();
                    while (anotheronlinevalue.hasNext()) {
                        System.out.println(anotheronlinevalue.next() + ": online");
                    }
                    while (anotherofflinevalue.hasNext()) {
                        System.out.println(anotherofflinevalue.next() + ": offline");
                    }
                    System.out.println("Who would you like to chat with?");
                    recipient = scan.next();

                    //check if they are friends with them
                    BufferedReader readerforfriend = new BufferedReader(new FileReader(filename));
                    line = readerforfriend.readLine();
                    while (line != null) {
                        line = line.replaceAll("\\s+", "");
                        if (recipient.equals(line)) { //friend is in the list
                            counter1++;
                        }
                        line = readerforfriend.readLine();
                    }
                }

                //check if they're online
                int onlineCount = 0;
                Iterator onlinevalue2 = onlineFriends.iterator();
                if(!onlinevalue2.hasNext()){ //no one is online
                    onlineCount++;
                }
                while (onlinevalue2.hasNext()) {
                    if (onlinevalue2.next().equals(recipient)) {
                        //dataOutputStream.writeUTF(recipient); //give recipient name to server
                        onlineCount = 0;
                        break;
                    } else {
                        onlineCount++;
                    }
                }
                while (onlineCount != 0) {
                    System.out.println("Sorry they're currently offline! Please type the name of a friend that is online: ");
                    recipient = scan.next();
                    Iterator onlinevalue3 = onlineFriends.iterator();
                    while (onlinevalue3.hasNext()) {
                        if (onlinevalue3.next().equals(recipient)) {
                            onlineCount = 0;
                            break;
                        } else {
                            onlineCount++;
                        }
                    }
                }

                if(!skipNotifications){
                    //give recipient to server
                    dataOutputStream.writeUTF(recipient);
                    System.out.println("i wrote recipient to server");
                    System.out.println("recipient at this point: " +recipient);

                    //at this point the server will send back data on counter2, indicating if someone is chatting with someone else
                    counter2 = Integer.parseInt(dataInputStream.readUTF());
                    System.out.println("counter2: " +counter2);
                }

                if(counter2 == 1){ //they are already chatting with someone

                    System.out.println("Sorry they are already chatting with another friend!" +
                            " Did you want to send them messages that they can view later or chat with someone else? [THEM | ANOTHER]");
                    response1 = scan.next();
                    System.out.println("response1: " +response1);

                    while(!validResponse){
                        if(response1.equals("ANOTHER")) {
                            //display friends list overallresponse == false
                            validResponse = true;
                            while(counter2 == 1){
                                //display friends list
                                System.out.println("Please choose another friend. They are already chatting with someone else.");
                                Iterator anotheronlinevalue = onlineFriends.iterator();
                                Iterator anotherofflinevalue = offlineFriends.iterator();
                                while(anotheronlinevalue.hasNext()){
                                    System.out.println(anotheronlinevalue.next() + ": online");
                                }
                                while(anotherofflinevalue.hasNext()){
                                    System.out.println(anotherofflinevalue.next() + ": offline");
                                }
                                //get recipient
                                System.out.println("Who would you like to chat with?");
                                recipient = scan.next();
                                //send recipient
                                dataOutputStream.writeUTF(recipient);
                                System.out.println("just sent recipient");
                                //get counter2 back if its 1 keep asking
                                counter2 = Integer.parseInt(dataInputStream.readUTF());
                                //System.out.println("counter 2 in another: " +counter2);
                                if(counter2 != 1){ //not already chatting with someone
                                    break;
                                }
                                //if not exit while
                            }
                           // System.out.println("i broke out of while loop");
                            skipNotifications = true;
                        }
                        else if(response1.equals("THEM")){
                            System.out.println("Okay! Go ahead and start sending messages to " + recipient + ". She will see them later when she wants to chat with you!" +
                                    " After you're done sending one message, press enter. You can keep sending other messages in the same way. When you're done sending messages, please 'LOGOUT'.");
                            validResponse = true;
                            String unreadmsg = "";
                            //write the date and time the msg was sent and who sent it
                            Date date = new Date();
                            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss");
                            String currdatetime = dateFormat.format(date);
                            BufferedWriter writer = new BufferedWriter(new FileWriter(recipient + "unread.txt", true));
                            writer.write(currdatetime);
                            writer.newLine();
                            writer.write(name);
                            writer.newLine();
                            //append the msgs
                            while(!unreadmsg.equals("LOGOUT")){
                                unreadmsg = scan.nextLine();
                                System.out.println("unreadmsg: " +unreadmsg);
                                writer.write(unreadmsg);
                                writer.newLine();
                            }
                            writer.flush();
                            writer.close();
                            //add te recipient to userswithunreadmsgs.txt
                            BufferedWriter unreadwriter = new BufferedWriter(new FileWriter("usersWithUnreadMsgs.txt", true));
                            unreadwriter.write(recipient);
                            unreadwriter.newLine();
                            unreadwriter.flush();
                            unreadwriter.close();
                            //will end up sending 'LOGOUT' to stepcount 4
                            dataOutputStream.writeUTF("LOGOUT");
                            overallResponse = true;
                            System.out.println("you are now logged out!");
                            dataInputStream.close();
                            dataOutputStream.close();
                            socket.close();
                        }
                        else{
                            System.out.println("Please input a valid response [THEM | ANOTHER]");
                            response1 = scan.next();
                        }
                    }
                    validResponse = false;

                }

                //send a notification to the friend saying they want to chat with them
                else if(counter2 == 0){
                    //add them to the usersWithNotifications.txt file
                    System.out.println("im in counter2 == 0");
                    BufferedWriter notifwriter = new BufferedWriter(new FileWriter("usersWithNotifications.txt", true));
                    notifwriter.write(recipient);
                    notifwriter.newLine();
                    notifwriter.flush();
                    notifwriter.close();
                    dataOutputStream.writeUTF(name + " would like to chat with you. Would you like to chat with them? [YES | NO]");

                    //retrieve their response
                    System.out.println("Waiting for " + recipient + "'s response..");
                    friendResponse = dataInputStream.readUTF();
                    String sendChatResponse = "";
                    String unreadmsgs = "";
                    boolean sendChat = false;
                    if(friendResponse.equals("accepted")){
                        response1 = "CHAT";
                        System.out.println(recipient + " has accepted your request! Go ahead and start sending each other messages! :)");
                        sendChat = true;
                        overallResponse = true;
                    }
                    else{
                        System.out.println("Sorry, " + recipient + " does not want to chat with you right now. Did you want to send them messages that they can view " +
                                "later or try to chat with someone else? [SEND | CHAT]");
                        sendChatResponse = scan.next();
                    }
                    while(!sendChat) {
                        if (sendChatResponse.equals("SEND")) {
                            System.out.println("Okay! Go ahead and start sending messages that " + recipient + " can see later. When you're done, please type 'LOGOUT'.");
                            unreadmsgs = scan.next();
                            Date currdate = new Date();

                            BufferedWriter writer = new BufferedWriter(new FileWriter("unread" + recipient + ".txt", true));
                            writer.write(name);
                            writer.newLine();
                            writer.write("hi");
                            writer.flush();
                            writer.close();

                            while (!unreadmsgs.equals("LOGOUT")) {
                                writer.write(unreadmsgs);
                                writer.newLine();
                                writer.flush();
                                writer.close();
                            }

                            sendChat = true;

                        } else if (sendChatResponse.equals("CHAT")) {
                            response1 = "CHATDISP";
                            sendChat = true;

                        } else {
                            System.out.println("Please enter a valid response [SEND | CHAT]");
                            sendChatResponse = scan.next();
                        }
                    }
                }

                //they are already chatting with them
                else if(counter2 == 2){
                    response1 = "CHAT";
                    System.out.println(friend + " has already requested to chat with you too! Go ahead and start sending each other messages! :)");
                    overallResponse = true;
                }
            }
            //they received a notification and want to chat with them
            else{ //counter == 3
                response1 = "CHAT";
                System.out.println("Request accepted! Go ahead and chat with " +recipient);
                overallResponse = true;
            }
        }

        array[0] = recipient;
        array[1] = response1;
        counter1 = 0;
        counter2 = 0;

        return array;
    }

    public Vector ResponseHandler1(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Client client, String response1, Vector alreadychatting, Scanner scan, boolean firstOnlineUser, Socket socket) throws IOException, ParseException {

        String recipient = null;
        String response2 = null;
        boolean validResponse = false;
        String[] chatReturn;
        Vector<Object> responseHandlerReturn = new Vector<>();
        //0: recipient
        //1: response1
        //2: validresponse

        if (response1.equals("ADD")) {
            response2 = client.ADD(dataOutputStream, dataInputStream, name, scan);
            if (response2.equals("ADD")) {
                response1 = response2;
                validResponse = false;
            } else if (response2.equals("CHAT")) {
                response1 = response2;
                validResponse = false;
            } else {
                System.out.println("please enter a valid response [ADD | CHAT]");
                response1 = scan.next();
                validResponse = false;
            }
        } else if (response1.equals("CHAT")) {
            chatReturn = client.CHAT(dataOutputStream, dataInputStream, name, alreadychatting, scan, firstOnlineUser, client, socket);
            response2 = chatReturn[1];
            if (response2.equals("ADD")) {
                response1 = response2;
                validResponse = false;
            } else if (response2.equals("CHAT")) {
                response1 = response2;
                validResponse = true; //not sure if this is right yet
            } else if (response2.equals("CHATDISP")) {
                response1 = "CHAT";
                validResponse = false;
            } else {
                System.out.println("please enter a valid response [ADD | CHAT]");
                response1 = scan.next();
                validResponse = false;
            }

            recipient = chatReturn[0];
            responseHandlerReturn.add(0, recipient);
            //validResponse = true;
        } else {
            System.out.println("please enter a valid response [ADD | CHAT]");
            response1 = scan.next();
            validResponse = false;
        }

        responseHandlerReturn.add(0, recipient);
        responseHandlerReturn.add(1, response1);
        responseHandlerReturn.add(2, validResponse);

        return responseHandlerReturn;

    }


}