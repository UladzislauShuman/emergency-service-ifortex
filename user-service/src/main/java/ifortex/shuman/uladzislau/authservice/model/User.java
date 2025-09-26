package ifortex.shuman.uladzislau.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_google_id", columnList = "googleId", unique = true)
})
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"roles", "tokens"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(length = 100)
  private String fullName;

  @Column(length = 20)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status;

  @Column(nullable = false)
  private boolean is2FAEnabled = true;

  @Column(unique = true)
  private String googleId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private Set<UserToken> tokens;

  private boolean isPasswordTemporary;
  private Instant passwordExpiryTime;

  private Instant blockedUntil;

  @Column(nullable = false)
  private boolean isPermanentlyBlocked = false;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (role == null) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new SimpleGrantedAuthority(role.getName().name()));
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    boolean isTemporarilyLocked =
        this.blockedUntil != null && this.blockedUntil.isAfter(Instant.now());
    return !isPermanentlyBlocked && !isTemporarilyLocked
        && this.status != UserStatus.PASSWORD_RESET_PENDING;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    if (!this.isPasswordTemporary) {
      return true;
    }
    return this.passwordExpiryTime != null && this.passwordExpiryTime.isAfter(Instant.now());
  }

  @Override
  public boolean isEnabled() {
    return this.status != UserStatus.DELETED
        && this.status != UserStatus.PENDING_VERIFICATION
        && this.status != UserStatus.PENDING_DELETION;
  }
}