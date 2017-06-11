import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * Created by nubian on 5/30/17.
 */
public class SimonServer extends JFrame {
    public static void main(String [] args) {SimonServer frame = new SimonServer();}

    public SimonServer() {
        int sessionNo = 1;
        JTextArea jtaLog = new JTextArea();

        JScrollPane scrollPane = new JScrollPane(jtaLog);
        add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setTitle("Simon Server");
        setVisible(true);

        try{
            ServerSocket serverSocket = new ServerSocket(8000);
            jtaLog.append("Server started at " +serverSocket.getLocalPort()+", on "+ new Date() + "\n");

            while(true) {

                jtaLog.append("Waiting for player 1 to join session..."+sessionNo+"\n");

                //connect player 1
                Socket player1 = serverSocket.accept();

                jtaLog.append(new Date() + ": Player 1 joined session " +
                        sessionNo + '\n');
                jtaLog.append("Player 1's IP address" +
                        player1.getInetAddress().getHostAddress() + '\n');

                jtaLog.append("Waiting for player 2 to join session... " +
                sessionNo + "\n");

                //Will require a new instance of the SimonClient Class
/*                Socket player2 = serverSocket.accept();

                jtaLog.append("Player 2 has joined session " + sessionNo + '\n');
                jtaLog.append("Player2's  IP address " +
                        player2.getInetAddress().getHostAddress() + '\n');

                //Display this session and increment session no.
                jtaLog.append(new Date() + "Start a thread for session: "
                + sessionNo++ +"\n");

                //create a new thread for this session
                SimonHandler handler = new SimonHandler(player1, player2);
*/
                SimonHandler handler = new SimonHandler(player1);
                //start the thread
                new Thread(handler).start();
            }
        }
        catch(IOException ex){
            System.out.println(ex);
        }
    }
}

class SimonHandler implements Runnable , SimonConstants {

    private Socket P1;
    private Socket P2;
    private int sequence = 4;
    private Deque<boardColor> deque;


    SimonHandler(Socket P1){
        this.P1 = P1;
    }
    SimonHandler(Socket P1, Socket P2) {
        this.P1 = P1;
        this.P2 = P2;
        //initialize the cells
    }

    /*Get both the input and output streams of the socket*/
    public void run() {

        try {
            DataInputStream player1In = new DataInputStream(
                    P1.getInputStream());
            DataOutputStream player1Out = new DataOutputStream(
                    P1.getOutputStream());
           /* DataInputStream player2In = new DataInputStream(P2.getInputStream());
            DataOutputStream player2Out = new DataOutputStream(P2.getOutputStream());
            */

            //INFORMS THE PLAYER WHO THEY ARE
            player1Out.writeInt(PLAYER1);
            //player2Out.writeInt(PLAYER2);

            while(true) {
                sendColors(player1Out);
                //sendColors(player2Out);

                //notifies if the player either won or lost
                int statusP1 = player1In.readInt();
                //int statusP2 = player2In.readInt();

                if(statusP1 == PLAYER1_LOST) {
                    player1Out.writeInt(1);
                }

                else if (statusP1 == PLAYER1_WON){
                    player1Out.writeInt(0);
                    player1Out.writeInt(0);
                    sequence++;
                }
            }

            //determine the game's status to the players
            /*while(true) {
                sendColors(player1Out);
                //sendColors(player2Out);

                //notifies if the player either won or lost
                int statusP1 = player1In.readInt();
                //int statusP2 = player2In.readInt();

                if(statusP1 == PLAYER1_LOST && statusP2 == PLAYER2_LOST) {
                    player1Out.writeInt(1);
                    player2Out.writeInt(1);
                }

                else if(statusP1 == PLAYER1_WON && statusP2 == PLAYER2_LOST) {
                    player1Out.writeInt(1);
                    player2Out.writeInt(0);
                    break;
                }

                else if(statusP2 == PLAYER2_WON && statusP1 == PLAYER1_LOST) {
                    player1Out.writeInt(0);
                    player2Out.writeInt(1);
                    break;
                }

                else if (statusP1 == PLAYER1_WON && statusP2 == PLAYER2_WON){
                    player1Out.writeInt(0);
                    player1Out.writeInt(0);
                    sequence++;
                }
            }*/
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public void sendColors(DataOutputStream P1) throws IOException {
        System.out.println("Writing");
        int[] colors = {0,1,2,3};
        Random rand = new Random(System.currentTimeMillis());

        try {
            for(int i = 0; i < sequence; i++) {
                int sendInt =rand.nextInt(colors.length);
                System.out.println(sendInt);
                P1.writeInt(sendInt);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//Enumeration for the colors the player chooses
enum boardColor {
    Red, Blue, Green, Yellow;
    private static final boardColor[] colors = boardColor.values();
    private static final int values = colors.length;
}
