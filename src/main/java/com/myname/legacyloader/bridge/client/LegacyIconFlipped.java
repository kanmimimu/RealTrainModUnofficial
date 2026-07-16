package com.myname.legacyloader.bridge.client;
public class LegacyIconFlipped implements LegacyIcon {
 private final LegacyIcon base; private final boolean flipU, flipV;
 public LegacyIconFlipped(LegacyIcon icon, boolean u, boolean v){ this.base=icon; this.flipU=u; this.flipV=v; }
 public int getIconWidth(){ return base==null?16:base.getIconWidth(); }
 public int getIconHeight(){ return base==null?16:base.getIconHeight(); }
 public float getMinU(){ return base==null?0:(flipU?base.getMaxU():base.getMinU()); }
 public float getMaxU(){ return base==null?1:(flipU?base.getMinU():base.getMaxU()); }
 public float getMinV(){ return base==null?0:(flipV?base.getMaxV():base.getMinV()); }
 public float getMaxV(){ return base==null?1:(flipV?base.getMinV():base.getMaxV()); }
 public String getIconName(){ return base==null?"missingno":base.getIconName(); }
}
