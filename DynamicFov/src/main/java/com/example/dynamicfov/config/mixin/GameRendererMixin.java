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
    private void applyDynamicFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) 
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

            // Updated variable name here
            if (elapsed < config.overallAnimationDurationMs) 
            {
                double baseFov = cir.getReturnValueD();
                double startFov = baseFov - config.initialFovOffset;

                double t = (double) elapsed / config.overallAnimationDurationMs;

                double invT = 1.0 - t;
                double easeOut = 1.0 - (invT * invT * invT);

                double currentFov = startFov + ((baseFov - startFov) * easeOut);
                cir.setReturnValue(currentFov);
            } 
            else 
            {
                DynamicFovClient.animationStartTime = -1;
            }
        }
    }
}