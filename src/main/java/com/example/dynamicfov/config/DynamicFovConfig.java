package com.example.dynamicfov.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
//fix #8 (tweaks to Bezier)
@Config(name = "dynamicfov")
public class DynamicFovConfig implements ConfigData 
{
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int initialFovOffset = 20;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public int overallAnimationDurationMs = 1500;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean fadeFromBlack = true;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int easingOrder = 5;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 3000)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int worldLoadCooldownMs = 1000;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean useBezierCurve = true;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public String bezierCurveValues = "0.27, 0.89, 0, 0.99";
}
