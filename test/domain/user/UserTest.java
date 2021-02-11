package domain.user;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

    User teacher;
    User student;

    @Before
    public void init(){
        teacher = new Teacher("teacher",1);
        student = new Student("student",2);
    }

    @Test
    public void getUsername() {
        assertEquals("teacher", teacher.getUsername());
        assertEquals("student", student.getUsername());
    }

    @Test
    public void getId() {
        assertEquals(1, teacher.getId());
        assertEquals(2,student.getId());
    }

    @Test
    public void equalsTest(){
        assertEquals(true, teacher.equals(teacher));
        assertEquals(false, teacher.equals(student));
        assertEquals(false, teacher.equals(null));
        assertEquals(false, teacher.equals(new Teacher("teacher2",1)));
        assertEquals(true, teacher.equals(new Teacher("teacher",1)));
        assertEquals(true, student.equals(student));
        assertEquals(false, student.equals(teacher));
        assertEquals(false, student.equals(null));
        assertEquals(false, teacher.equals(new Student("student",5)));
        assertEquals(true, student.equals(new Student("student",2)));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(teacher.hashCode(), teacher.hashCode());
        assertEquals(student.hashCode(), student.hashCode());
        assertNotEquals(teacher.hashCode(), student.hashCode());
    }
}