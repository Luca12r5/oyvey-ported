package dev.lego.visual.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.RenderPipelines;

public class ShaderUtil {
   private static Snippet transformsSnippet;
   private static Method registerMethod;
   private static boolean resolved;

   public static Snippet getTransformsSnippet() {
      resolve();
      return transformsSnippet;
   }

   public static RenderPipeline register(RenderPipeline pipeline) {
      resolve();
      if (registerMethod == null) {
         return pipeline;
      } else {
         try {
            return (RenderPipeline)registerMethod.invoke(null, pipeline);
         } catch (Throwable var2) {
            throw new RuntimeException("Failed to register pipeline", var2);
         }
      }
   }

   private static void resolve() {
      if (!resolved) {
         resolved = true;

         try {
            Field f = RenderPipelines.class.getDeclaredField("field_60125");
            f.setAccessible(true);
            transformsSnippet = (Snippet)f.get(null);
         } catch (Throwable var4) {
            System.err.println("[LegoClient] Failed to get TRANSFORMS_AND_PROJECTION_SNIPPET: " + var4);
         }

         try {
            for (Method m : RenderPipelines.class.getDeclaredMethods()) {
               if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == RenderPipeline.class && m.getReturnType() == RenderPipeline.class) {
                  m.setAccessible(true);
                  registerMethod = m;
                  break;
               }
            }
         } catch (Throwable var5) {
            System.err.println("[LegoClient] Failed to get register method: " + var5);
         }
      }
   }
}
