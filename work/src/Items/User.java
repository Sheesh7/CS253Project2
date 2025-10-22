package Items;
import java.util.Objects;

public class User {
    protected String userID;
    protected String name;
    protected String email;
    protected int weight;

    // Constructor
    public User(String userID, String name, String email, int weight) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.weight = weight;
    }

    // getters
    public String getUserID() { return userID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getWeight() { return weight; }

    // setters
    public void setUserID(String userID) { this.userID = userID; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setWeight(int weight) { this.weight = weight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userID, user.userID) && Objects.equals(name, user.name)
                &&  Objects.equals(email, user.email) && weight == user.weight;
    }
}