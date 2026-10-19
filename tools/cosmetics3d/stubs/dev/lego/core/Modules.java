package dev.lego.core;
public final class Modules { public interface Persist { Object save(); void load(Object o); } public static void scheduleSave() {} public static void persist(String k, Persist p) {} }
