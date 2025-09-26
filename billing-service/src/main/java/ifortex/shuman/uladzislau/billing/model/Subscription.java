package ifortex.shuman.uladzislau.billing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private Instant startsAt;
    @Column(nullable = false)
    private Instant endsAt;
    private Instant canceledAt;
}