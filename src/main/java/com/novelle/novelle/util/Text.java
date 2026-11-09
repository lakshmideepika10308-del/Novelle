package com.novelle.novelle.util;
public final class Text { private Text(){}
  public static int readingTime(String c){ return Math.max(1,(int)Math.ceil(c.trim().split("\\s+").length/200.0)); }
  public static String safeUrl(String u){ if(u==null) return null; u=u.trim(); return u.matches("(?i)^https?://\\S+$")?u:null; }
}
