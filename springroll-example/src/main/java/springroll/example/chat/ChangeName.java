package springroll.example.chat;

import java.io.Serializable;

public class ChangeName implements Serializable {

    String newName;

    public ChangeName(String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

}
