package dev.lego.visual.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ShaderUtil {
    private static RenderPipeline.Snippet transformsSnippet;
    private static Method registerMethod;
    private static boolean resolved;

    public static RenderPipeline.Snippet getTransformsSnippet() {
        resolve();
        return transformsSnippet;
    }

    public static RenderPipeline register(RenderPipeline pipeline) {
        resolve();
        if (registerMethod == null) return pipeline;
        try {
            return (RenderPipeline) registerMethod.invoke(null, pipeline);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to register pipeline", t);
        }
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Field f = net.minecraft.class_10799.class.getDeclaredField("field_60125");
            f.setAccessible(true);
            transformsSnippet = (RenderPipeline.Snippet) f.get(null);
        } catch (Throwable t) {
            System.err.println("[LegoClient] Failed to get TRANSFORMS_AND_PROJECTION_SNIPPET: " + t);
        }
        try {
            for (Method m : net.minecraft.class_10799.class.getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == RenderPipeline.class
                        && m.getReturnType() == RenderPipeline.class) {
                    m.setAccessible(true);
                    registerMethod = m;
                    break;
                }
            }
        } catch (Throwable t) {
            System.err.println("[LegoClient] Failed to get register method: " + t);
        }
    }
}
