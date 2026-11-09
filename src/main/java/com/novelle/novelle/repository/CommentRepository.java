package com.novelle.novelle.repository;
import com.novelle.novelle.entity.Comment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface CommentRepository extends JpaRepository<Comment,Long>{
  List<Comment> findByPostIdOrderByCreatedAtDesc(Long id); long countByPostId(Long id);
  void deleteByPostId(Long id); void deleteByUserId(Long id);
  List<Comment> findTop100ByOrderByCreatedAtDesc(); List<Comment> findTop5ByOrderByCreatedAtDesc();
}
