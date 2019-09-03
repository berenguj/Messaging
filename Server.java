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
            String received = "";
            String toreturn;
            while (!received.equals("LOGOUT")) {
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
