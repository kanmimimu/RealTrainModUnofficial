package com.myname.legacyloader.bridge.forge;
import java.util.*;
public interface LegacyIShearable { default boolean isShearable(Object item,Object world,int x,int y,int z){return false;} default List<Object> onSheared(Object item,Object world,int x,int y,int z,int fortune){return Collections.emptyList();} }
