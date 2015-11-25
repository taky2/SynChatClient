import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/********************************************************************************************************
 * Client-side GUI app connects to multithreaded server to allow multiple clients to cross-communicate  *
 ********************************************************************************************************/
public class ClientUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 7145714966993813033L;
    // will first hold "Username:", later on "Enter message"
    private JLabel label;
    // to hold the Username and later on the messages
    private JTextField textField;
    // to hold the server address an the port number
    private JTextField jtfServer, jtfPort;
    // to Logout and get the list of the users
    private JButton login, logout, userList, history;
    // for the chat room
    private JTextArea chatArea;
    // if it is for connection
    private boolean jtfActive;
    // the Client object
    private Client client;
    // the default port number
    private int defaultPort;
    private String defaultHost;


    /** MAIN: Launch UI and open connection to sever **/
    public static void main(String[] args) {
        new ClientUI("localhost", 8700);
    }


    /** Constructor creates a UI for client (accepts host & port numbers) **/
    ClientUI(String host, int port) {

        super("Chat Client");
        defaultPort = port;
        defaultHost = host;

        // define gridLayout for top of display
        JPanel topPanel = new JPanel(new GridLayout(2,1));
        // define the gridLayout for server and port display
        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
        // construct labels and text-fields with default values
        jtfServer = new JTextField(host);
        jtfPort = new JTextField("" + port);
        jtfPort.setHorizontalAlignment(SwingConstants.RIGHT);
        // add labels and text-fields to panel layout
        serverAndPort.add(new JLabel(" Server Address:  "));
        serverAndPort.add(jtfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(jtfPort);
        serverAndPort.add(new JLabel(""));
        // add server and port layout to top panel of UI
        topPanel.add(serverAndPort);
        add(topPanel, BorderLayout.NORTH);

        // create midPanel and add chatbox text area
        chatArea = new JTextArea("Welcome to the Chat room\n", 80, 45);
        JPanel midPanel = new JPanel(new GridLayout(1,1));
        midPanel.add(new JScrollPane(chatArea));
        chatArea.setEditable(false);
        add(midPanel, BorderLayout.CENTER);

        // add buttons for user to send and fetch data
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);		// must login before asking to logout
        userList = new JButton("Current Users");
        userList.addActionListener(this);
        userList.setEnabled(false);		// must login before asking for list of connected clients
        history = new JButton("History");
        history.addActionListener(this);
        history.setEnabled(false);      // must login before asking for chat history

        // create bottomPanel layout for buttons at bottom of display
        JPanel bottomPanel = new JPanel(new GridLayout(4,1));
        // add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(login);
        buttonPanel.add(logout);
        buttonPanel.add(userList);
        buttonPanel.add(history);
        bottomPanel.add(buttonPanel);

        // user input area & defaults
        label = new JLabel(" Enter your username below");
        bottomPanel.add(label, BorderLayout.LINE_START);
        textField = new JTextField("User" + (int)(Math.random()*9000));
        textField.setBackground(Color.WHITE);
        bottomPanel.add(textField);
        // add to bottom of display
        add(bottomPanel, BorderLayout.SOUTH);

        // specifications for GUI window
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        textField.requestFocus();

    } // end clientUI constructor

    /** called by client to update and maintain chat contents **/
    void append(String str) {
        chatArea.append(str);
        chatArea.setCaretPosition(chatArea.getText().length() - 1);
    }
    /** if connection failed, reset UI buttons, labels & textfields **/
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        userList.setEnabled(false);
        label.setText(" Enter your username below");
        textField.setText("User" + (int)(Math.random()*9000));
        jtfPort.setText("" + defaultPort);
        jtfServer.setText(defaultHost);
        jtfServer.setEditable(false);
        jtfPort.setEditable(false);
        history.setEnabled(false);
        textField.removeActionListener(this);
        jtfActive = false;
    }

    /** Handle JButton or JTextField click events **/
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if(obj == logout) {
            // logout request from user
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            return;
        }
        if(obj == userList) {
            // active user list requested
            client.sendMessage(new ChatMessage(ChatMessage.USERLIST, ""));
            return;
        }
        if(obj == history) {
            // chatlog history is
            client.sendMessage(new ChatMessage(ChatMessage.HISTORY, ""));
            return;
        }

        if(jtfActive) {
            // text field is active, send input from client
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, textField.getText()));
            textField.setText("");
            return;
        }

        // is connection request?
        if(obj == login) {
            String username = textField.getText().trim();   // ignore empty username
            if(username.length() == 0)
                return;
            String server = jtfServer.getText().trim();     // ignore empty serverAddress
            if(server.length() == 0)
                return;
            String portNumber = jtfPort.getText().trim();   // ignore empty or invalid port number
            if(portNumber.length() == 0)
                return;
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception ex) {
                return;   // invalid port num
            }

            // create new client connection with GUI
            client = new Client(server, port, username, this);
            if(!client.start())
                return;
            textField.setText("");
            // change prompt and activate textField
            label.setText(" Enter your message below");
            jtfActive = true;

            // change button and textField permissions
            login.setEnabled(false);
            logout.setEnabled(true);
            userList.setEnabled(true);
            history.setEnabled(true);
            jtfServer.setEditable(false);
            jtfPort.setEditable(false);
            textField.addActionListener(this);  // add actionListener to user text field
        }
    } // end actionPerformed method

} // end ClientUI class
