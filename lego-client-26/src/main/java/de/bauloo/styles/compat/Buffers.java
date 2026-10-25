package de.bauloo.styles.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class Buffers {
   private final SubmitNodeCollector collector;
   private final Map<RenderType, Buffers.Recorder> open = new LinkedHashMap<>();

   public Buffers(SubmitNodeCollector collector) {
      this.collector = collector;
   }

   public SubmitNodeCollector collector() {
      return this.collector;
   }

   public VertexConsumer getBuffer(RenderType type) {
      return this.open.computeIfAbsent(type, t -> new Buffers.Recorder());
   }

   public void flush() {
      PoseStack identity = new PoseStack();

      for (Entry<RenderType, Buffers.Recorder> e : this.open.entrySet()) {
         Buffers.Recorder r = e.getValue();
         if (!r.vertices.isEmpty()) {
            this.collector.submitCustomGeometry(identity, e.getKey(), (pose, vc) -> r.replay(vc));
         }
      }

      this.open.clear();
   }

   private static final class Recorder implements VertexConsumer {
      final List<Buffers.V> vertices = new ArrayList<>();
      private Buffers.V cur;

      void replay(VertexConsumer vc) {
         for (Buffers.V v : this.vertices) {
            vc.addVertex(v.x, v.y, v.z);
            if (v.hasColor) {
               vc.setColor(v.color);
            }

            if (v.hasUv) {
               vc.setUv(v.u, v.v);
            }

            if (v.hasUv1) {
               vc.setUv1(v.u1, v.v1);
            }

            if (v.hasUv2) {
               vc.setUv2(v.u2, v.v2);
            }

            if (v.hasNormal) {
               vc.setNormal(v.nx, v.ny, v.nz);
            }

            if (v.hasLineWidth) {
               vc.setLineWidth(v.lineWidth);
            }
         }
      }

      public VertexConsumer addVertex(float x, float y, float z) {
         this.cur = new Buffers.V();
         this.cur.x = x;
         this.cur.y = y;
         this.cur.z = z;
         this.vertices.add(this.cur);
         return this;
      }

      public VertexConsumer setColor(int r, int g, int b, int a) {
         return this.setColor(a << 24 | r << 16 | g << 8 | b);
      }

      public VertexConsumer setColor(int color) {
         this.cur.color = color;
         this.cur.hasColor = true;
         return this;
      }

      public VertexConsumer setUv(float u, float v) {
         this.cur.u = u;
         this.cur.v = v;
         this.cur.hasUv = true;
         return this;
      }

      public VertexConsumer setUv1(int u, int v) {
         this.cur.u1 = u;
         this.cur.v1 = v;
         this.cur.hasUv1 = true;
         return this;
      }

      public VertexConsumer setUv2(int u, int v) {
         this.cur.u2 = u;
         this.cur.v2 = v;
         this.cur.hasUv2 = true;
         return this;
      }

      public VertexConsumer setNormal(float x, float y, float z) {
         this.cur.nx = x;
         this.cur.ny = y;
         this.cur.nz = z;
         this.cur.hasNormal = true;
         return this;
      }

      public VertexConsumer setLineWidth(float width) {
         this.cur.lineWidth = width;
         this.cur.hasLineWidth = true;
         return this;
      }
   }

   private static final class V {
      float x;
      float y;
      float z;
      float u;
      float v;
      float nx;
      float ny;
      float nz;
      float lineWidth;
      int color;
      int u1;
      int v1;
      int u2;
      int v2;
      boolean hasColor;
      boolean hasUv;
      boolean hasUv1;
      boolean hasUv2;
      boolean hasNormal;
      boolean hasLineWidth;
   }
}
