package com.novelle.novelle.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="users",uniqueConstraints=@UniqueConstraint(name="uk_users_email",columnNames="email")) @Getter @Setter
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="full_name",nullable=false,length=100) private String fullName;
  @Column(nullable=false,length=150) private String email;
  @Column(nullable=false) private String password;
  @Column(length=500) private String bio;
  @Column(nullable=false,length=10) private String role="USER";
  @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
}
