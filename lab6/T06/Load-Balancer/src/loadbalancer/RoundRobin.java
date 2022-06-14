/**
 *
 * @author Jigar Borad
 *
 */
package loadbalancer;

public class RoundRobin {
    private int min;
    private int max;
    private int nextIdx = 0;

    public RoundRobin(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * there are four server defined in our program and all of them are waiting in
     * the queue.
     * for example, queue = [6002,6003,6004,6001].
     *
     * @return the next available server port number waiting in the queue
     */
    public int next() {
        int currentIdx = nextIdx;
        nextIdx = (nextIdx + 1) % (max - min + 1);
        return min + currentIdx;
    }

    public static void main(String[] args) {
        RoundRobin RR = new RoundRobin(6001, 6004);
        System.out.println(RR.next());
        System.out.println(RR.next());
        System.out.println(RR.next());
        System.out.println(RR.next());
        System.out.println(RR.next());
        System.out.println(RR.next());
    }
}
