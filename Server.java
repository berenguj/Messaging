import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class Server {

    //vector to store active clients
    static Vector<ClientHandler> clients = new Vector<>();


    public static void main(String args[]) throws IOException {

        //variables
        Server server = new Server();
        boolean existingUser = false;
        boolean correctPassword = false;
        boolean validResponse = false;
        Object validResponseObject;
        Object recipientObject;
        Object response1Object;
        String response1; //String addChat;
        String response2; //String secondAddChat;
        String signupORlogin;
        String name = null;
        String recipient = null;
        String[] chatReturn;
        Vector<Object> responseHandlerReturn = new Vector<>();
        Vector<String> alreadychatting = new Vector<String>();
        Vector<String> onlineusers = new Vector<>();

        //server listening on port 5056
        ServerSocket serverSocket = new ServerSocket(5056);

        //run infinite loop to get client request
        while (true) {
            Socket s = null;

            try {
                //socket object to receive incoming client request
                s = serverSocket.accept();

                System.out.println("A new client is connected: " + s);

                //obtain input and output streams
                DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());

                //ask for username from client and add them to the user list
                dataOutputStream.writeUTF("Would you like to sign up for an account or login to an existing account?:  [SIGN UP | LOGIN]");
                signupORlogin = dataInputStream.readUTF();

                while(!validResponse){
                    if(signupORlogin.equals("SIGN UP")){
                        dataOutputStream.writeUTF("Please type in the username you would like: ");
                        name = dataInputStream.readUTF();
                        validResponse = true;
                    }
                    else if(signupORlogin.equals("LOGIN")){
                        dataOutputStream.writeUTF("Please type in your username: ");
                        name = dataInputStream.readUTF();
                        validResponse = true;
                    }
                    else{
                        dataOutputStream.writeUTF("Please enter a valid response [SIGN UP | LOGIN]");
                        signupORlogin = dataInputStream.readUTF();
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

                    dataOutputStream.writeUTF("oneline users before: " +onlineusers);
                    for(int i = 0; i < onlineusers.size(); i++){
                        if(onlineusers.get(i).equals(name)){
                            dataOutputStream.writeUTF("Sorry you are already logged on! Please use the other window instead.");
                            System.exit(10);
                        }
                    }

                    dataOutputStream.writeUTF("Welcome back " + name + "!" + " Please type in your password: ");
                    String password = dataInputStream.readUTF();
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
                        dataOutputStream.writeUTF("Incorrect password. Please try again: ");
                        password = dataInputStream.readUTF();
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

                    dataOutputStream.writeUTF("You are now logged in! You can logout anytime during chatting by typing 'LOGOUT'. Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");


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
                    dataOutputStream.writeUTF("Your username is set! Please type in a password: ");
                    String password = dataInputStream.readUTF();

                    PrintWriter writer2 = new PrintWriter(new FileWriter(name + ".txt", true));
                    writer2.write("\n");
                    writer2.write(password);
                    writer2.flush();
                    writer2.close();
                    dataOutputStream.writeUTF("You are now signed up! You can logout anytime while you're chatting by typing 'LOGOUT'. Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");
                }

                //based on response from user, ask if they would like to add a friend or chat with someone
                //consider all possible cases like adding then chatting, or invalid responses, etc
                response1 = dataInputStream.readUTF();
                //responseHandlerReturn = server.ResponseHandler(dataOutputStream, dataInputStream, name, server, response1);
                while (!validResponse) {

                    responseHandlerReturn = server.ResponseHandler2(dataOutputStream, dataInputStream, name, server, response1, alreadychatting);
                    validResponseObject = responseHandlerReturn.get(2);
                    validResponse = (boolean) validResponseObject;
                    recipientObject = responseHandlerReturn.get(0);
                    recipient = (String) recipientObject;
                    response1Object = responseHandlerReturn.get(1);
                    response1 = (String) response1Object;

                }
                validResponse = false;

                //dataOutputStream.writeUTF("Who would you like to talk to?");
                //String recipient = dataInputStream.readUTF();
                //dataOutputStream.writeUTF("Okay! Go ahead and start sending messages to " +recipient);

                System.out.println("Creating new handler for this client...");

                //create new handler
                //ClientHandler handler = new ClientHandler(s, counter, dataInputStream, dataOutputStream);
                ClientHandler handler = new ClientHandler(s, name, dataInputStream, dataOutputStream, recipient);

                //create new thread
                Thread thread = new Thread(handler);

                //add to list of clients
                clients.add(handler);

                //invoke start method on thread
                thread.start();
                System.out.println("Starting the thread for this client: " + name);

                //increment counter for new client
                //counter++;
                //System.out.println(counter);
            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }


    }

    public String ADD(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name) throws IOException {
        boolean validUser = false;
        String response1 = null;
        dataOutputStream.writeUTF("Please type the friend you would like to add");
        String friend = dataInputStream.readUTF();

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
                dataOutputStream.writeUTF("Okay! " + friend + " was added! Would you like to add another friend or start chatting with current friends? [ADD | CHAT]");
                response1 = dataInputStream.readUTF();
                break;
            }
            line = reader.readLine();
        }
        if(!validUser){
            dataOutputStream.writeUTF("Sorry, they don't have an account. Would you like to try adding again or start chatting? [ADD | CHAT]");
            response1 = dataInputStream.readUTF();
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

    public String[] CHAT(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Vector alreadychatting) throws IOException {
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
        dataOutputStream.writeUTF("Here is your friends list: \n");
        while ((line = reader1.readLine()) != null) {
            dataOutputStream.writeUTF(line);
        }
        dataOutputStream.writeUTF("already chatting before: " +alreadychatting);
        dataOutputStream.writeUTF("already chtting size: " +alreadychatting.size());
        dataOutputStream.writeUTF("Who would you like to chat with?");
        recipient = dataInputStream.readUTF();
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
        dataOutputStream.writeUTF("i can get here 1");
        //check if they are already chatting with someone or chose to already chat with you
        for(int i = 0; i < alreadychatting.size(); i++){
            dataOutputStream.writeUTF("i can get here 2");
            dataOutputStream.writeUTF("already chatting get i: " +alreadychatting.get(i));
            dataOutputStream.writeUTF("already chatting get i+1: " +alreadychatting.get(i+1));
            /*if((alreadychatting.get(i).equals(name) || alreadychatting.get(i).equals(recipient)) && (alreadychatting.get(i+1).equals(name) || alreadychatting.get(i+1).equals(recipient))){
                counter2+=2;
                dataOutputStream.writeUTF("i can get here 3");
            }*/
            if((alreadychatting.get(i).equals(name) && alreadychatting.get(i+1).equals(recipient)) || (alreadychatting.get(i).equals(recipient) && alreadychatting.get(i+1).equals(name))){
                counter2+=2;
                dataOutputStream.writeUTF("i can get here 3");
            }
            else if(alreadychatting.get(i).equals(name) || alreadychatting.get(i).equals(recipient) || alreadychatting.get(i+1).equals(name) || alreadychatting.get(i+1).equals(recipient)){
                counter2++;
                dataOutputStream.writeUTF("i can get here 4");
            }
            i+=2;
        }

        dataOutputStream.writeUTF("counter2: " +counter2);

        if (counter1 == 0) { //counter didn't increase
            dataOutputStream.writeUTF("Sorry you're not friends with them! Did you want to add them or chat with a different friend? [ADD | CHAT | LOGOUT]");
            response1 = dataInputStream.readUTF();
            if (response1.equals("CHAT")) {
                response1 = "CHATDISP"; //CHATDISP: display friends list and ask who they want to chat with
            }
        }
        else if(counter2 == 1){ //they are already chatting with someone
            dataOutputStream.writeUTF("Sorry they are already chatting with another friend!" +
                    "Did you want to send them messages that they can view later? [CHAT WITH THEM | CHAT WITH SOMEONE ELSE]");
            response1 = dataInputStream.readUTF();
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
                    dataOutputStream.writeUTF("Please input a valid response [CHAT WITH THEM | CHAT WITH SOMEONE ELSE]");
                    response1 = dataInputStream.readUTF();
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
            dataOutputStream.writeUTF("Okay! Go ahead and start sending messages to " + recipient);

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

            dataOutputStream.writeUTF("already chatting after: " +alreadychatting);
        }

        array[0] = recipient;
        array[1] = response1;
        counter1 = 0;
        counter2 = 0;

        return array;
    }

    public Vector ResponseHandler2(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Server server, String response1, Vector alreadychatting) throws IOException {

        String recipient = null;
        String response2 = null;
        boolean validResponse = false;
        String[] chatReturn;
        Vector<Object> responseHandlerReturn = new Vector<>();
        //0: recipient
        //1: response1
        //2: validresponse

        if (response1.equals("ADD")) {
            response2 = server.ADD(dataOutputStream, dataInputStream, name);
            if (response2.equals("ADD")) {
                response1 = response2;
                validResponse = false;
            } else if (response2.equals("CHAT")) {
                response1 = response2;
                validResponse = false;
            } else {
                dataOutputStream.writeUTF("please enter a valid response [ADD | CHAT]");
                response1 = dataInputStream.readUTF();
                validResponse = false;
            }
        } else if (response1.equals("CHAT")) {
            chatReturn = server.CHAT(dataOutputStream, dataInputStream, name, alreadychatting);
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
                dataOutputStream.writeUTF("please enter a valid response [ADD | CHAT]");
                response1 = dataInputStream.readUTF();
                validResponse = false;
            }

            recipient = chatReturn[0];
            responseHandlerReturn.add(0, recipient);
            //validResponse = true;
        } else {
            dataOutputStream.writeUTF("please enter a valid response [ADD | CHAT]");
            response1 = dataInputStream.readUTF();
            validResponse = false;
        }

        dataOutputStream.writeUTF("repsonse one in function: " +response1);
        responseHandlerReturn.add(0, recipient);
        responseHandlerReturn.add(1, response1);
        responseHandlerReturn.add(2, validResponse);

        return responseHandlerReturn;

    }

    static class ClientHandler extends Thread {
        Scanner scanner = new Scanner(System.in);
        final DataInputStream dataInputStream;
        final DataOutputStream dataOutputStream;
        final Socket socket;
        boolean loggedin;
        private String name;
        private String recipient;
        //private int clientNum;
        //boolean nameSet = false;

        public ClientHandler(Socket s, String n, DataInputStream dis, DataOutputStream dos, String r) {
            this.socket = s;
            this.name = n;
            this.dataInputStream = dis;
            this.dataOutputStream = dos;
            this.loggedin = true;
            this.recipient = r;
        }

        @Override
        public void run() {
            String received;
            String toreturn;
            while (true) {
                try {
                    //receive string
                    received = dataInputStream.readUTF();

                    System.out.println(received);

                    if (received.equals("logout")) {
                        this.loggedin = false;
                        this.socket.close();
                        break;
                    }

                    String msgToSend = received;

                    //search for recipient in list
                    for (ClientHandler mc : Server.clients) {

                        //if recipient found, write on its output stream
                        if (mc.name.equals(recipient) && mc.loggedin) {
                            mc.dataOutputStream.writeUTF(this.name + ": " + msgToSend);
                            break;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                //closing resources
                this.dataInputStream.close();
                this.dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
