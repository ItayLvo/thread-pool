package waitingqueue;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WaitablePQueueTest {

    @Test
    void testEnqueueAndDequeue() {
        WaitablePQueue<Integer> wpq = new WaitablePQueue<>();
        wpq.enqueue(3);
        wpq.enqueue(1);
        wpq.enqueue(2);

        assertEquals(3, wpq.size());
        assertEquals(1, wpq.dequeue());
        assertEquals(2, wpq.dequeue());
        assertEquals(3, wpq.dequeue());
        assertEquals(0, wpq.size());
    }


    public static class Student {
        private String name;
        private float avgGrade;

        public Student(String name, float avgGrade) {
            this.name = name;
            this.avgGrade = avgGrade;
        }

        public String getName() {
            return name;
        }

        public float getAvgGrade() {
            return avgGrade;
        }
    }

    @Test
    void testEnqueueAndDequeueWithComparator() {
        Comparator<Student> studentComparator = new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return (int) (s1.getAvgGrade() - s2.getAvgGrade());
            }
        };
        WaitablePQueue<Student> wpq = new WaitablePQueue<>(studentComparator);
        wpq.enqueue(new Student("John", 50.5f));
        wpq.enqueue(new Student("Itay", 99.9f));
        wpq.enqueue(new Student("Bob", 75.3f));

        assertEquals(3, wpq.size());
        assertEquals("John", wpq.dequeue().getName());
        assertEquals("Bob", wpq.dequeue().getName());
        assertEquals("Itay", wpq.dequeue().getName());
    }

    @Test
    void remove() {
        WaitablePQueue<Integer> wpq = new WaitablePQueue<>();
        wpq.enqueue(3);
        wpq.enqueue(1);
        wpq.enqueue(2);
        wpq.remove(2);
        assertEquals(2, wpq.size());
        assertEquals(1, wpq.dequeue());
        assertEquals(3, wpq.dequeue());
    }

    @org.junit.jupiter.api.Test
    void peek() {
        WaitablePQueue<Integer> wpq = new WaitablePQueue<>();
        wpq.enqueue(3);
        assertEquals(3, wpq.peek());
        wpq.enqueue(1);
        assertEquals(1, wpq.peek());
    }

    @Test
    void timeoutDuringDequeue() {
        WaitablePQueue<Integer> wpq = new WaitablePQueue<>();
        wpq.enqueue(3);
        wpq.enqueue(1);
        Integer i = wpq.dequeue();
        Integer j = wpq.dequeue();
        Integer k = wpq.dequeue(3, TimeUnit.SECONDS);
        assertNull(k);
    }
}