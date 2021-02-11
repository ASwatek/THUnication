package domain.user;

public class Teacher implements User {

    private int id;
    private String username;

    public Teacher(String username, int id) {
        this.id = id;
        this.username = username;
    }

    /**
     * @return the name of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the identification number of the user
     */
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == null || getClass() != o.getClass())
            return false;

        if (o == this) {
            return true;
        }

        Teacher teacher = (Teacher) o;
        if (this.username.equals(teacher.username))
            return this.id == teacher.id;
        return false;
    }

    @Override
    public int hashCode() {
        return ((this.username.hashCode() * this.id) * 11) / 7;
    }
}
