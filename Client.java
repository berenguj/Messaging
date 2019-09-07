import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.*;

public class Client {

    //variables
    private static boolean existingUser = false;
    private  static boolean correctPassword = false;
    private  static boolean validResponse = false;
    private  static boolean initialLogin = false;
    private static boolean firstOnlineUser = true;
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
            System.out.println("existing user:" +existingUser);

            //login an existing user
            while(!initialLogin) {

                if ((existingUser && signupORlogin.equals("LOGIN"))) {

                    System.out.println("oneline users before: " + onlineusers);
                    for (int i = 0; i < onlineusers.size(); i++) {
                        if (onlineusers.get(i).equals(name)) {
                            System.out.println("Sorry you are already logged on! Please use the other window instead.");
                            System.exit(10);
                        }
                    }

                    //first online user check
                    File onlineUsersFile = new File("onlineusers.txt");
                    if(onlineUsersFile.length() != 0){
                        firstOnlineUser = false;
                    }
                    System.out.println("firstonlineuser: " + firstOnlineUser);

                    //give name to server
                    dataOutputStream.writeUTF(name); //gives the name to the server
                    System.out.println("Welcome back " + name + "!" + " Please type in your password: ");

                    String password = scan.next();

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

                    initialLogin = true;
                    System.out.println("You are now logged in! You can logout anytime during chatting by typing 'LOGOUT'. Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");
                    //first check if they are the first person online or else there will be no notifications to read later on

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
                    System.out.println("existignuser: " +existingUser);
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

                responseHandlerReturn = client.ResponseHandler2(dataOutputStream, dataInputStream, name, client, response1, alreadychatting, scan, firstOnlineUser);
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

    public String[] CHAT(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Vector alreadychatting, Scanner scan, boolean firstOnlineUser) throws IOException {
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
        boolean mutualChat = false;
        System.out.println("First lets see if there's any notifications for you:");
        //first check if they are the first person online or else there will be no notifications to read
        if(firstOnlineUser){
            System.out.println("No notifications. You can now choose who you'd like to chat with.");
        }
        else{
            //now check for notifications
            //System.out.println("data input stream: " + dataInputStream.readUTF());
            notification = dataInputStream.readUTF();
            if(notification != null){
                if(notification.contains("would like to chat with you")){
                    //need to send something to the server indicating that this client got a notification
                    dataOutputStream.writeUTF("notification");
                    //print out notification
                    System.out.println(notification);
                    friendResponse = scan.next();
                    recipient = notification.substring(0, notification.indexOf(" "));
                    //send the name of the person who sent the notification to the server
                    dataOutputStream.writeUTF(recipient);
                    while(!validFriendResponse){
                        if(friendResponse.equals("YES")){
                            counter2 = 3;
                            validFriendResponse = true;
                        }
                        else if(friendResponse.equals("NO")){
                            validFriendResponse = true;
                        }
                        else{
                            System.out.println("Please enter a valid response [YES | NO]");
                            friendResponse = scan.next();
                        }
                    }
                }
                else{
                    System.out.println("No notifications. You can now choose who you'd like to chat with.");
                }
            }
        }

        if(counter2 != 3){
            //displaying friend's list with online status
            BufferedReader reader1 = new BufferedReader(new FileReader(filename));
            String line = reader1.readLine(); //skip two lines so it doesn't show the password
            line = reader1.readLine();
            System.out.println("Here is your friends list and who is online: \n");
            Vector<String> onlineFriends = new Vector<>();
            Vector<String> offlineFriends = new Vector<>();
            String friend = "";
            while ((line = reader1.readLine()) != null) {
                friend = line;
                BufferedReader reader2 = new BufferedReader(new FileReader("onlineUsers.txt"));
                String line2;
                while((line2 = reader2.readLine()) != null){
                    if(line2.equals(friend)){
                        onlineFriends.add(friend);
                    }
                }
                Iterator onlinevalue = onlineFriends.iterator();
                int friendcount = 0;
                while(onlinevalue.hasNext()){
                    if(onlinevalue.next().equals(friend)){
                        friendcount++;
                    }
                }
                if(friendcount == 0) {
                    offlineFriends.add(friend);
                }
            }
            System.out.println("onlinefriends: " +onlineFriends);
            System.out.println("offilnefriends: " +offlineFriends);
            Iterator onlinevalue = onlineFriends.iterator();
            Iterator offlinevalue = offlineFriends.iterator();
            while(onlinevalue.hasNext()){
                System.out.println(onlinevalue.next() + ": online");
            }
            while(offlinevalue.hasNext()){
                System.out.println(offlinevalue.next() + ": offline");
            }

            //getting user input
            System.out.println("already chatting before: " +alreadychatting);
            System.out.println("already chtting size: " +alreadychatting.size());
            System.out.println("Who would you like to chat with?");
            recipient = scan.next();

            //check if they're online
            int onlineCount = 0;
            Iterator onlinevalue2 = onlineFriends.iterator();
            while(onlinevalue2.hasNext()){
                if(onlinevalue2.next().equals(recipient)){
                    dataOutputStream.writeUTF(recipient); //give recipient name to server
                    onlineCount = 0;
                    break;
                }
                else{
                    onlineCount++;
                }
            }
            while(onlineCount != 0){
                System.out.println("Sorry they're currently offline! Please type the name of a friend that is online: ");
                recipient = scan.next();
                Iterator onlinevalue3 = onlineFriends.iterator();
                while(onlinevalue3.hasNext()){
                    if(onlinevalue3.next().equals(recipient)){
                        dataOutputStream.writeUTF(recipient);
                        onlineCount = 0;
                        break;
                    }
                    else{
                        onlineCount++;
                    }
                }
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

            //at this point the server will send back data on counter2, indicating if someone is chatting with someone else
            counter2 = Integer.parseInt(dataInputStream.readUTF());
            System.out.println("counter2: " +counter2);

            if (counter1 == 0) { //counter didn't increase
                System.out.println("Sorry you're not friends with them! Did you want to add them or chat with a different friend? [ADD | CHAT | LOGOUT]");
                response1 = scan.next();
                if (response1.equals("CHAT")) {
                    response1 = "CHATDISP"; //CHATDISP: display friends list and ask who they want to chat with
                }
            }
            else if(counter2 == 1){ //they are already chatting with someone
                System.out.println("Sorry they are already chatting with another friend!" +
                        " Did you want to send them messages that they can view later? [THEM | ANOTHER]");
                response1 = scan.next();
                System.out.println("response1: " +response1);
                while(!validResponse){
                    if (response1.equals("ANOTHER")) {
                        System.out.println("im in here 2");
                        response1 = "CHATDISP"; //CHATDISP: display friends list and ask who they want to chat with
                        validResponse = true;
                    }
                    else if(response1.equals("THEM")){
                        System.out.println("im in here 1");
                        response1 = "CHAT"; //they can send unread messages to their friend
                        System.out.println("Okay! Go ahead and start sending messages to " + recipient + ". She will see them later when she wants to chat with you!");
                        validResponse = true;
                    }
                    else{
                        System.out.println("Please input a valid response [CHAT WITH THEM | CHAT WITH SOMEONE ELSE]");
                        response1 = scan.next();
                    }
                }
                validResponse = false;
            }

            //send a notification to the friend saying they want to chat with them
            else if(counter2 == 0){
                dataOutputStream.writeUTF(name + " would like to chat with you. Would you like to chat with them? [YES | NO]");
                //retrieve their response
                friendResponse = dataInputStream.readUTF();
                System.out.println("friendresponse: " +friendResponse);
                String sendChatResponse = "";
                String unreadmsgs = "";
                if(friendResponse.equals("accepted")){
                    response1 = "CHAT";
                    System.out.println(friend + " has accepted your request! Go ahead and start sending each other messages! :)");
                }
                else{
                    System.out.println("Sorry, " + recipient + " does not want to chat with you right now. Did you want to send them messages that they can view " +
                            "later or try to chat with someone else? [SEND | CHAT]");
                    sendChatResponse = scan.next();
                }
                while(sendChatResponse != null){
                    System.out.println("Please enter a valid response [SEND | CHAT]");
                    sendChatResponse = scan.next();
                }
                if(sendChatResponse.equals("SEND")){
                    System.out.println("Okay! Go ahead and start sending messages that " + recipient + " can see later. When you're done, please type 'LOGOUT'.");
                    unreadmsgs = scan.next();
                    Date currdate = new Date();

                    BufferedWriter writer = new BufferedWriter(new FileWriter("unread" + recipient + ".txt", true));
                    writer.write(name);
                    writer.newLine();
                    writer.write("hi");
                    writer.flush();
                    writer.close();

                    while(!unreadmsgs.equals("LOGOUT")){
                        writer.write(unreadmsgs);
                        writer.newLine();
                        writer.flush();
                        writer.close();
                    }

                }
                else if(sendChatResponse.equals("CHAT")){
                    response1 = "CHATDISP";
                }
            }

            //they are already chatting with them
            else if(counter2 == 2){
                response1 = "CHAT";
                System.out.println(friend + " has already requested to chat with you too! Go ahead and start sending each other messages! :)");
            }
        }
        //they received a notification and want to chat with them
        else{ //counter == 3
            response1 = "CHAT";
            System.out.println("Request accepted! Go ahead and chat with " +recipient);
        }

        //are able to chat with them
        /*else if(counter2 == 2 || counter2 == 0){
            response1 = "CHAT";
            System.out.println("Okay! Go ahead and start sending messages to " + recipient);
        }*/

        array[0] = recipient;
        array[1] = response1;
        counter1 = 0;
        counter2 = 0;

        return array;
    }

    public Vector ResponseHandler2(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Client client, String response1, Vector alreadychatting, Scanner scan, boolean firstOnlineUser) throws IOException {

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
            chatReturn = client.CHAT(dataOutputStream, dataInputStream, name, alreadychatting, scan, firstOnlineUser);
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

    public static String getName(){
        return name;
    }

    public static String getRecipient(){
        return recipient;
    }



}