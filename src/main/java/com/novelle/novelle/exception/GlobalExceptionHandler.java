package com.novelle.novelle.exception;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.http.*; import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
  private ResponseEntity<Map<String,Object>> r(HttpStatus s,String m){ return ResponseEntity.status(s).body(Map.of("status",s.value(),"message",m)); }
  @ExceptionHandler(ApiException.class) ResponseEntity<Map<String,Object>> api(ApiException e){ return r(e.status,e.getMessage()); }
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> val(MethodArgumentNotValidException e){
    return r(HttpStatus.BAD_REQUEST,e.getBindingResult().getFieldErrors().stream().findFirst().map(f->f.getField()+" "+f.getDefaultMessage()).orElse("Invalid input.")); }
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<Map<String,Object>> dup(DataIntegrityViolationException e){ return r(HttpStatus.CONFLICT,"That record already exists or is still in use."); }
  @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<Map<String,Object>> bad(HttpMessageNotReadableException e){ return r(HttpStatus.BAD_REQUEST,"Malformed request."); }
}
