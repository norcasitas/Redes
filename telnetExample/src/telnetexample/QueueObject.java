/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetexample;


public class QueueObject {
    private int time;
   private long pid;

   /**
    * recibe el timestamp y el pid del proceso para identificarlo
    * @param time
    * @param pid 
    */
    public QueueObject(int time, long pid) {
        this.time = time;
        this.pid = pid;
    }

    public int getTime() {
        return time;
    }

    public long getPid() {
        return pid;
    }
    
    @Override
    public String toString(){
        return "time: "+ time+" - pid: "+ pid;
    }
    
}


