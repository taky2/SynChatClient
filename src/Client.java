import java.net.*;
import java.io.*;
import java.util.*;

/***********************************************************************************
 * client designed to be run via console or with UI                                *
 ***********************************************************************************
 *** WARNING: Console mode does not work correctly PLEASE USE UI VERSION ONLY!!! ***
 ***********************************************************************************/

public class Client  {

    private ObjectInputStream socketInputStream; // to read from the socket
    private ObjectOutputStream socketOutputStream; // to write on the socket
    private Socket socket; // socket for I/O streams
    private int port; // port for socket
    private ClientUI ui; // reference to GUI class
    private String server;
    private String username;

    /*
     * client CLI is intended to be used with the following commands
     * > java Client
     * > java Client username
     * > java Client username portNumber
     * > java Client username portNumber serverAddress
     *
     * If portNumber is not specified, 8700 is set as the default
     * If serverAddress is not specified "localHost" is set as default
     * If username is not specified a random 4 digit number is appended to "User"
     *
     * In console mode, if an error occurs the program simply stops
     * when a GUI id used, the GUI is informed of the disconnection
     */
    public static void main(String[] args) {
        int portNumber = 8700;
        String serverAddress = "localhost";
        String userName = "User" + (int)(Math.random()*9000);

        // check number of arguments to decide how to parse
        switch(args.length) {
            case 3:     // username, portNumber, serverAddress specified: (> javac Client username portNumber serverAddress)
                serverAddress = args[2];
            case 2:     // username and portNumber specified: (> javac Client username portNumber)
                try {
                    portNumber = Integer.parseInt(args[1]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage: > java Client [username] [portNumber] [serverAddress]");
                    return;
                }
            case 1:     // username specified (> javac Client username)
                userName = args[0];

            case 0:     // defaults used: (> java Client)
                break;
            default:    // invalid number of arguments
                System.out.println("Usage: > java Client [username] [portNumber] [serverAddress]");
                return;
        }
        // create new client object
        Client client = new Client(serverAddress, portNumber, userName);
        // start connection to server?
        if(!client.start())
            return;
        // scanner waits for messages from client user
        Scanner scan = new Scanner(System.in);
        // wait indefinitely for messages from clint user
        while(true) {
            System.out.print("> ");
            String msg = scan.nextLine(); // read new message from user

            if(msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));    // handle request to logout
                break;  // disconnects
            }
            else if(msg.equalsIgnoreCase("USERLIST")) {
                client.sendMessage(new ChatMessage(ChatMessage.USERLIST, ""));  // handle request for list of active users
            }
            else if(msg.equalsIgnoreCase("HISTORY")) {
                client.sendMessage(new ChatMessage(ChatMessage.HISTORY, ""));   // handle request for chatlog history
            }
            else {
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));  // handle default message request
            }
            client.disconnect(); // disconnect
        }
    } // end main

    /**
     *  polymorphic console constructor for CLI (no GUI)
     *
     *  @param server: server address
     *  @param port: port number
     *  @param username: username
     **/
    Client(String server, int port, String username) {
        this(server, port, username, null); // GUI set to null when constructor called
    }

    /**
     * polymorphic display constructor for GUI
     *
     *  @param server: server address
     *  @param port: port number
     *  @param username: username
     *  @param ui: signals use of GUI interface
     **/
    Client(String server, int port, String username, ClientUI ui) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.ui = ui;
    }

    /*
     * To start the dialog
     */
    public boolean start() {
        // connect to server
        try {
            socket = new Socket(server, port);
        }
        catch(Exception e_connection) {
            display("Error connecting to server:" + e_connection);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort()
                + "\n------------------------------------------------------------------------";
        display(msg);

		// INITIALIZING: create data streams
        try
        {
            socketInputStream = new ObjectInputStream(socket.getInputStream());
            socketOutputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException e_IO) {
            display("Exception creating new I/O Streams: " + e_IO);
            return false;
        }

        new ListenerThread().start();        // create Thread to listen from server

        // INITIALIZING: send username to the server as string
        try
        {
            socketOutputStream.writeObject(username);
        }
        catch (IOException e_IO) {
            display("Exception doing login : " + e_IO);
            disconnect();
            return false;
        }
        return true; // SUCCESS
    } // end start()

    /** display message to user (console or GUI) **/
    private void display(String msg) {
        if(ui == null)
            System.out.println(msg);    // console
        else
            ui.append(msg + "\n");		// GUI
    }

    /** send a message to server from user **/
    void sendMessage(ChatMessage msg) {
        try {
            socketOutputStream.writeObject(msg);
        }
        catch(IOException e) {
            display("Error writing to server: " + e);
        }
    }

    /** disconnect : close I/O and disconnect (catch errors) **/
    private void disconnect() {
        try {
            if(socketInputStream != null) socketInputStream.close();
        }
        catch(Exception e) {}
        try {
            if(socketOutputStream != null) socketOutputStream.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}

        if(ui != null)
            ui.connectionFailed(); // inform GUI of disconnect

    }


    /** wait for message from the server, append to JTextArea (console or GUI) **/
    class ListenerThread extends Thread {
        public void run() {
            while(true) {
                try {
                    String msg = (String) socketInputStream.readObject();
                    // console mode
                    if(ui == null) {
                        System.out.println(msg);
                        System.out.print("> ");
                    }
                    // ui mode
                    else {
                        ui.append(msg);

                    }
                }
                catch(IOException e) {
                    display("Server has close the connection: " + e);
                    if(ui != null)
                        ui.connectionFailed();
                    break;
                }
                catch(ClassNotFoundException e2) { // required
                }
            }
        }
    }
}
