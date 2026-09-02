package com.example.dynamicfov.mixin;

import com.example.dynamicfov.DynamicFovClient;
import com.example.dynamicfov.config.DynamicFovConfig;
import com.example.dynamicfov.util.BezierUtil;
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
        DynamicFovConfig config = AutoConfig.getConfigHolder(DynamicFovConfig.class).getConfig();
        long currentTime = System.currentTimeMillis();

        // Check if cooldown applies (only on the first world load of the launch session)
        if (DynamicFovClient.needsFovAnimation) 
        {
            if (DynamicFovClient.worldLoadCount == 0 && 
                DynamicFovClient.worldLoadTime > 0 && 
            (currentTime - DynamicFovClient.worldLoadTime) < config.worldLoadCooldownMs) 
            {
                // Hold static FOV offset during the cooldown delay
                float baseFov = cir.getReturnValue();
                cir.setReturnValue(baseFov - config.initialFovOffset);
                return;
            } 
            else 
            {
                // Cooldown elapsed or skipped: start animation AND consume the first-load flag immediately
                if (DynamicFovClient.worldLoadCount == 0) {
                    DynamicFovClient.worldLoadCount = 1; // Ensures future world joins skip cooldown regardless of lag!
                }
                DynamicFovClient.animationStartTime = currentTime;
                DynamicFovClient.needsFovAnimation = false;
            }
        }

        // Run smooth FOV transition
        long startTime = DynamicFovClient.animationStartTime;
        if (startTime > 0) 
        {
            long elapsed = currentTime - startTime;

            if (elapsed < config.overallAnimationDurationMs) 
            {
                float baseFov = cir.getReturnValue(); 
                float startFov = baseFov - config.initialFovOffset;

                float t = (float) elapsed / config.overallAnimationDurationMs;
                float easeFactor;

                // Branch depending on whether Bezier curves or polynomial order are enabled
                if (config.useBezierCurve) 
                {
                    easeFactor = BezierUtil.evalBezier(config.bezierCurveValues, t);
                } 
                else 
                {
                    int order = Math.max(1, Math.min(10, config.easingOrder));
                    easeFactor = 1.0f - (float) Math.pow(1.0 - t, order);
                }

                float currentFov = startFov + ((baseFov - startFov) * easeFactor);
                cir.setReturnValue(currentFov);
            } 
            else 
            {
                // Reset animation state once time completes
                DynamicFovClient.animationStartTime = -1;
            }
        }
    }
}
