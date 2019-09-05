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
        Vector<String> alreadychatting = new Vector<>();

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
                ClientHandler handler = new ClientHandler(s, name, dataInputStream, dataOutputStream, recipient , alreadychatting);

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

    static class ClientHandler extends Thread {
        Scanner scanner = new Scanner(System.in);
        final DataInputStream dataInputStream;
        final DataOutputStream dataOutputStream;
        final Socket socket;
        boolean loggedin;
        private String name;
        private String recipient;
        private int stepCount;
        Vector<String> alreadychatting = new Vector<>();
        int counter2 = 0;
        //private int clientNum;
        //boolean nameSet = false;

        public ClientHandler(Socket s, String n, DataInputStream dis, DataOutputStream dos, String r, Vector ac) {
            this.socket = s;
            this.name = n;
            this.dataInputStream = dis;
            this.dataOutputStream = dos;
            this.loggedin = true;
            this.recipient = r;
            this.alreadychatting = ac;
        }

        @Override
        public void run() {
            String received = "";
            String toreturn;
            while (!received.equals("LOGOUT")) {
                try {
                    stepCount++;
                    if(stepCount == 1){ //obtain username
                        name = dataInputStream.readUTF();
                        System.out.println("name recieved: " + name);

                        BufferedWriter writer1 = new BufferedWriter(new FileWriter("onlineUsers.txt", true));
                        writer1.write(name);
                        writer1.newLine();
                        writer1.flush();
                        writer1.close();
                    }
                    else if(stepCount == 2) { //obtain recipient and see if they are already chatting with someone
                        recipient = dataInputStream.readUTF();
                        System.out.println("recipient recieved: " + recipient);

                        //check who is chatting with each other already
                        //check if they are already chatting with someone or chose to already chat with you
                        for (int i = 0; i < alreadychatting.size(); i++) {
                            if ((alreadychatting.get(i).equals(name) && alreadychatting.get(i + 1).equals(recipient)) || (alreadychatting.get(i).equals(recipient) && alreadychatting.get(i + 1).equals(name))) {
                                counter2 += 2;
                            } else if (alreadychatting.get(i).equals(name) || alreadychatting.get(i).equals(recipient) || alreadychatting.get(i + 1).equals(name) || alreadychatting.get(i + 1).equals(recipient)) {
                                counter2++;
                            }
                            i += 2;
                        }

                        System.out.println("counter2: " +counter2);
                        dataOutputStream.writeUTF(Integer.toString(counter2));

                        if(counter2 == 2 || counter2 == 0){

                            //add them to the already chatting vector
                            if(counter2 == 0){
                                alreadychatting.add(name);
                                alreadychatting.add(recipient);
                            }

                            System.out.println("already chatting after: " +alreadychatting);
                        }
                    }
                    else if(stepCount == 3){ //give the notification to the recipient that someone wants to chat with them
                        System.out.println("i am in stepcount 3");
                        received = dataInputStream.readUTF();
                        System.out.println("recieved: " +received);

                        //search for recipient in list
                        for (ClientHandler mc : Server.clients) {
                            //if recipient found, write on its output stream
                            if (mc.name.equals(recipient) && mc.loggedin) {
                                mc.dataOutputStream.writeUTF(received);
                                break;
                            }
                        }

                        String friendresponse = dataInputStream.readUTF();
                        System.out.println("friend response: " +friendresponse);
                        if(friendresponse.equals("YES") || friendresponse.equals("NO")){
                            dataOutputStream.writeUTF(friendresponse);
                        }
                        else{
                            //search for recipient in list
                            for (ClientHandler mc : Server.clients) {

                                //if recipient found, write on its output stream
                                if (mc.name.equals(recipient) && mc.loggedin) {
                                    mc.dataOutputStream.writeUTF("Please enter a valid response [YES | NO]");
                                    break;
                                }
                            }
                        }

                    }
                    else{
                        //receive string
                        received = dataInputStream.readUTF();

                        //System.out.println(received);

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
                    System.out.println(e);
                }
            }

            try {
                //remove from online users txt file
                File onlineUsers = new File("onlineUsers.txt");
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

                //closing resources
                System.out.println("Closing connection");
                socket.close();
                this.dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
