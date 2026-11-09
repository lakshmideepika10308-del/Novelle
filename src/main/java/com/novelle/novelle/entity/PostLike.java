package com.novelle.novelle.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="likes",uniqueConstraints=@UniqueConstraint(name="uk_like_user_post",columnNames={"user_id","post_id"}),indexes=@Index(name="idx_likes_post",columnList="post_id")) @Getter @Setter
public class PostLike {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="post_id",nullable=false) private Post post;
  @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
}
