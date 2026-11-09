package com.novelle.novelle.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="comments",indexes=@Index(name="idx_comments_post",columnList="post_id")) @Getter @Setter
public class Comment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false,length=1000) private String content;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="post_id",nullable=false) private Post post;
  @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
}
