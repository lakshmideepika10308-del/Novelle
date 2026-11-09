package com.novelle.novelle.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="posts",indexes={@Index(name="idx_posts_category",columnList="category"),@Index(name="idx_posts_created",columnList="created_at"),@Index(name="idx_posts_user",columnList="user_id")}) @Getter @Setter
public class Post {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false,length=200) private String title;
  @Column(nullable=false,length=500) private String description;
  @Column(nullable=false,columnDefinition="LONGTEXT") private String content;
  @Column(name="cover_image",length=1000) private String coverImage;
  @Column(nullable=false,length=60) private String category;
  @Column(nullable=false,length=20) private String status="PUBLISHED";
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User author;
  @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
  @Column(name="updated_at") private LocalDateTime updatedAt=LocalDateTime.now();
  @PreUpdate void touch(){ updatedAt=LocalDateTime.now(); }
}
