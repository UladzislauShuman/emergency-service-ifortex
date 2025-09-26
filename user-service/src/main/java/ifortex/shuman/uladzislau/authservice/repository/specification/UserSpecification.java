package ifortex.shuman.uladzislau.authservice.repository.specification;

import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserSpecification {

  public Specification<User> hasFullName(String fullName) {
    return createSpecificationLike(fullName, "fullName");
  }

  public Specification<User> hasEmail(String email) {
    return createSpecificationLike(email, "email");
  }

  public Specification<User> hasPhone(String phone) {
    return (root, query, criteriaBuilder) ->
        phone == null ? criteriaBuilder.conjunction() :
            criteriaBuilder.like(root.get("phone"), "%" + phone + "%");
  }

  public Specification<User> hasStatuses(Set<UserStatus> statuses) {
    return (root, query, criteriaBuilder) -> {
      if (statuses == null || statuses.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return root.get("status").in(statuses);
    };
  }

  public Specification<User> hasRoles(Set<UserRole> roles) {
    return (root, query, criteriaBuilder) -> {
      if (roles == null || roles.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return root.get("role").get("name").in(roles);
    };
  }

  public Specification<User> fetchRoles() {
    return (root, query, criteriaBuilder) -> {
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        root.fetch("role", JoinType.LEFT);
        query.distinct(true);
      }
      return criteriaBuilder.conjunction();
    };
  }

  private Specification<User> createSpecificationLike(String criteria, String criteriaName) {
    return (root, query, criteriaBuilder) ->
        criteria == null ? criteriaBuilder.conjunction() :
            criteriaBuilder.like(criteriaBuilder
                .lower(root.get(criteriaName)), "%" + criteria.toLowerCase() + "%");
  }

  public Specification<User> isActive() {
    return (root, query, cb) -> cb.and(
        cb.equal(root.get("status"), UserStatus.ACTIVE),
        cb.equal(root.get("isPermanentlyBlocked"), false),
        cb.or(
            cb.isNull(root.get("blockedUntil")),
            cb.lessThan(root.get("blockedUntil"), Instant.now())
        )
    );
  }

  public Specification<User> isUnlocked() {
    return (root, query, cb) -> cb.and(
        cb.isFalse(root.get("isPermanentlyBlocked")),
        cb.or(
            cb.isNull(root.get("blockedUntil")),
            cb.lessThanOrEqualTo(root.get("blockedUntil"), Instant.now())
        )
    );
  }

  public Specification<User> isLocked() {
    return isPermanentlyBlocked().or(isTemporarilyBlocked());
  }

  private Specification<User> isPermanentlyBlocked() {
    return (root, query, cb) -> cb.isTrue(root.get("isPermanentlyBlocked"));
  }

  private Specification<User> isTemporarilyBlocked() {
    return (root, query, cb) -> cb.and(
        cb.isFalse(root.get("isPermanentlyBlocked")),
        cb.isNotNull(root.get("blockedUntil")),
        cb.greaterThan(root.get("blockedUntil"), Instant.now())
    );
  }

}
