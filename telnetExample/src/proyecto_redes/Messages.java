/*
 * Class that holds all the possible messages used by the peer.
 */
package proyecto_redes;

public final class Messages {

    public static final int MSGAVAILABLE = 1; //Check available seats
    public static final int MSGRESERVE = 2; //Reserve an amount of seats
    public static final int MSGCANCEL = 3; //Cancel an amount of seats
    public static final int MSGACK = 4; //Acknowledge an incoming request
    public static final int MSGENTER = 5; //Notify other peers about this peer's intention to use the shared resource
    public static final int MSGRELEASE = 6; //Notify other peers that this peer finished using the shared resource
    public static final int MSGNEWCONECTION = 7; //Notify other peers of a new connection
    public static final int MSGACKNEWCONECTION = 8; //Acknowlegde a new connection
} //End of Messages class.
