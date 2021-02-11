package domain.user;

import java.io.Serializable;

public interface User extends Serializable {

    /**
     * @return the name of the user
     */
    String getUsername();

    /**
     * @return the identification number of the user
     */
    int getId();
}
