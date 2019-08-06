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
        boolean validResponse = false;
        Object validResponseObject;
        Object recipientObject;
        Object response1Object;
        String response1; //String addChat;
        String response2; //String secondAddChat;
        String[] chatReturn;
        Vector<Object> responseHandlerReturn = new Vector<>();

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
                dataOutputStream.writeUTF("If you don't have an account yet, please type in a username you'd like. To log in please type in your existing username: ");
                String name = dataInputStream.readUTF();

                //check if they are an existing user
                BufferedReader reader = new BufferedReader(new FileReader("users.txt"));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (name.equals(line)) {
                        existingUser = true;
                    } else {
                        existingUser = false;
                    }
                }

                //login an existing user
                if (existingUser) {
                    dataOutputStream.writeUTF("Welcome back! Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");
                }

                //add the new user to the user list and log them in
                else if (!existingUser) {
                    PrintWriter writer = new PrintWriter(new FileWriter("users.txt", true));
                    writer.write("\n");
                    writer.write(name);
                    writer.flush();
                    writer.close();
                    dataOutputStream.writeUTF("You are now signed up! Would you like to add friends to your friends list or chat with a current friend? [ADD | CHAT]");
                }

                //based on response from user, ask if they would like to add a friend or chat with someone
                //consider all possible cases like adding then chatting, or invalid responses, etc
                String recipient = null;
                response1 = dataInputStream.readUTF();
                //responseHandlerReturn = server.ResponseHandler(dataOutputStream, dataInputStream, name, server, response1);
                while (!validResponse) {

                    responseHandlerReturn = server.ResponseHandler(dataOutputStream, dataInputStream, name, server, response1);
                    validResponseObject = responseHandlerReturn.get(2);
                    validResponse = (boolean) validResponseObject;
                    recipientObject = responseHandlerReturn.get(0);
                    recipient = (String) recipientObject;
                    response1Object = responseHandlerReturn.get(1);
                    response1 = (String) response1Object;

                    System.out.println("validResponse: " + validResponse);
                    System.out.println("response1: " + response1);
                    System.out.println("recipient: " + recipient);
                    /*if (response1.equals("ADD")) {
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
                        chatReturn = server.CHAT(dataOutputStream, dataInputStream, name);
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
                        //validResponse = true;
                    } else {
                        dataOutputStream.writeUTF("please enter a valid response [ADD | CHAT]");
                        response1 = dataInputStream.readUTF();
                        validResponse = false;
                    }*/
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
        dataOutputStream.writeUTF("Please type the friend you would like to add");
        String friend = dataInputStream.readUTF();
        PrintWriter writer = new PrintWriter(new FileWriter(name + ".txt", true));
        writer.write("\n");
        writer.write(friend);
        writer.flush();
        writer.close();

        dataOutputStream.writeUTF("Okay! " + friend + " was added! Would you like to add another friend or start chatting with current friends? [ADD | CHAT]");
        String response1 = dataInputStream.readUTF();

        return response1;
    }

    public String[] CHAT(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name) throws IOException {
        String[] array = new String[2];
        String recipient; //0 in array
        String response1 = "CHAT"; //1 in array
        String filename = name + ".txt";

        System.out.println("name: " + name);
        System.out.println("filename: " + name + ".txt");
        BufferedReader reader1 = new BufferedReader(new FileReader(filename));
        String line = null;
        dataOutputStream.writeUTF("Here is your friends list: \n");
        while ((line = reader1.readLine()) != null) {
            dataOutputStream.writeUTF(line);
        }
        dataOutputStream.writeUTF("Who would you like to chat with?");
        recipient = dataInputStream.readUTF();
        System.out.println(recipient);
        int counter = 0;
        BufferedReader reader2 = new BufferedReader(new FileReader(filename));
        line = reader2.readLine();

        while (line != null) {
            line = line.replaceAll("\\s+", "");
            if (recipient.equals(line)) { //friend is in the list
                counter++;
            }
            line = reader2.readLine();
        }
        System.out.println(counter);
        if (counter == 0) { //counter didn't increase
            dataOutputStream.writeUTF("Sorry you're not friends with them! Did you want to add them or chat with a different friend? [ADD | CHAT]");
            response1 = dataInputStream.readUTF();
            if (response1.equals("CHAT")) {
                response1 = "CHATDISP"; //CHATDISP: display friends list and ask who they want to chat with
            }
        } else {
            response1 = "CHAT";
            dataOutputStream.writeUTF("Okay! Go ahead and start sending messages to " + recipient);
        }
        array[0] = recipient;
        array[1] = response1;

        return array;
    }

    public Vector ResponseHandler(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String name, Server server, String response1) throws IOException {

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
            chatReturn = server.CHAT(dataOutputStream, dataInputStream, name);
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
