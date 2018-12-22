package model;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "files")
public class File {

    @GeneratedValue
    @Id
    private UUID id = UUID.randomUUID();

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // отправитель

    public File(User user, String name, String path) {
        this.user = user;
        this.name = name;
        this.path = path;
    }

    public File() {}

    private String name; // имя файла

    private String path; // путь к файлу на сервере
}
