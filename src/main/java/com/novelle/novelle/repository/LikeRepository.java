package com.novelle.novelle.repository;
import com.novelle.novelle.entity.PostLike; import org.springframework.data.jpa.repository.JpaRepository;
public interface LikeRepository extends JpaRepository<PostLike,Long>{
  boolean existsByUserIdAndPostId(Long u,Long p); void deleteByUserIdAndPostId(Long u,Long p); long countByPostId(Long id);
  void deleteByPostId(Long id); void deleteByUserId(Long id);
}
