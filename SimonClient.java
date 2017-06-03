import javax.swing.*;
import javax.swing.border.LineBorder;
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
implements Runnable{

    private boolean continuePlay = true;
    private boolean waiting = true;
    private boolean myTurn = false;
    private JLabel jlblTitle = new JLabel();
    private JLabel jlblStatus = new JLabel();
    private int sequence = 4;
    private int rowSelected;
    private int columnSelected;
    private Cell[][] cell = new Cell[2][2];
    private Deque<boardColor> colorQueue;
    private DataInputStream fromServer;
    private DataOutputStream toServer;


    public void init(){
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

    private void connectToServer(){
        try{
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

    public void run(){
        try{
            //The sequence that the player must copy
            jlblTitle.setText("Player 1 copy the sequence of colors as they light up");
            readColors();
            /**Implementation for lighting the colors on the cells as they correspond to the
             * sequence*/

            myTurn = true;
            while(continuePlay){
                waitForPlayerAction();
            }
        }
        catch (Exception ex){
            System.out.println(ex);
        }
    }

    private void waitForPlayerAction() throws InterruptedException{
        while(waiting){
            Thread.sleep(100);
        }
        waiting = true;
    }

    private void readColors() throws IOException{
        int i = 0;
        while(i < sequence){
            switch (fromServer.readInt()){
                case 0:
                    colorQueue.add(boardColor.Red);     break;
                case 1:
                    colorQueue.add(boardColor.Yellow);  break;
                case 2:
                    colorQueue.add(boardColor.Blue);    break;
                case 3:
                    colorQueue.add(boardColor.Green);   break;
                default:
                    System.out.println("INVALID INPUT FROM SERVER!!"); sequence++;
            }
        }
    }

    public class Cell extends JPanel {
        // Indicate the row and column of this cell in the board
        private boardColor color;
        private int cellColor;
        private int row;
        private int column;

        // Token used for this cell

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            setCellColor();
            setBorder(new LineBorder(Color.black, 3)); // Set cell's border
            addMouseListener(new ClickListener());  // Register listener
        }

        @Override /** Paint the cell */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        }

        /** Handle mouse click on a cell */
        private class ClickListener extends MouseAdapter {
            public void mouseClicked(MouseEvent e) {
                //if the color does not match then player loses round
                if(color != colorQueue.removeFirst()){
                    myTurn = false;     return;
                }
                
                rowSelected = row;
                columnSelected = column;
            }
        }

        //Should only be called four times
        private void setCellColor(){
            color = boardColor.values()[cellColor++];

            switch (color){
                case Red:
                    setBackground(Color.RED);
                    break;
                case Yellow:
                    setBackground(Color.YELLOW);
                    break;
                case Blue:
                    setBackground(Color.BLUE);
                    break;
                case Green:
                    setBackground(Color.GREEN);
                    break;
            }
        }
    }
}
