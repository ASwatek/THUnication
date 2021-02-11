package domain.user;

public class Student implements User {

    private int id;
    private String username;

    public Student(String username, int id) {
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

        if (o == null || getClass() != o.getClass())
            return false;

        if (o == this) {
            return true;
        }

        Student student = (Student) o;
        if (this.username.equals(student.username))
            return this.id == student.id;
        return false;
    }

    @Override
    public int hashCode() {
        return ((this.username.hashCode() * this.id) * 11) / 7;
    }
}
