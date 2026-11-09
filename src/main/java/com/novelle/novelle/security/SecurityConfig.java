package com.novelle.novelle.security;
import com.novelle.novelle.repository.UserRepository; import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager; import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.csrf.*;
@Configuration
public class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
  @Bean AuthenticationManager authManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
  @Bean UserDetailsService uds(UserRepository r){
    return email -> r.findByEmail(email).map(u->org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPassword()).roles(u.getRole()).build())
      .orElseThrow(()->new UsernameNotFoundException("Unknown user")); }
  @Bean SecurityFilterChain chain(HttpSecurity h) throws Exception {
    h.csrf(c->c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
     .authorizeHttpRequests(a->a.requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET,"/api/**").permitAll()
        .requestMatchers("/api/auth/register","/api/auth/login","/api/newsletter").permitAll()
        .requestMatchers("/api/**").authenticated().anyRequest().permitAll())
     .exceptionHandling(e->e
        .authenticationEntryPoint((q,s,x)->{ s.setStatus(401); s.setContentType("application/json"); s.getWriter().write("{\"status\":401,\"message\":\"Please log in.\"}"); })
        .accessDeniedHandler((q,s,x)->{ s.setStatus(403); s.setContentType("application/json"); s.getWriter().write("{\"status\":403,\"message\":\"You do not have permission to do that.\"}"); }));
    return h.build();
  }
}
