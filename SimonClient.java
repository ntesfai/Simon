import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Created by nubian on 6/1/17.
 */
public class SimonClient extends JApplet
implements Runnable, SimonConstants {

    private boolean continuePlay = true;
    private boolean waiting = true;
    private boolean myTurn = false;
    private JLabel jlblTitle = new JLabel();
    private JLabel jlblStatus = new JLabel();
    private int sequence = 4;
    private int serverStatus;
    private Cell[][] cell = new Cell[2][2];
    private Deque<boardColor> colorQueue;
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Simon Client");

        SimonClient client = new SimonClient();
        client.getContentPane().add(frame, BorderLayout.CENTER);

        client.init();
        client.start();

        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void init() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2, 0, 0));
        for(int i = 0; i < 2; i++)
            for(int j = 0; j < 2; j++)
                p.add(cell[i][j] = new Cell(i, j));

        //No boarders
        p.setBorder(new LineBorder(Color.black, 0));
        jlblTitle.setHorizontalAlignment(JLabel.CENTER);
        jlblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        jlblTitle.setBorder(new LineBorder(Color.black, 1));
        jlblStatus.setBorder(new LineBorder(Color.black, 1));

        // Place the panel and the labels to the applet
        add(jlblTitle, BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
        add(jlblStatus, BorderLayout.SOUTH);

        // Connect to the server
        connectToServer();
    }

    /**Sets up the server objects*/
    private void connectToServer() {
        try {
        Socket sock = new Socket(getCodeBase().getHost(), 8000);
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
        try{
            //The sequence that the player must copy
            jlblTitle.setText("Player, copy the sequence of colors as they light up");
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
        }
        catch (Exception ex){
            System.out.println(ex);
        }
    }

    /**The game conditions for losing if a player wins a round, if both win and both lose
     * or if only one wins*/
    private void gameStatusToServer() throws IOException {
        serverStatus = fromServer.readInt();
        if (!myTurn && serverStatus == PLAYER1) {
            toServer.writeInt(PLAYER1_LOST);
            jlblStatus.setText("Incorrect entry");

        } else if (!myTurn && serverStatus == PLAYER2) {
            toServer.writeInt(PLAYER2_LOST);
            jlblStatus.setText("Incorrect entry");
        }

        //Player got the sequence correct
        else if (serverStatus == PLAYER1) {
            toServer.writeInt(PLAYER1_WON);
            sequence++;
        }
    }

    /**Waits for the Player to make a move*/
    private void waitForPlayerAction() throws InterruptedException{
        while(waiting){
            Thread.sleep(100);
        }
        waiting = true;
    }

    /**Fills the colorQueue according to the integers from the Server*/
    private void readColorsFromServer() throws IOException {
        int i = 0;
        while(i < sequence){
            switch (fromServer.readInt()){
                case 0:
                    colorQueue.add(boardColor.Red);     break;
                case 1:
                    colorQueue.add(boardColor.Blue);    break;
                case 2:
                    colorQueue.add(boardColor.Yellow);  break;
                case 3:
                    colorQueue.add(boardColor.Green);   break;
                default:
                    System.out.println("INVALID INPUT FROM SERVER!!"); i++;
            }
        }
    }

    /**Class that is added to the GridLayout*/
    public class Cell extends JPanel {
        // Indicate the row and column of this cell in the board
        private boardColor color;
        private int cellColor;
        private int row;
        private int column;

        // Token used for this cell
        public Cell(){}

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            setCellColor();
            setBorder(new LineBorder(Color.black, 3)); // Set cell's border
            addMouseListener(new ClickListener());  // Register listener
        }

        /** Handle mouse click on a cell
         * calls the brighten cell method*/
        private class ClickListener extends MouseAdapter{
            public void mouseClicked(MouseEvent e) {
                //if the color does not match then player loses round
                if(color != colorQueue.removeFirst()){
                    myTurn = false;     return;
                }
                brightenCell();
                if(!colorQueue.isEmpty()){
                    try {
                        waitForPlayerAction();
                    } catch (InterruptedException e1) {
                        System.out.println(e1);;
                    }
                }
            }
        }

        /**Sets the specific cell's color*/
        private void setCellColor() {
            int cellColor = 0;
            color = boardColor.values()[cellColor++];
            switch (color){
                case Red:
                    setBackground(Color.RED);
                    break;
                case Yellow:
                    setBackground(Color.BLUE);
                    break;
                case Blue:
                    setBackground(Color.YELLOW);
                    break;
                case Green:
                    setBackground(Color.GREEN);
                    break;
            }
        }
        private String getCellColor(){
            return this.color.toString();
        }

        public String toString() {
            switch (color){
                case Red:
                    return "Red";
                case Yellow:
                    return "Yellow";
                case Blue:
                    return "Blue";
                case Green:
                    return "Green";
                default:
                    return "Cell has invalid color";
            }
        }

        /**Displays the sequence that must be copied by the player exactly*/
        private void displayColorPattern() {
            Iterator iter = colorQueue.iterator();
            while(iter.hasNext()) {
                long start = System.currentTimeMillis();
                setBackground(Color.getColor(iter.next().toString()).brighter());
                long end = System.currentTimeMillis();
                for(int i = 0; i > -1; i++){
                    if(end - start > 550){
                        //maybe use the .darker() method???
                        setBackground(Color.getColor(getCellColor().toString()).darker());
                        break;
                    }
                    else
                        end = System.currentTimeMillis();
                }
            }
        }

        /**Brightens the cell momentarily so that when clicked on it lights up*/
        private void brightenCell() {
            long start = System.currentTimeMillis();
            setBackground(Color.getColor(getCellColor().toString()).brighter());
            long end = System.currentTimeMillis();
            for(int i = 0; i > -1; i++) {
                if(end - start > 450) {
                setBackground(Color.getColor(getCellColor().toString()).darker());
                break;
                }
                else
                    end = System.currentTimeMillis();
            }
        }

    }
}
