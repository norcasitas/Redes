/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;


public class QueueObject {
    private long time;
   private long pid;

   /**
    * recibe el timestamp y el pid del proceso para identificarlo
    * @param time
    * @param pid 
    */
    public QueueObject(long time, long pid) {
        this.time = time;
        this.pid = pid;
    }

    public long getTime() {
        return time;
    }

    public long getPid() {
        return pid;
    }
    
    
}


