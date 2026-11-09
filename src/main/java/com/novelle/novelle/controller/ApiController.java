package com.novelle.novelle.controller;
import com.novelle.novelle.dto.Dtos.*; import com.novelle.novelle.entity.User; import com.novelle.novelle.exception.ApiException; import com.novelle.novelle.service.NovelleService;
import jakarta.servlet.http.*; import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.security.authentication.*; import org.springframework.security.core.*;
import org.springframework.security.core.context.*; import org.springframework.security.web.context.HttpSessionSecurityContextRepository; import org.springframework.security.web.csrf.CsrfToken; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api")
public class ApiController {
  private final NovelleService s; private final AuthenticationManager am;
  public ApiController(NovelleService s,AuthenticationManager am){ this.s=s; this.am=am; }
  @GetMapping("/auth/me") Map<String,Object> me(Authentication a,CsrfToken t){ User u=s.current(a); Map<String,Object> m=new HashMap<>(); m.put("user",u==null?null:s.dto(u)); m.put("csrf",t.getToken()); return m; }
  @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED) UserDto register(@Valid @RequestBody RegisterReq r){ return s.register(r); }
  @PostMapping("/auth/login") UserDto login(@Valid @RequestBody LoginReq r,HttpServletRequest q,HttpServletResponse res){
    try{ Authentication a=am.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(r.email().trim().toLowerCase(),r.password()));
      q.getSession(true); q.changeSessionId(); SecurityContext c=SecurityContextHolder.createEmptyContext(); c.setAuthentication(a); SecurityContextHolder.setContext(c);
      new HttpSessionSecurityContextRepository().saveContext(c,q,res); return s.dto(s.current(a));
    }catch(AuthenticationException e){ throw new ApiException(HttpStatus.UNAUTHORIZED,"Invalid email or password."); } }
  @PostMapping("/auth/logout") Map<String,String> logout(HttpServletRequest q){ HttpSession h=q.getSession(false); if(h!=null) h.invalidate(); SecurityContextHolder.clearContext(); return Map.of("message","Logged out."); }
  @PostMapping("/newsletter") Map<String,String> newsletter(@RequestBody Map<String,String> b){ String e=b.getOrDefault("email","");
    if(!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new ApiException(HttpStatus.BAD_REQUEST,"Enter a valid email address."); return Map.of("message","Thanks. We will be in touch."); }
  @GetMapping("/posts") Map<String,Object> posts(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String category,@RequestParam(defaultValue="newest") String sort,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="9") int size,Authentication a){ return s.list(q.trim(),category,sort,page,size,s.current(a)); }
  @GetMapping("/posts/{id}") PostDto post(@PathVariable Long id,Authentication a){ return s.get(id,s.current(a)); }
  @PostMapping("/posts") @ResponseStatus(HttpStatus.CREATED) PostDto create(@Valid @RequestBody PostReq r,Authentication a){ return s.create(r,s.require(a)); }
  @PutMapping("/posts/{id}") PostDto update(@PathVariable Long id,@Valid @RequestBody PostReq r,Authentication a){ return s.update(id,r,s.require(a)); }
  @DeleteMapping("/posts/{id}") Map<String,String> delete(@PathVariable Long id,Authentication a){ s.delete(id,s.require(a)); return Map.of("message","Story deleted."); }
  @GetMapping("/posts/{id}/comments") List<CommentDto> comments(@PathVariable Long id){ return s.comments(id); }
  @PostMapping("/posts/{id}/comments") @ResponseStatus(HttpStatus.CREATED) CommentDto comment(@PathVariable Long id,@Valid @RequestBody CommentReq r,Authentication a){ return s.addComment(id,r,s.require(a)); }
  @DeleteMapping("/comments/{id}") Map<String,String> delComment(@PathVariable Long id,Authentication a){ s.deleteComment(id,s.require(a)); return Map.of("message","Comment deleted."); }
  @PostMapping("/posts/{id}/like") PostDto like(@PathVariable Long id,Authentication a){ return s.like(id,s.require(a),true); }
  @DeleteMapping("/posts/{id}/like") PostDto unlike(@PathVariable Long id,Authentication a){ return s.like(id,s.require(a),false); }
  @GetMapping("/users/authors") List<UserDto> authors(){ return s.authors(); }
  @GetMapping("/users/{id}") UserDto user(@PathVariable Long id){ return s.user(id); }
  @PutMapping("/users/profile") UserDto profile(@Valid @RequestBody ProfileReq r,Authentication a){ return s.updateProfile(r,s.require(a)); }
  @GetMapping("/users/{id}/posts") List<PostDto> userPosts(@PathVariable Long id,Authentication a){ return s.userPosts(id,s.current(a)); }
  @GetMapping("/admin/dashboard") Map<String,Object> dash(){ return s.dashboard(); }
  @GetMapping("/admin/users") List<UserDto> aUsers(){ return s.allUsers(); }
  @DeleteMapping("/admin/users/{id}") Map<String,String> aDelUser(@PathVariable Long id,Authentication a){ s.deleteUser(id,s.require(a)); return Map.of("message","User deleted."); }
  @GetMapping("/admin/posts") List<PostDto> aPosts(Authentication a){ return s.allPosts(s.current(a)); }
  @DeleteMapping("/admin/posts/{id}") Map<String,String> aDelPost(@PathVariable Long id,Authentication a){ s.delete(id,s.require(a)); return Map.of("message","Story deleted."); }
  @GetMapping("/admin/comments") List<CommentDto> aComments(){ return s.allComments(); }
}
