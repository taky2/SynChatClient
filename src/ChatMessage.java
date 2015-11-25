import java.io.*;



/**
 * defines different types of messages exchanged, allowing client/server to exclusively pass objects
 **/

public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 7145714966993813033L;

    /* Types of message objects:
    *
    * USERLIST to receive the list of the users connected
    * MESSAGE an ordinary message
    * LOGOUT to disconnect from the Server
    * HISTORY to view chatlog history
    */
    static final int USERLIST = 0, MESSAGE = 1, LOGOUT = 2, HISTORY = 3;
    private int type;
    private String message;

    // mutator
    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    // accessor
    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
}
