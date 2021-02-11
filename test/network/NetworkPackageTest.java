package network;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NetworkPackageTest {

    @Test
    public void testPackageWithAdditionalDataConstructor() {
        NetworkPackage networkPackage = new NetworkPackage(NetworkPackage.Type.REGISTER, new RegisterData("username", "password", "role"), "testError", 1234);

        assertEquals("testError", networkPackage.getErrorMessage());
        assertEquals(1234, networkPackage.getAdditionalData());
    }

    @Test
    public void testPackageWithoutError() {
        NetworkPackage networkPackage = new NetworkPackage(NetworkPackage.Type.LOGIN, new LoginCredentials("testUser", "testPassword"));

        assertFalse(networkPackage.hasValidationError());
        assertEquals("", networkPackage.getErrorMessage());
        assertEquals(NetworkPackage.Type.LOGIN, networkPackage.getType());
    }

    @Test
    public void testPackageWithError() {
        NetworkPackage networkPackage = new NetworkPackage(
                NetworkPackage.Type.REGISTER,
                new LoginCredentials("testUser", "testPassword"),
                "test error message"
        );

        assertTrue(networkPackage.hasValidationError());
        assertEquals("test error message", networkPackage.getErrorMessage());
    }

    @Test
    public void testPackageTypes() {
        NetworkPackage networkPackage1 = new NetworkPackage(NetworkPackage.Type.REGISTER, null);
        NetworkPackage networkPackage2 = new NetworkPackage(NetworkPackage.Type.LOGIN, null);
        NetworkPackage networkPackage3 = new NetworkPackage(NetworkPackage.Type.MESSAGE, null);
        NetworkPackage networkPackage4 = new NetworkPackage(NetworkPackage.Type.MESSAGES, null);
        NetworkPackage networkPackage5 = new NetworkPackage(NetworkPackage.Type.CONVERSATIONS, null);
        NetworkPackage networkPackage6 = new NetworkPackage(NetworkPackage.Type.USERS, null);
        NetworkPackage networkPackage7 = new NetworkPackage(NetworkPackage.Type.PERMISSIONS, null);

        assertEquals(NetworkPackage.Type.REGISTER, networkPackage1.getType());
        assertEquals(NetworkPackage.Type.LOGIN, networkPackage2.getType());
        assertEquals(NetworkPackage.Type.MESSAGE, networkPackage3.getType());
        assertEquals(NetworkPackage.Type.MESSAGES, networkPackage4.getType());
        assertEquals(NetworkPackage.Type.CONVERSATIONS, networkPackage5.getType());
        assertEquals(NetworkPackage.Type.USERS, networkPackage6.getType());
        assertEquals(NetworkPackage.Type.PERMISSIONS, networkPackage7.getType());
    }

    @Test
    public void testLoginPackage() {
        NetworkPackage networkPackage = new NetworkPackage(
                NetworkPackage.Type.REGISTER,
                new LoginCredentials("testUser", "testPassword")
        );

        LoginCredentials credentials = (LoginCredentials) networkPackage.getContent();
        assertEquals("testUser", credentials.getUsername());
        assertEquals("testPassword", credentials.getPassword());
    }

    @Test
    public void testPermissionPackage() {
        Set<Integer> userIds = new HashSet<>();
        userIds.add(5);
        userIds.add(15);

        NetworkPackage networkPackage = new NetworkPackage(
                NetworkPackage.Type.PERMISSIONS,
                new Permissions(userIds, 7)
        );

        Permissions permissions = (Permissions) networkPackage.getContent();
        assertEquals(7, permissions.getConversationID());

        for (Integer userId : permissions.getUserIds()) {
            assertEquals(true, userIds.contains(userId));
        }
    }

    @Test
    public void testAdditionalData() {
        NetworkPackage networkPackage = new NetworkPackage(
                NetworkPackage.Type.CONVERSATIONS,
                null
        );
        networkPackage.setAdditionalData(12);

        assertEquals(12, networkPackage.getAdditionalData());
    }

    @Test
    public void sourceIdTest() {
        NetworkPackage networkPackage = new NetworkPackage(
                NetworkPackage.Type.CONVERSATIONS,
                null
        );
        networkPackage.setSourceId(1234);
        assertEquals(1234, networkPackage.getSourceId());
    }
}
