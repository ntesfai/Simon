import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Created by nubian on 6/1/17.
 */
public class SimonClient extends JApplet
implements Runnable, SimonConstants {

    private String host = "localhost";

    private boolean continuePlay = true;
    private boolean isStandAlone = false;
    private boolean waiting = true;
    private boolean myTurn = false;

    private JFrame frame;
    private JLabel jlblTitle = new JLabel();
    private JLabel jlblStatus = new JLabel();
    private JButton button;

    private int cellColor = 0;
    private int sequence = 4;
    private int playerIs;

    private Cell[][] cell = new Cell[2][2];

    private Deque<boardColor> colorQueue = new LinkedList<boardColor>();

    private DataInputStream fromServer;
    private DataOutputStream toServer;


    public static void main(String[] args) {
        JFrame frame = new JFrame("Simon Client");

        SimonClient client = new SimonClient();
        /**
        client.isStandAlone = true;
        frame.add(client, BorderLayout.CENTER);

        client.init();

        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
         */
    }

    public SimonClient() {
        frame = new JFrame("Simon Client");
        frame.add(this, BorderLayout.CENTER);
        this.isStandAlone = true;
        frame.setSize(500,500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        init();
    }

    public void init() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2, 0, 0));
        for(int i = 0; i < 2; i++)
            for(int j = 0; j < 2; j++) {
                p.add(cell[i][j] = new Cell(i, j));
        }

        //No boarders
        p.setBorder(new LineBorder(Color.black, 1));
        jlblTitle.setHorizontalAlignment(JLabel.CENTER);
        jlblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        jlblTitle.setBorder(new LineBorder(Color.black, 1));
        jlblStatus.setBorder(new LineBorder(Color.black, 1));

        // Place the panel and the labels to the applet
        add(jlblTitle, BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
        add(jlblStatus, BorderLayout.SOUTH);
        p.setVisible(true);

        // Connect to the server
        connectToServer();
    }

    /**Sets up the server objects*/
    private void connectToServer() {
        try {
            Socket sock;
        if(isStandAlone)
            sock = new Socket(host, 8000);
        else
            sock = new Socket(getCodeBase().getHost(), 8000);

        fromServer = new DataInputStream(sock.getInputStream());
        toServer = new DataOutputStream(sock.getOutputStream());
        }
        catch (IOException ex){
            System.out.println(ex);
        }
        Thread thread = new Thread(this);
        thread.start();

    }

    public void run() {
        try {
            playerIs = fromServer.readInt();
            //The sequence that the player must copy
            jlblTitle.setText("Player, copy the sequence of colors exactly");
            /**Implementation for lighting the colors on the cells as they correspond to the
             * sequence*/
            myTurn = true;
            while(continuePlay) {
                readColorsFromServer();
                Cell cell = new Cell();
                cell.displayColorPattern();
                waitForPlayerAction();
                gameStatusToServer();
            }
            if(!continuePlay)
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**Slows the pace of the game*/
    private void pause(long length) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        for(int i =0; i> -1; i++){
            if(end - start > length)
                break;
            else
                end = System.currentTimeMillis();
        }
    }


    /**Gets data from server indicating which player won the game
     * and if a player wants to keep playing*/
    private void gameStatusToServer() throws IOException {

        if (!myTurn && playerIs == PLAYER1) {
            waiting = false;
            toServer.writeInt(PLAYER1_LOST);
            jlblStatus.setText("Incorrect entry Player 1");
            colorQueue.clear();
            if(fromServer.readInt() == 0) {
                jlblStatus.setText("Player 2 wins!");
                continuePlay = false;
            }
            else
                myTurn = true;
                jlblStatus.setText("Its a draw!");

        } else if (!myTurn && playerIs == PLAYER2) {
            waiting = false;
            toServer.writeInt(PLAYER2_LOST);
            jlblStatus.setText("Incorrect entry Player 2");
            colorQueue.clear();
            if (fromServer.readInt() == 0) {
                jlblStatus.setText("Player 1 wins!");
                continuePlay = false;
            }
            else
                myTurn = true;
                jlblStatus.setText("Its a draw!");
        }

        else if (playerIs == PLAYER1) {
            toServer.writeInt(PLAYER1_WON);
            if (fromServer.readInt() == 1)
                jlblStatus.setText("Player 2 lost, you win!");
            else {
                jlblStatus.setText("Good Job! It gets harder!");
                colorQueue.clear();
                sequence++;
                pause(2000);
                jlblStatus.setText("");
            }
        }

        else if (playerIs == PLAYER2) {
            toServer.writeInt(PLAYER2_WON);
            if (fromServer.readInt() == 1)
                jlblStatus.setText("Player 1 lost, you Win!");
            else {
                jlblStatus.setText("Good Job! It gets harder!");
                colorQueue.clear();
                sequence++;
                pause(2000);
                jlblStatus.setText("");
            }
        }
    }

    /**Waits for the Player to make a move*/
    private void waitForPlayerAction() throws InterruptedException, IOException {
        while(waiting) {
            Thread.sleep(100);
            if(!myTurn || colorQueue.isEmpty() || !continuePlay)
                waiting = false;
                //gameStatusToServer();
        }
        waiting = true;
    }

    /**Fills the colorQueue according to the integers from the Server*/
    private void readColorsFromServer() throws IOException {
        System.out.println("Reading");
        for(int i = 0; i < sequence; i++) {
            int in = fromServer.readInt();
            System.out.println(in);
            switch (in){
                case 0:
                    colorQueue.add(boardColor.Red);     break;
                case 1:
                    colorQueue.add(boardColor.Blue);    break;
                case 2:
                    colorQueue.add(boardColor.Yellow);  break;
                case 3:
                    colorQueue.add(boardColor.Green);   break;
                default:
                    jlblStatus.setText("INVALID INPUT FROM SERVER"); i++;
            }
        }
        System.out.println(colorQueue);
    }

    /**Class that is added to the GridLayout*/
    private class Cell extends JPanel {
        // Indicate the row and column of this cell in the board
        private boardColor color;
        private int row;
        private int column;

        // Token used for this cell
        public Cell(){}

        public Cell(int row, int column) {
            int delay = 2000;
            this.row = row;
            this.column = column;
            setCellColor();
            setBorder(new LineBorder(Color.black, 6)); // Set cell's border
            addMouseListener(new ClickListener());  // Register listener
        }


        /** Handle mouse click on a cell
         * calls the brighten cell method*/
        private class ClickListener extends MouseAdapter {
            public void mouseClicked(MouseEvent e) {
               //if the color does not match then player loses round
                System.out.println(colorQueue);
                if(color != colorQueue.removeFirst()) {
                    myTurn = false;
                }
            }

            public void mouseEntered(MouseEvent e) {
                setBackground(getBackground().brighter());
            }
            public void mouseExited(MouseEvent e) {
                setBackground(getBackground().darker());
            }
        }

        /**Sets the specific cell's color*/
        private void setCellColor() {
            color = boardColor.values()[cellColor++];
            switch (color) {
                case Red:
                    setBackground(Color.RED.darker());
                    break;
                case Yellow:
                    setBackground(Color.YELLOW.darker());
                    break;
                case Blue:
                    setBackground(Color.BLUE.darker());
                    break;
                case Green:
                    setBackground(Color.GREEN.darker());
                    break;
            }
        }

        /**Displays the sequence that must be copied by the player exactly*/
        private void displayColorPattern() {
            System.out.println("Displaying");
            Deque<boardColor> deque = new LinkedList<>();
            while(!colorQueue.isEmpty()) {
                boardColor bc = colorQueue.removeFirst();
                for(int i = 0; i < 2; i++)
                    for(int j = 0; j < 2; j++) {

                    if(cell[i][j].color == bc) {
                        Color temp = cell[i][j].getBackground();
                        cell[i][j].setBackground(temp.brighter());
                        pause(400);
                        cell[i][j].setBackground(temp);
                        pause(600);
                        deque.add(bc);
                    }
                }
            }
            refillQueue(deque);
        }

        /**Adds the values to the Queue again using a stack*/
        private void refillQueue(Deque deque) {
            System.out.println("Refilling Queue");
            while (!deque.isEmpty()) {
                colorQueue.addLast((boardColor) deque.removeFirst());
            }
        }
    }
}