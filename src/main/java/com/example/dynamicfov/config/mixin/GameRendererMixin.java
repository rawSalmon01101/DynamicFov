package com.example.dynamicfov.mixin;

import com.example.dynamicfov.DynamicFovClient;
import com.example.dynamicfov.config.DynamicFovConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin 
{

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void applyDynamicFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) 
    {
        
        if (DynamicFovClient.needsFovAnimation) 
        {
            DynamicFovClient.animationStartTime = System.currentTimeMillis();
            DynamicFovClient.needsFovAnimation = false;
        }

        long startTime = DynamicFovClient.animationStartTime;
        if (startTime > 0) 
        {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - startTime;

            DynamicFovConfig config = AutoConfig.getConfigHolder(DynamicFovConfig.class).getConfig();

            if (elapsed < config.overallAnimationDurationMs) 
            {
                float baseFov = cir.getReturnValue(); 
                float startFov = baseFov - config.initialFovOffset;

                float t = (float) elapsed / config.overallAnimationDurationMs;

                float invT = 1.0f - t;
                // Quintic ease-out curve (invT raised to the 5th power)
                float easeOut = 1.0f - (invT * invT * invT * invT * invT);

                float currentFov = startFov + ((baseFov - startFov) * easeOut);

                cir.setReturnValue(currentFov);
            } 
            else 
            {
                DynamicFovClient.animationStartTime = -1;
            }
        }
    }
}
