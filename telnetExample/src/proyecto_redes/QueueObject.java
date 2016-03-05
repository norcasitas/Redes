/*
 * Class that represents an object of the priority queue of tasks.
 */
package proyecto_redes;

public class QueueObject {

    private final int time;
    private final long pid;

    /**
     * Constuctor: Receives the time and process id.
     *
     * @param time The time of the peer.
     * @param pid The process id of the peer that creates the object.
     */
    public QueueObject(int time, long pid) {
        this.time = time;
        this.pid = pid;
    }

    /**
     * Returns the time of the task
     *
     * @return The time of the task
     */
    public int getTime() {
        return time;
    }

    /**
     * Returns the pid of the process that created the task.
     *
     * @return The pid of the process that created the task.
     */
    public long getPid() {
        return pid;
    }

    @Override
    public String toString() {
        return "time: " + time + " - pid: " + pid;
    }

} //End of QueueObject class.
