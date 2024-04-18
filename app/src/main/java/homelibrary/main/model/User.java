package homelibrary.main.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "users")
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "email_unique", columnNames = "email")
})
@Data
public class User {
    @Id
    @SequenceGenerator(
            name = "users_sequence",
            sequenceName = "users_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "users_sequence"
    )
    @Column(name = "id",
            updatable = false,
            columnDefinition = "serial"
    )
    private  Long id;
    @Column(
            name = "name",
            nullable = false,
            columnDefinition = "varchar(32)"
    )
    private  String name;
    @Column(
            name = "email",
            nullable = false,
            columnDefinition = "varchar(128)"
    )
    private  String email;
    @Column(
            name = "password",
            nullable = false,
            columnDefinition = "varchar(128)"
    )
    private  String password;

    public User(
            String name,
            String email,
            String password
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User() {
        this.name = "";
        this.email = "";
        this.password = "";
    }
}
