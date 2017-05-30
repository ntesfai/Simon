import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.Date;
import java.io.*;

/**
 * Created by nubian on 5/30/17.
 */
public class SimonServer extends JFrame {
    public static void main(String [] args){SimonServer frame = new SimonServer();}

    public SimonServer(){

        JTextArea jtaLog = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(jtaLog);
        add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setTitle("Simon Server");
        setVisible(true);

        try{
            ServerSocket serverSocket = new ServerSocket(8000);
            jtaLog.append("Server started at " + new Date() +"\n");

            //number of sessions
            int sessionNo = 1;

            while(true){

                jtaLog.append("Waiting for players to join session..."+sessionNo+"\n");

                //connect player 1
                Socket player1 = serverSocket.accept();

                jtaLog.append(new Date() + ": Player 1 joined session " +
                        sessionNo + '\n');
                jtaLog.append("Player 1's IP address" +
                        player1.getInetAddress().getHostAddress() + '\n');

                // Notify that the player is Player 1
                new DataOutputStream(
                        player1.getOutputStream()).writeInt(1);

                new Thread().start();
            }
        }
        catch(IOException ex){
            System.out.println(ex);
        }
    }
}

class SimonHandler{
    private Socket P1;

    public enum colors{Blue, Yellow, Red, Green}
}
