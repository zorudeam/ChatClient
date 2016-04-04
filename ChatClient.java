/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class ChatClient extends Application implements Runnable {
    
    static Thread t;
    boolean continueFlag;
    String chatArg;
    static String a;
    static String p;
    static private Socket socket;
    static ObjectInputStream objectIn;
    static ObjectOutputStream objectOut;
    static TextArea text;
    static Label state;
    boolean stopOrder;
    TextField tf;
    double blurScale = 0;
    BoxBlur bb = new BoxBlur(blurScale, blurScale, 1);
    String ip;
    String port;
    boolean blurring;
    boolean doubleClicked, clickedFlag;
    int pooto;
    
    //GUI
    @Override
    public void start(Stage primaryStage) {
        
        StackPane root = new StackPane();
        
        blurring = false;
        stopOrder = false;
        state = new Label("Please specify the address and port");
        state.setEffect(bb);
        Button btn = new Button();
        text = new TextArea();
        text.setEditable(false);
        tf = new TextField("");
        btn.setText("Send");
        tf.setEditable(true);
        System.out.println("Hola ");
        text.setWrapText(true);
        btn.setOnAction(new EventHandler<ActionEvent>() {
        
            @Override
            public void handle(ActionEvent event) {
                
                
                if(state.getText().equals("Connected")){
                
                    sendMessage(tf.getText());
                    tf.setText("");
                    handleCommand(tf.getText());
                    
                }
                
                else{
                    handleCommand(tf.getText());
                    tf.setText("");
                    
                    Thread e = new Thread(() -> { 
                    
                    if(!blurring){
                        
                        blurring = true;
                        for(int i=0; i<=9; i++){
                            try {
                                BoxBlur bb = new BoxBlur(blurScale, blurScale, 1);
                                blurScale=blurScale+1.0;
                                Thread.sleep(30);

                                state.setEffect(bb);
                            } catch (InterruptedException ex) {System.out.println("ERROR");}
                            System.out.println(""+blurScale); 
                        }

                        for(int i=9; i>=0; i--){
                            try {
                                BoxBlur bb = new BoxBlur(blurScale, blurScale, 1);
                                blurScale=blurScale-1.0;

                                Thread.sleep(30);
                                state.setEffect(bb);
                            } catch (InterruptedException ex) {System.out.println("ERROR");}
                            System.out.println(""+blurScale); 
                        }
                        blurring = false;
                    }
                    
                    });
                    e.start();
                }
            }
            
        });
        
        Button connect = new Button("Connect");
        
        //THIS IS THE SAME AS SEND BUT FOR ENTER
        tf.setOnKeyPressed(new EventHandler<KeyEvent>(){
        
            public void handle(KeyEvent e){
            if(e.getCode()==KeyCode.ENTER){
                if(state.getText().equals("Connected")){
                    sendMessage(tf.getText());
                }
                System.out.println("checking text for commands");
                handleCommand(tf.getText());
                tf.setText("");
            }
            }
        });
        
        //THIS IS THE CONNECT BUTTON. THIS CODE IS NOT USED.
        connect.setOnAction(new EventHandler<ActionEvent>(){
        public void handle(ActionEvent e){
            if(//state.getText().equals("Connecting...") || 
                    state.getText().equals("Connected") || 
                    state.getText().equals("Please specify the address and port")){
                stopOrder=true;
                ip = null;
                port = null;
                state.setText("Cancelled. Re-enter address:port.");
                connect.setText("Connect");
            }
            else{
                state.setText("Connecting...");
                connect.setText("Cancel");
            }
        }
        });
        
        StackPane.setAlignment(state, Pos.TOP_LEFT);
        StackPane.setAlignment(tf, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(btn, Pos.BOTTOM_RIGHT);
        
        StackPane.setMargin(state, new Insets(3,10,10,10));
        StackPane.setMargin(text, new Insets(25,10,60,10));
        StackPane.setMargin(tf, new Insets(20,10,20,10));
        StackPane.setMargin(btn, new Insets(20,10,20,10));
        primaryStage.setMinHeight(150);
        primaryStage.setMinWidth(200);
       // s.setContent(text);
        root.getChildren().addAll(state,
                text,tf,btn
                );
                
        
        Scene scene = new Scene(root, 700, 400);
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent e){
                   primaryStage.setTitle(" Bye bye :) ");
                   stopOrder = true;
                   System.out.println("serversciket closed");
                    System.exit(0);
                }
        });
        primaryStage.setTitle("Instamess");
        primaryStage.setScene(scene);
        
        primaryStage.show();
        
        
        showMessage("Welcome to Instamess chat. For starting a connection state the address and port of the server with the following commands:"
                + "\naddress()"
                + "\nport()"
                + "\nconnect()"
                + "\nYou should write the information between the parenthesis of address and port with no extra spaces. "
                + "You can also write help() for more information."
                );
        
         doubleClicked = false;
         clickedFlag = false;
         
        text.setOnMouseClicked(new EventHandler<MouseEvent>(){
        public void handle(MouseEvent e){
        Thread t = new Thread(()->{
            
            try {
                clickedFlag=true;
                Thread.sleep(500);
            } catch (InterruptedException ex) {}
            clickedFlag=false;

        });
        t.start();
        if(clickedFlag){
            text.requestFocus();
            text.selectEnd();
            text.deselect();
        }
        else{
            tf.requestFocus();
            tf.selectEnd();}
        }
        });
        
        
    }
    
    //states the logic thread and GUI thread
    public static void main(String[] args){
        ChatClient client = new ChatClient();
        
        byte[] b = new byte[] {127,0,0,1};
        t = new Thread(client);
        
        launch(args);
    }
    
    //starts the client chat logic
    public void run(){
        
    this.trigger();
    }
    
    //main function of the chat logic, executed by thread t
    public void trigger(){
        
            
            try {
                System.out.println("trigger");
            attemptConnection();
            
                System.out.println("attempted connection");
            setStreams();
            
                System.out.println("set ap strims");
            continueFlag = true;
            Platform.runLater(() -> state.setText("Connected"));
            
                System.out.println("going to receiv");
            receiving();
            
            }catch (UnknownHostException ex) {showMessage("Unknown host! Host may not exist!");
            state.setText("Uh-oh, I couldn't determine the host address");}
            catch (IOException streamse) {showMessage("Streams could not be set up.");
            state.setText("Could not find streams for connection");}
            catch (NullPointerException NPE) {showMessage("\nThe port may be wrong.");}
            
    }
    
    private void attemptConnection() throws UnknownHostException {
        System.out.println(""+a+p);
        
        InetAddress inetaddress = InetAddress.getByName(a);
        
        pooto = Integer.parseInt(p,10);
        try {
            socket = new Socket(inetaddress,pooto);
        } catch (IOException ex) {showMessage("Could not connect!");}
    }
    
    private void setStreams() throws IOException {
        System.out.println("im the strim ster");
        try{
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            
            objectOut.flush();
            
            objectIn = new ObjectInputStream(socket.getInputStream());
            System.out.println("settne ap");
        }catch(NullPointerException npe){
            //If the port is wrong the program will loop attemptConnection()
            //and this method until NullPointerExceptions are not thrown
            //anymore (when the user enters a valid port)
            trigger();}
    }
    
    private void receiving() {
        
        showMessage("\nYou are now connected to " + socket.getInetAddress().getHostName() + "!");
        
        do{
            try {
                String receivedm = (String) objectIn.readObject();
                showMessage("\n" + receivedm);
            } catch (IOException ex) {showMessage("Could not read object! Ending connection with" +
                    socket.getInetAddress().getHostName() + "!");
                    state.setText("Connection ended. Probably sudden disconnection.");} 
            catch (ClassNotFoundException ex) {
               System.out.println("It is not a String!");
               showMessage("It is not a String. Error: Is someone trying to hack this?");
            }
        }while(continueFlag);
        System.out.println("stopped receiving!");
        
            state.setText("Connection ended. connect() to reconnect");
        
    }
    
    private void sendMessage(String m){
        try {System.out.println("SENDING " + m);
            objectOut.writeObject(m);
            showMessage("\n"+m);
            objectOut.flush();
            System.out.println("sent");
        } catch (IOException ex) {showMessage("Error. Could not send message.");}
    }
    
    private static void showMessage(String m){
        
        Platform.runLater(()->{
        System.out.println("showing in run later:  " + m);
        text.appendText("\n"+m);
        });
        
        System.out.println("SHOWN");
        
    }
    
    private void handleCommand(String cm){
        System.out.println("handling cm");
        
        if("help()".equals(cm)){
            System.out.println("es help!");
            showMessage("\nWelcome to help section. If you typed this you may be confused of addresses and ports. "
                    + "\n" + "address(127.0.0.1)"
                    + "\nIs the same as \naddress(localhost)\n"+
                    "Which means that the server is in your computer. You can write the IP or host name.\n" +
                    "Then, you have to write the port. For example:"
                    + "\nport(8080)"+
                    "\nThat information has to be requested to the owner of the computer you want to use as server."
                    +"\nAfter writing the address and port you should write connect() for starting the conversation with the server."
                    +" You can write anything you want between the parenthesis of connect() (oops, this feature is for the next version!) in case the server supports it. For example, a password."
                    + "\nUse kill() for ending connection. clean() for cleaning off the messages."
                    );
            
        }else{System.out.println("handling command, its not help");};
        
        //if cm matches address(X)
        if(Pattern.matches("address\\(.+\\)", cm))
        {
             a = new String(); 
                    for(int i=8; i<cm.length()-1; i++){
                    a= a + cm.charAt(i)+"";
                    }
                    
            try {
                System.out.println(InetAddress.getByName(a));
            } catch (UnknownHostException ex) {System.out.println("exception in address name");}
            
            showMessage("\nNew address: " + a);
        }
        
        //If cm matches port()
        if(Pattern.matches("port\\([0-9]+\\)", cm))
        {
             p = new String(); 
                    for(int i=5; i<cm.length()-1; i++){
                    p= p + cm.charAt(i)+"";
                    }
                    
            showMessage("\nNew port: " + p);
        }
        
        if(Pattern.matches("connect\\(\\)",cm))
        {
            chatArg = new String();
                for(int i=8; i<cm.length()-1; i++){
                    chatArg = chatArg + cm.charAt(i);
                }
                if(a.equals("null") || p.equals("null")){
                    showMessage("\nFirst declare the address and port.");
                    state.setText("Declare the addres and port. Write help() if you need help");
                }
                else{
            showMessage("\nEstablishing conection with address " + a + " on port " + p + "...");
                    continueFlag = false; //kill other current threads
                    //THIS IS THE THREAD OF ALL THE CONNECTION LOGIC. 
                    
                    ChatClient.t.start();
                    
                }
                System.out.println(chatArg);
        }
        
        if(Pattern.matches("kill\\(\\)", cm)){
            continueFlag = false;
            showMessage("\nConnection was killed.");
        }
        
        if(Pattern.matches("clean\\(\\)",cm)){
            text.setText("");
        }
        
    }
}