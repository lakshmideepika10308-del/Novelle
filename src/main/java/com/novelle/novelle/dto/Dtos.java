package com.novelle.novelle.dto;
import jakarta.validation.constraints.*; import java.time.LocalDateTime;
public class Dtos {
  public record RegisterReq(@NotBlank @Size(max=100) String fullName,@NotBlank @Email @Size(max=150) String email,@NotBlank @Size(min=8,max=72,message="must be 8-72 characters") String password,@NotBlank String confirmPassword){}
  public record LoginReq(@NotBlank String email,@NotBlank String password){}
  public record PostReq(@NotBlank @Size(max=200) String title,@NotBlank @Size(max=500) String description,@NotBlank String content,@Size(max=1000) String coverImage,@NotBlank String category){}
  public record CommentReq(@NotBlank @Size(max=1000) String content){}
  public record ProfileReq(@Size(max=500) String bio,@Size(max=100) String fullName){}
  public record UserDto(Long id,String fullName,String email,String bio,String role,LocalDateTime createdAt,long postCount){}
  public record PostDto(Long id,String title,String description,String content,String coverImage,String category,String status,Long authorId,String authorName,LocalDateTime createdAt,int readingTime,long likes,boolean liked,long comments){}
  public record CommentDto(Long id,String content,Long userId,String userName,Long postId,LocalDateTime createdAt){}
}
