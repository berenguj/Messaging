import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class Client {

    //variables
    private static boolean existingUser = false;
    private  static boolean correctPassword = false;
    private  static boolean validResponse = false;
    private  static Object validResponseObject;
    private  static Object recipientObject;
    private  static Object response1Object;
    private  static String response1; //String addChat;
    private  static String response2; //String secondAddChat;
    private static String signupORlogin;
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
                    dataOutputStream.writeUTF(name); //gives the name to the server
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
            if (existingUser) {

                System.out.println("oneline users before: " +onlineusers);
                for(int i = 0; i < onlineusers.size(); i++){
                    if(onlineusers.get(i).equals(name)){
                        System.out.println("Sorry you are already logged on! Please use the other window instead.");
                        System.exit(10);
                    }
                }

                System.out.println("Welcome back " + name + "!" + " Please type in your password: ");
                String password = scan.next();
                //dataOutputStream.writeUTF("Welcome back! Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");

                BufferedReader reader2 = new BufferedReader(new FileReader(name + ".txt")); //password will be the very first line of their text file followed by their friends list
                for(int i = 0; i < 2; i++){
                    line = reader2.readLine();
                    if (password.equals(line)) {
                        correctPassword = true;
                    } else {
                        correctPassword = false;
                    }
                }

                while(!correctPassword){
                    System.out.println("Incorrect password. Please try again: ");
                    password = scan.next();
                    BufferedReader reader3 = new BufferedReader(new FileReader(name + ".txt"));
                    for(int i = 0; i < 2; i++){
                        line = reader3.readLine();
                        if (password.equals(line)) {
                            correctPassword = true;
                        } else {
                            correctPassword = false;
                        }
                    }
                }

                System.out.println("You are now logged in! You can logout anytime during chatting by typing 'LOGOUT'. Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");


                //add them to the online users file list
                BufferedWriter writer = new BufferedWriter(new FileWriter("onlineusers.txt", false));
                writer.write(name);
                writer.newLine();
                writer.flush();
                writer.close();

            }

            //add the new user to the user list and log them in
            else if (!existingUser) {
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
                System.out.println("You are now signed up! You can logout anytime while you're chatting by typing 'LOGOUT'. Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");
            }

            //based on response from user, ask if they would like to add a friend or chat with someone
            //consider all possible cases like adding then chatting, or invalid responses, etc
            response1 = scan.next();
            //responseHandlerReturn = server.ResponseHandler(dataOutputStream, dataInputStream, name, server, response1);
            while (!validResponse) {

                responseHandlerReturn = client.ResponseHandler2(dataOutputStream, dataInputStream, name, client, response1, alreadychatting, scan);
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
                            //write message
                            dataOutputStream.writeUTF(msg);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    try{
                        dataOutputStream.writeUTF("Your friend has left the chat. Please login again and choose another friend to chat with!");
                        System.out.println("you are now logged out!");
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
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
                            e.printStackTrace();
                        }
                    }
                }
            });

            //start the threads
            sendMessage.start();
            readMessage.start();

        }
        catch (Exception e){
            e.printStackTrace();
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

    public String[] CHAT(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Vector alreadychatting, Scanner scan) throws IOException {
        String[] array = new String[2];
        String recipient; //0 in array
        String response1 = "CHAT"; //1 in array
        String filename = name + ".txt";
        boolean validResponse = false;
        int chattingnowcount = 0;
        int counter1 = 0;
        int counter2 = 0;
        //alreadychatting vector:
        //set up: have pairs of people talking to each other right next to each other
        //ie: Jada Andler Nanu Ina means Jada and Andler are chatting and Nanu and Ina are chatting

        System.out.println("name: " + name);
        System.out.println("filename: " + name + ".txt");
        BufferedReader reader1 = new BufferedReader(new FileReader(filename));
        String line = reader1.readLine(); //skip two lines so it doesn't show the password
        line = reader1.readLine();
        System.out.println("Here is your friends list: \n");
        while ((line = reader1.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("already chatting before: " +alreadychatting);
        System.out.println("already chtting size: " +alreadychatting.size());
        System.out.println("Who would you like to chat with?");
        recipient = scan.next();
        dataOutputStream.writeUTF(recipient); //gives the recipient's name to the server
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
        System.out.println("i can get here 1");
        //check if they are already chatting with someone or chose to already chat with you
        for(int i = 0; i < alreadychatting.size(); i++){
            System.out.println("i can get here 2");
            System.out.println("already chatting get i: " +alreadychatting.get(i));
            System.out.println("already chatting get i+1: " +alreadychatting.get(i+1));
            /*if((alreadychatting.get(i).equals(name) || alreadychatting.get(i).equals(recipient)) && (alreadychatting.get(i+1).equals(name) || alreadychatting.get(i+1).equals(recipient))){
                counter2+=2;
                dataOutputStream.writeUTF("i can get here 3");
            }*/
            if((alreadychatting.get(i).equals(name) && alreadychatting.get(i+1).equals(recipient)) || (alreadychatting.get(i).equals(recipient) && alreadychatting.get(i+1).equals(name))){
                counter2+=2;
                System.out.println("i can get here 3");
            }
            else if(alreadychatting.get(i).equals(name) || alreadychatting.get(i).equals(recipient) || alreadychatting.get(i+1).equals(name) || alreadychatting.get(i+1).equals(recipient)){
                counter2++;
                System.out.println("i can get here 4");
            }
            i+=2;
        }

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
                    "Did you want to send them messages that they can view later? [CHAT WITH THEM | CHAT WITH SOMEONE ELSE]");
            response1 = scan.next();
            while(!validResponse){
                if (response1.equals("CHAT WITH SOMEONE ELSE")) {
                    response1 = "CHATDISP"; //CHATDISP: display friends list and ask who they want to chat with
                    validResponse = true;
                }
                else if(response1.equals("CHAT WITH THEM")){
                    response1 = "CHAT"; //they can send unread messages to their friend
                    dataOutputStream.writeUTF("Okay! Go ahead and start sending messages to " + recipient + ". She will see them later when she wants to chat with you!");
                    validResponse = true;
                }
                else{
                    System.out.println("Please input a valid response [CHAT WITH THEM | CHAT WITH SOMEONE ELSE]");
                    response1 = scan.next();
                }
            }
            validResponse = false;
            /*if (response1.equals("CHAT WITH SOMEONE ELSE")) {
                response1 = "CHATDISP"; //CHATDISP: display friends list and ask who they want to chat with
            }
            else if(response1.equals("CHAT WITH THEM")){

            }
            else{
                dataOutputStream.writeUTF("Please input a valid response [CHAT WITH THEM | CHAT WITH SOMEONE ELSE]");
            }*/
        }
        else if(counter2 == 2 || counter2 == 0){
            response1 = "CHAT";
            System.out.println("Okay! Go ahead and start sending messages to " + recipient);

            //check if they are already chatting with someone
            /*for(int i = 0; i < alreadychatting.size(); i++){
                if(alreadychatting.get(i).equals(name) || alreadychatting.get(i).equals(recipient)){
                    counter2++;
                }
                else if(alreadychatting.get(i++).equals(name) || alreadychatting.get(i++).equals(recipient)){
                    counter2++;
                }
                i+=2;
            }*/

            //add them to the already chatting vector
            if(counter2 == 0){
                alreadychatting.add(name);
                alreadychatting.add(recipient);
            }

            System.out.println("already chatting after: " +alreadychatting);
        }

        array[0] = recipient;
        array[1] = response1;
        counter1 = 0;
        counter2 = 0;

        return array;
    }

    public Vector ResponseHandler2(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Client client, String response1, Vector alreadychatting, Scanner scan) throws IOException {

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
            chatReturn = client.CHAT(dataOutputStream, dataInputStream, name, alreadychatting, scan);
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

        System.out.println("repsonse one in function: " +response1);
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