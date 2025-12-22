package io.falconFlow.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;

@Entity
@Table(name = "loan_applications")
@DynamicUpdate
public class LoanApplication {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String applicantName;

  private Double loanAmount;

  private String status; // e.g. PENDING, APPROVED, REJECTED

  private LocalDate applicationDate;

  private int bureauScore;
  private String bre1Status;
  private String bre2Status;
  private String bre3Status;

  // --- Constructors ---
  public LoanApplication() {}

  public LoanApplication(
      String applicantName, Double loanAmount, String status, LocalDate applicationDate) {
    this.applicantName = applicantName;
    this.loanAmount = loanAmount;
    this.status = status;
    this.applicationDate = applicationDate;
  }

  // --- Getters & Setters ---
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getApplicantName() {
    return applicantName;
  }

  public void setApplicantName(String applicantName) {
    this.applicantName = applicantName;
  }

  public Double getLoanAmount() {
    return loanAmount;
  }

  public void setLoanAmount(Double loanAmount) {
    this.loanAmount = loanAmount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDate getApplicationDate() {
    return applicationDate;
  }

  public void setApplicationDate(LocalDate applicationDate) {
    this.applicationDate = applicationDate;
  }

  public int getBureauScore() {
    return bureauScore;
  }

  public void setBureauScore(int bureauScore) {
    this.bureauScore = bureauScore;
  }

  public String getBre1Status() {
    return bre1Status;
  }

  public void setBre1Status(String bre1Status) {
    this.bre1Status = bre1Status;
  }

  public String getBre2Status() {
    return bre2Status;
  }

  public void setBre2Status(String bre2Status) {
    this.bre2Status = bre2Status;
  }

  public String getBre3Status() {
    return bre3Status;
  }

  public void setBre3Status(String bre3Status) {
    this.bre3Status = bre3Status;
  }
}
