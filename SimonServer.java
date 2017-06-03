import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.stream.Stream;

/**
 * Created by nubian on 5/30/17.
 */
public class SimonServer extends JFrame {
    public static void main(String [] args){SimonServer frame = new SimonServer();}
    public SimonServer(){
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
            jtaLog.append("Server started at " +serverSocket.getLocalPort()+", on "+ new Date() +"\n");

            while(true){

                jtaLog.append("Waiting for players to join session..."+sessionNo+"\n");

                //connect player 1
                Socket player1 = serverSocket.accept();

                jtaLog.append(new Date() + ": Player 1 joined session " +
                        sessionNo + '\n');
                jtaLog.append("Player 1's IP address" +
                        player1.getInetAddress().getHostAddress() + '\n');

                //Display this session and increment session no.
                jtaLog.append(new Date() + "Start a thread for session: "
                + sessionNo++ +"\n");

                //create a new thread for this session
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

class SimonHandler implements Runnable{
    private Socket P1;
    private int sequence = 4;
    private final boardColor[][] cell = new boardColor[2][2];
    private Deque<boardColor> deque;

    SimonHandler(Socket P1){
        this.P1 = P1;
        //initialize the cells
        fillCells();
    }

    /*Get both the input and output streams of the socket*/
    public void run(){
        try{
            DataInputStream player1In = new DataInputStream(
                    P1.getInputStream());
            DataOutputStream player1Out = new DataOutputStream(
                    P1.getOutputStream());

            //determine the game's status to the players
            while(true){
                sendColors(player1Out);
                /**Implement remaining Handler Class*/

            }
        }
        catch (IOException ex){
            System.out.println(ex);
        }
    }

    private void sendColors(DataOutputStream P1)throws IOException{
        Random rand = new Random();
        int i = 0;   while(i < sequence){
            P1.writeInt(rand.nextInt(boardColor.values().length));
            i++;
        }
    }

synchronized private void fillQueue(){
        //fill the queue with random colors
        for(int i = 0; i < sequence; i++){
            //deque.add(boardColor.randomColor());
            boardColors[i] = boardColor.randomColor();
        }
    }

    /*Sets the cells to some enum value boardColor*/
    private void fillCells(){
        int k = 0;
        for(int i = 0; i < 2; i++)
            for(int j = 0; j < 2; j++){
            cell[i][j] = boardColor.values()[k++];
            }
    }

    private boolean roundWin(Queue<boardColor> q){
        for (boardColor c: q) {
            while(q.iterator().hasNext() && deque.iterator().hasNext())
                if(q.iterator().next() != deque.iterator().next())
                    return false;
        }
        return true;
    }
}

//Enumeration for the colors the player chooses
enum boardColor {
    Red, Blue, Green, Yellow;
    private static final boardColor[] colors = boardColor.values();
    private static final int values = colors.length;
    private static final Random rand = new Random();

    //method for returning one enum value randomly
    public static boardColor randomColor(){
        return colors[rand.nextInt(values)];
    }

    public static boardColor getColor(int i){
        switch (i){
            case 0:
                return Red;     break;
            case 1:
                return Blue;    break;
            case 2:
                return Yellow;  break;
            case 3:
                return Green;   break;
        }
    }
}
