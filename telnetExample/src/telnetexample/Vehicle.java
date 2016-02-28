/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;

public class Vehicle {

    private static int reserved;
    private final int SEATS = 10;

    public boolean reserve(int seats) {
        if (SEATS - reserved >= seats) {
            reserved += seats;
            return true;
        } else {
            return false;
        }
    }

    public boolean cancel(int seats) {
        if (reserved - seats > 0) {
            reserved = -seats;
            return true;
        } else {
            return false;
        }
    }

    public int available() {
        return SEATS - reserved;
    }

    public Vehicle() {
        reserved = 0;
    }
    
    public void setSeats(int seats){
        reserved= seats;
    }
}
