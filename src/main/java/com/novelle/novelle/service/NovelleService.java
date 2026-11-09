package com.novelle.novelle.service;
import com.novelle.novelle.dto.Dtos.*; import com.novelle.novelle.entity.*; import com.novelle.novelle.exception.ApiException; import com.novelle.novelle.repository.*; import com.novelle.novelle.util.Text;
import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.security.authentication.AnonymousAuthenticationToken; import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service @Transactional
public class NovelleService {
  static final Set<String> CATS=Set.of("Technology","Programming","Career","Education","Artificial Intelligence","Lifestyle");
  private final UserRepository users; private final PostRepository posts; private final CommentRepository comments; private final LikeRepository likes; private final PasswordEncoder enc;
  public NovelleService(UserRepository u,PostRepository p,CommentRepository c,LikeRepository l,PasswordEncoder e){ users=u; posts=p; comments=c; likes=l; enc=e; }
  static ApiException err(HttpStatus s,String m){ return new ApiException(s,m); }
  public User current(Authentication a){ if(a==null||a instanceof AnonymousAuthenticationToken||!a.isAuthenticated()) return null; return users.findByEmail(a.getName()).orElse(null); }
  public User require(Authentication a){ User u=current(a); if(u==null) throw err(HttpStatus.UNAUTHORIZED,"Please log in."); return u; }
  public UserDto register(RegisterReq r){
    if(!r.password().equals(r.confirmPassword())) throw err(HttpStatus.BAD_REQUEST,"Passwords do not match.");
    String e=r.email().trim().toLowerCase(); if(users.existsByEmail(e)) throw err(HttpStatus.CONFLICT,"That email is already registered.");
    User u=new User(); u.setFullName(r.fullName().trim()); u.setEmail(e); u.setPassword(enc.encode(r.password())); return dto(users.save(u)); }
  public UserDto dto(User u){ return new UserDto(u.getId(),u.getFullName(),u.getEmail(),u.getBio(),u.getRole(),u.getCreatedAt(),posts.countByAuthorIdAndStatus(u.getId(),"PUBLISHED")); }
  public UserDto user(Long id){ return dto(users.findById(id).orElseThrow(()->err(HttpStatus.NOT_FOUND,"Author not found."))); }
  public UserDto updateProfile(ProfileReq r,User u){ u.setBio(r.bio()==null?null:r.bio().trim()); if(r.fullName()!=null&&!r.fullName().isBlank()) u.setFullName(r.fullName().trim()); return dto(users.save(u)); }
  public List<UserDto> authors(){ return users.findAll().stream().map(this::dto).filter(d->d.postCount()>0).sorted(Comparator.comparingLong(UserDto::postCount).reversed()).limit(3).toList(); }
  public List<PostDto> userPosts(Long id,User me){ return posts.findByAuthorIdAndStatusOrderByCreatedAtDesc(id,"PUBLISHED").stream().map(p->dto(p,me,false)).toList(); }
  public PostDto dto(Post p,User me,boolean full){
    return new PostDto(p.getId(),p.getTitle(),p.getDescription(),full?p.getContent():null,p.getCoverImage(),p.getCategory(),p.getStatus(),p.getAuthor().getId(),p.getAuthor().getFullName(),p.getCreatedAt(),
      Text.readingTime(p.getContent()),likes.countByPostId(p.getId()),me!=null&&likes.existsByUserIdAndPostId(me.getId(),p.getId()),comments.countByPostId(p.getId())); }
  public Map<String,Object> list(String q,String cat,String sort,int page,int size,User me){
    boolean liked="liked".equals(sort);
    Pageable pg=PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),24),liked?Sort.unsorted():Sort.by(Sort.Direction.DESC,"createdAt"));
    Page<Post> r=liked?posts.searchLiked(q,cat,pg):posts.search(q,cat,pg);
    return Map.of("items",r.getContent().stream().map(p->dto(p,me,false)).toList(),"page",r.getNumber(),"totalPages",r.getTotalPages(),"total",r.getTotalElements()); }
  Post find(Long id){ return posts.findById(id).orElseThrow(()->err(HttpStatus.NOT_FOUND,"Story not found.")); }
  boolean canEdit(Post p,User u){ return u!=null&&(u.getRole().equals("ADMIN")||p.getAuthor().getId().equals(u.getId())); }
  public PostDto get(Long id,User me){ Post p=find(id); if(!"PUBLISHED".equals(p.getStatus())&&!canEdit(p,me)) throw err(HttpStatus.NOT_FOUND,"Story not found."); return dto(p,me,true); }
  void fill(Post p,PostReq r){
    if(!CATS.contains(r.category())) throw err(HttpStatus.BAD_REQUEST,"Choose a valid category.");
    p.setTitle(r.title().trim()); p.setDescription(r.description().trim()); p.setContent(r.content().trim()); p.setCategory(r.category()); p.setCoverImage(Text.safeUrl(r.coverImage())); }
  public PostDto create(PostReq r,User u){ Post p=new Post(); fill(p,r); p.setAuthor(u); return dto(posts.save(p),u,true); }
  public PostDto update(Long id,PostReq r,User u){ Post p=find(id); if(!canEdit(p,u)) throw err(HttpStatus.FORBIDDEN,"You can only edit your own stories."); fill(p,r); return dto(posts.save(p),u,true); }
  public void delete(Long id,User u){ Post p=find(id); if(!canEdit(p,u)) throw err(HttpStatus.FORBIDDEN,"You can only delete your own stories."); purge(p); }
  void purge(Post p){ comments.deleteByPostId(p.getId()); likes.deleteByPostId(p.getId()); posts.delete(p); }
  CommentDto dto(Comment c){ return new CommentDto(c.getId(),c.getContent(),c.getUser().getId(),c.getUser().getFullName(),c.getPost().getId(),c.getCreatedAt()); }
  public List<CommentDto> comments(Long postId){ find(postId); return comments.findByPostIdOrderByCreatedAtDesc(postId).stream().map(this::dto).toList(); }
  public CommentDto addComment(Long postId,CommentReq r,User u){ Post p=find(postId); if(!"PUBLISHED".equals(p.getStatus())) throw err(HttpStatus.NOT_FOUND,"Story not found.");
    Comment c=new Comment(); c.setContent(r.content().trim()); c.setUser(u); c.setPost(p); return dto(comments.save(c)); }
  public void deleteComment(Long id,User u){ Comment c=comments.findById(id).orElseThrow(()->err(HttpStatus.NOT_FOUND,"Comment not found."));
    if(!u.getRole().equals("ADMIN")&&!c.getUser().getId().equals(u.getId())) throw err(HttpStatus.FORBIDDEN,"You can only delete your own comments."); comments.delete(c); }
  public PostDto like(Long id,User u,boolean on){ Post p=find(id); boolean has=likes.existsByUserIdAndPostId(u.getId(),id);
    if(on&&!has){ PostLike l=new PostLike(); l.setUser(u); l.setPost(p); likes.saveAndFlush(l); } else if(!on&&has) likes.deleteByUserIdAndPostId(u.getId(),id);
    return dto(p,u,false); }
  public Map<String,Object> dashboard(){
    List<Map<String,Object>> recent=new ArrayList<>();
    posts.findTop5ByOrderByCreatedAtDesc().forEach(p->recent.add(Map.of("text",p.getAuthor().getFullName()+" published \""+p.getTitle()+"\"","at",p.getCreatedAt())));
    comments.findTop5ByOrderByCreatedAtDesc().forEach(c->recent.add(Map.of("text",c.getUser().getFullName()+" commented on \""+c.getPost().getTitle()+"\"","at",c.getCreatedAt())));
    recent.sort((a,b)->((java.time.LocalDateTime)b.get("at")).compareTo((java.time.LocalDateTime)a.get("at")));
    return Map.of("users",users.count(),"posts",posts.count(),"comments",comments.count(),"likes",likes.count(),"recent",recent); }
  public List<UserDto> allUsers(){ return users.findAll().stream().map(this::dto).toList(); }
  public List<PostDto> allPosts(User me){ return posts.findAll(Sort.by(Sort.Direction.DESC,"createdAt")).stream().map(p->dto(p,me,false)).toList(); }
  public List<CommentDto> allComments(){ return comments.findTop100ByOrderByCreatedAtDesc().stream().map(this::dto).toList(); }
  public void deleteUser(Long id,User admin){ if(admin.getId().equals(id)) throw err(HttpStatus.BAD_REQUEST,"You cannot delete your own account.");
    User u=users.findById(id).orElseThrow(()->err(HttpStatus.NOT_FOUND,"User not found."));
    comments.deleteByUserId(id); likes.deleteByUserId(id); posts.findByAuthorId(id).forEach(this::purge); users.delete(u); }
}
