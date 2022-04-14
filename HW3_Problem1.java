import java.util.*;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicInteger;

class HW3_Problem1 {
    // here is an implementation of a lock-free linked list from
    // the textbook; I have named the class ExtremelyBasedList
    static class ExtremelyBasedList {
        class Node {
            long key;
            AtomicMarkableReference<Node> next;
            Node(long key){
                this.key = key;
            }
        }
        class Window {
            Node pred, curr;
            Window(Node myPred, Node myCurr) {
                pred = myPred;
                curr = myCurr;
            }
        }
        public Window find(Node head, long key) {
            Node pred = null, curr = null, succ = null;
            boolean[] marked = {false};
            boolean snip;
            retry: while (true) {
                pred = head;
                curr = pred.next.getReference();
                while (true) {
                    succ = curr.next.get(marked);
                    while (marked[0]) {
                        snip = pred.next.compareAndSet(curr, succ, false, false);
                        if (!snip){
                            continue retry;
                        }
                        curr = succ;
                        succ = curr.next.get(marked);
                    }
                    if (curr.key >= key){
                        return new Window(pred, curr);
                    }
                    pred = curr;
                    curr = succ;
                }
            }
        }

        private Node head;
        public ExtremelyBasedList() {
            head = new Node(Long.MIN_VALUE);
            head.next = new AtomicMarkableReference(new Node(Long.MAX_VALUE), false);
        }
        public boolean add(long key) {
            while (true) {
                Window window = find(head, key);
                Node pred = window.pred, curr = window.curr;
                if (curr.key == key) {
                    return false;
                } else {
                    Node node = new Node(key);
                    node.next = new AtomicMarkableReference(curr, false);
                    if (pred.next.compareAndSet(curr, node, false, false)) {
                        return true;
                    }
                }
            }
        }
        public boolean remove(long key) {
            boolean snip;
            while (true) {
                Window window = find(head, key);
                Node pred = window.pred, curr = window.curr;
                if (curr.key != key) {
                    return false;
                } else {
                    Node succ = curr.next.getReference();
                    snip = curr.next.compareAndSet(succ, succ, false, true);
                    if (!snip){
                        continue;
                    }
                    pred.next.compareAndSet(curr, succ, false, false);
                    return true;
                }
            }
        }
        public boolean contains(long key) {
            boolean[] marked = {false};
            Node curr = head;
            while (curr.key < key) {
                curr = curr.next.getReference();
                Node succ = curr.next.get(marked);
            }
            return (curr.key == key && !marked[0]);
        }
    }

    static final int NUM_SERVANTS = 4;
    static final int NUM_PRESENTS = 500_000;

    static ExtremelyBasedList presentList;
    // we will store the number of presents in the linked list as an atomic integer
    static AtomicInteger numPresentsInBag, numPresentsInList;

    static class Servant extends Thread {
        public void run(){
            boolean addingPresent = true;
            while(numPresentsInBag.get() > 0 || numPresentsInList.get() > 0){
                if(addingPresent){
                    if(numPresentsInBag.get() > 0){
                        // -1 to the presents in the bag
                        numPresentsInBag.decrementAndGet();
                        // +1 to the presents in the list
                        numPresentsInList.incrementAndGet();

                        // add a present from the unordered bag into the list
                        long curr = (long)(Math.random() * Long.MAX_VALUE);

                        presentList.add(curr);
                    }
                }
                else{
                    if(numPresentsInList.get() > 0){
                        // -1 to the presents in the bag
                        numPresentsInBag.decrementAndGet();

                        // remove one of the things from the list... but I'm not sure
                        // how to choose which one?
                        presentList.remove(presentList.head.key);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        presentList = new ExtremelyBasedList();
        numPresentsInBag = new AtomicInteger(0);
        numPresentsInList = new AtomicInteger(0);

        // simulate our servants
        Thread[] threads = new Thread[NUM_SERVANTS];
        for(int i = 0; i < NUM_SERVANTS; ++i){
            threads[i] = new Servant();
            threads[i].start();
        }

        for(int i = 0; i < NUM_SERVANTS; ++i){
            threads[i].join();
        }
    }
}