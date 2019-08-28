import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException{

        try{
            Scanner scanner = new Scanner(System.in);

            //get localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            //establish connection with server port 5056
            Socket socket = new Socket(ip, 5056);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

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

}