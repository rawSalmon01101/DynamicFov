package com.example.dynamicfov.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "dynamicfov")
public class DynamicFovConfig implements ConfigData 
{

    @ConfigEntry.Gui.Tooltip(count = 1)
    public int initialFovOffset = 10;

    @ConfigEntry.Gui.Tooltip(count = 1)
    public int overallAnimationDurationMs = 2000; // Controls BOTH FOV and the screen fade

    @ConfigEntry.Gui.Tooltip(count = 1)
    public boolean fadeFromBlack = true;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public int easingOrder = 5; // Default polynomial power (e.g., 5 for quintic)
}
