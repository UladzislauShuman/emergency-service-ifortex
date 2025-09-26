package ifortex.shuman.uladzislau.billing.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "plans")
@Data
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String planCode;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChronoUnit intervalUnit;

    @Column(nullable = false)
    private int intervalCount;
}