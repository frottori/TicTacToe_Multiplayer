import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

// red color = 0xff615f
// blue color = 0x3ec5f3
// assigned 
// -1 : none
// player = 0 : O
// player = 1 : X

public class TicTacToe implements ActionListener {

    private int  port = 1761;
    private String serverAddress = "localhost";
    private Socket sock;
    private JButton[][] board;
    private int[][] assigned;
    private JFrame frame;
    private ImageIcon xIcon,oIcon;
    private int player;
    private JLabel winner;
    int whoWon = -1;
    private Timer timer;
    boolean flag;

    private int currentPlayer;

    private void initConnection()
    {
        try{
            sock = new Socket(serverAddress,port);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            currentPlayer = Integer.parseInt(in.readLine());
            player = Integer.parseInt(in.readLine());
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void checkTurn()
    {
        if (currentPlayer == 0) {
            winner.setText("O Plays...");
            winner.setForeground(new Color(0x3ec5f3));
        } else {
            winner.setText("X Plays...");
            winner.setForeground(new Color(0xff615f));
        }
    }

    public TicTacToe(){

        initConnection();

        winner = new JLabel("");
        winner.setFont(new Font("Fish Grill", Font.PLAIN, 42));
        winner.setVerticalAlignment(SwingConstants.CENTER);
        
        checkTurn();

        assigned = new int[3][3];

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        board = new JButton[3][3];
        xIcon = new ImageIcon(getClass().getResource("icons/x.png"));
        oIcon = new ImageIcon(getClass().getResource("icons/o.png"));

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++ ) {
                assigned[i][j] = -1;
                board[i][j] = new JButton();
                board[i][j].setPreferredSize(new Dimension(110,100));
                board[i][j].addActionListener(this);
                board[i][j].setBackground(Color.WHITE);
                board[i][j].setFocusable(false);
                frame.add(board[i][j]);
            }
        }
        
        frame.add(winner);
        
        ImageIcon customIcon = new ImageIcon(getClass().getResource("icons/icon.png"));
        frame.setIconImage(customIcon.getImage());
        frame.setTitle("TicTacToe");
        frame.setPreferredSize(new Dimension(380,410));
        frame.pack();
        frame.getContentPane().setBackground(Color.LIGHT_GRAY);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);

        timer = new Timer(500, e -> updateBoardIcons()); // Set up a Timer to fire every 1000 milliseconds (1 second)
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if (e.getSource() == board[i][j]){
                    if(player == 0 && assigned[i][j] == -1) {
                        board[i][j].setIcon(oIcon);
                        assigned[i][j] = 0;
                        setAssigned(0, i, j); //set assigned for o 
                    }
                    else if(player == 1 && assigned[i][j] == -1) {
                        board[i][j].setIcon(xIcon);
                        assigned[i][j]=1;
                        setAssigned(1,i,j);
                    } 
                } 
            }
        }
         
    }

    private void closeSocket() {
        try {
            if (sock != null && !sock.isClosed()) {
                PrintWriter pr = new PrintWriter(sock.getOutputStream());
                pr.println(Integer.toString(0));
                sock.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public static boolean equals3(int a,int b,int c){
        if(a == b && b == c & a != -1)
            return true;
        else 
            return false;
    }

    private void setWinner(JButton b1,JButton b2,JButton b3,int color){
        if(color == 0){ // O WON
            b1.setBackground(new Color(0x3ec5f3));
            b2.setBackground(new Color(0x3ec5f3));
            b3.setBackground(new Color(0x3ec5f3));
        }
        else{
            b1.setBackground(new Color(0xff615f));
            b2.setBackground(new Color(0xff615f));
            b3.setBackground(new Color(0xff615f));
        }

    }

    public void setAssigned(int val,int i,int j){
        try{
            PrintWriter pr = new PrintWriter (sock.getOutputStream());
            pr.println(Integer.toString(1));
            pr.println(Integer.toString(val));
            pr.println(Integer.toString(i));
            pr.println(Integer.toString(j));
            pr.println(Integer.toString(player));

            pr.flush();

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void updateBoardIcons() {
      
        try{
            if(checkWinner())
                return;
            if(sock== null || sock.isClosed())
                return;
            flag = true;
            PrintWriter pr = new PrintWriter (sock.getOutputStream());
            pr.println(Integer.toString(2));
            pr.flush();
                
            ObjectInputStream oiStream = new ObjectInputStream(sock.getInputStream());
            Object recvObject = oiStream.readObject();

            if(recvObject instanceof int[][]){
                this.assigned = (int[][]) recvObject;
                // System.out.println("received array from server");   
                   for (int k=0;k<3;k++){
                        for(int l=0;l<3;l++ ){
                            // System.out.print(this.assigned[k][l]);
                            if(assigned[k][l] == 0){
                                board[k][l].setIcon(oIcon);
                            }
                            if(assigned[k][l] == 1){
                                board[k][l].setIcon(xIcon);
                            }
                        }
                        // System.out.println();
                    }
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            int turn = Integer.parseInt(in.readLine());
            // System.out.println("Player that plays: " + turn);

            currentPlayer = turn;

            if (currentPlayer == 0) {
                winner.setText("O Plays...");
                winner.setForeground(new Color(0x3ec5f3));
            } else {
                winner.setText("X Plays...");
                winner.setForeground(new Color(0xff615f));
            }

            for (int k = 0; k < 3; k++) {
                for (int l = 0; l < 3; l++) {
                    board[k][l].setEnabled(turn == player);
                }
            }    

        }
        catch(IOException | ClassNotFoundException e ){
            e.printStackTrace();
        }
    }

    private boolean checkWinner(){
        // horizontal check
        for (int i=0;i<3;i++){
            if(equals3(assigned[i][0],assigned[i][1],assigned[i][2])){
                whoWon = assigned[i][0]; 
                setWinner(board[i][0],board[i][1],board[i][2],whoWon);
            }
        } 
        // vertical check
        for (int i=0;i<3;i++){
                if(equals3(assigned[0][i],assigned[1][i],assigned[2][i])){
                    whoWon = assigned[0][i];
                    setWinner(board[0][i],board[1][i],board[2][i],whoWon);
                }
        }  
        // 1st diagonal check
        if(equals3(assigned[0][0],assigned[1][1],assigned[2][2])){
                    whoWon = assigned[0][0];
                    setWinner(board[0][0],board[1][1],board[2][2],whoWon);

        }
        // 2nd diagonal check
        if(equals3(assigned[0][2],assigned[1][1],assigned[2][0])){
                    whoWon = assigned[0][2];
                    setWinner(board[0][2],board[1][1],board[2][0],whoWon);
        }

        if (whoWon == 1) { // X WON
            winner.setText("X Won!");
            winner.setForeground(new Color(0xff615f));
            buttonsDisabled();
            closeSocket();
            return true;
        } else if (whoWon == 0) { // O WON
            winner.setText("O Wins!");
            winner.setForeground(new Color(0x3ec5f3));
            buttonsDisabled();
            closeSocket();
            return true;
        }
        for (int k = 0; k < 3; k++) {
                for (int l = 0; l < 3; l++) {
                    Icon icon = board[l][k].getIcon();
                    if (icon == null) {
                        flag = false;
                    }
                }
            }   
        if (flag == true) { //TIE
                winner.setText("Tie!");
                winner.setForeground(Color.BLACK);
                buttonsDisabled();
                closeSocket();
                return true;
        }
        return false;

    }

    private void buttonsDisabled(){
          for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    board[i][j].setEnabled(false);  
                }
            }
    }

    public static void main(String[] args) {
        new TicTacToe();
    }
}