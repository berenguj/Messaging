import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class Server {

    private String name;
    private String recipient;

    public Server(String n, String r){
        name = n;
        recipient = r;
    }

    //vector to store active clients
    static Vector<ClientHandler> clients = new Vector<>();


    public static void main(String args[]) throws IOException {

        String name = null;
        String recipient = null;

        //server listening on port 5056
        ServerSocket serverSocket = new ServerSocket(5056);

        //run infinite loop to get client request
        while (true) {
            Socket s = null;

            try {
                //socket object to receive incoming client request
                s = serverSocket.accept();

                Client client = new Client();
                System.out.println("A new client is connected: " + s);

                //obtain input and output streams
                DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());

                //obtain name and recipient


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
        private int stepCount;
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
                    stepCount++;
                    if(stepCount == 1){
                        name = dataInputStream.readUTF();
                        System.out.println("name recieved: " + name);
                    }
                    else if(stepCount == 2){
                        recipient = dataInputStream.readUTF();
                        System.out.println("recipient recieved: " +recipient);
                    }
                    else{
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
