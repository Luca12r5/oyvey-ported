package de.bauloo.styles.mixin;

import de.bauloo.styles.CrystalAge;
import net.minecraft.class_10014;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(class_10014.class)
public class CrystalAgeStateMixin implements CrystalAge {
   @Unique
   private float baulooStyles$realAge = Float.NaN;

   @Override
   public float baulooStyles$realAge() {
      return this.baulooStyles$realAge;
   }

   @Override
   public void baulooStyles$setRealAge(float age) {
      this.baulooStyles$realAge = age;
   }
}
