package network;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegisterDataTest {
    @Test
    public void registerDataTest() {
        RegisterData data = new RegisterData("username", "password", "myRole");
        assertEquals("myRole", data.getRole());
    }
}
