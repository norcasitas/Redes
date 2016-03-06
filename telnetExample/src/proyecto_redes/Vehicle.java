/*
 * Class that represents the Vehicle shared resource utilized by the peers.
 * 
 */
package proyecto_redes;

public class Vehicle {

    private static int reserved; //Amount of reserved seats.
    private final int SEATS = 10; //Total amount of seats in the vehicle.

    /**
     * Reserves a given amount of seats.
     *
     * @param seats THe amount of seats to reserve.
     * @return True if the amount of seats to reserve is greater or equal than
     * zero and the amount of available seats is greater or equal than the
     * amount of seats to reserve. Return false otherwise.
     */
    public boolean reserve(int seats) {
        if (seats >= 0 && SEATS - reserved >= seats) {
            reserved += seats;
            return true;
        }
        return false;

    }

    /**
     * Cancels a given amount of reserved seats.
     *
     * @param seats The amount of reserves to cancel.
     * @return True if the amount of reserves to cancel is greater or equal than
     * zero and the amount of reserved seats is greater or equeal than the
     * amount of reserves to cancel. Otherwise return false.
     */
    public boolean cancel(int seats) {
        if (seats >= 0 && reserved - seats >= 0) {
            reserved -= seats;
            return true;
        }
        return false;
    }

    /**
     * Returns the available seats. The available seats are those that are not
     * reserved.
     *
     * @return The amount of available seats.
     */
    public int available() {
        return SEATS - reserved;
    }

    /**
     * Constructor: Initializer the vehicle with no reserved seats.
     */
    public Vehicle() {
        reserved = 0;
    }

    /**
     * Sets the reserved seats of the vehicle.
     *
     * @param seats The amount of reserved seats.
     */
    public void setReservedSeats(int seats) {
        reserved = seats;
    }

    /**
     * Returns the reserved seats of the vehicle.
     *
     * @return The reserved seats of the vehicle.
     */
    public int getReservedSeats() {
        return reserved;
    }

} //End of vehicle class.
