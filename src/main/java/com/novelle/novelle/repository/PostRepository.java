package com.novelle.novelle.repository;
import com.novelle.novelle.entity.Post; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.List;
public interface PostRepository extends JpaRepository<Post,Long>{
  String W="p.status='PUBLISHED' and (:cat='' or p.category=:cat) and (lower(p.title) like lower(concat('%',:q,'%')) or lower(p.description) like lower(concat('%',:q,'%')) or lower(p.content) like lower(concat('%',:q,'%')))";
  @Query(value="select p from Post p where "+W,countQuery="select count(p) from Post p where "+W)
  Page<Post> search(@Param("q") String q,@Param("cat") String cat,Pageable pg);
  @Query(value="select p from Post p where "+W+" order by (select count(l) from PostLike l where l.post=p) desc, p.createdAt desc",countQuery="select count(p) from Post p where "+W)
  Page<Post> searchLiked(@Param("q") String q,@Param("cat") String cat,Pageable pg);
  List<Post> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long id,String status);
  List<Post> findByAuthorId(Long id);
  long countByAuthorIdAndStatus(Long id,String status);
  List<Post> findTop5ByOrderByCreatedAtDesc();
}
