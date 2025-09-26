package ifortex.shuman.uladzislau.authservice.paramedic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paramedic_applications")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParamedicApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String email;

  @Column(nullable = false, length = 100)
  private String fullName;

  @Column(nullable = false, length = 20)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ParamedicApplicationStatus status;

  private String identityDocumentPath;
  private String selfieWithDocumentPath;
  private String medicalCertificatePath;

  private String rejectionReason;
  private Instant submittedAt;
  private Instant reviewedAt;
}