package com.example.dynamicfov.integration;

import com.example.dynamicfov.config.DynamicFovConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi 
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() 
    {
        return parent -> AutoConfig.getConfigScreen(DynamicFovConfig.class, parent).get();
    }
}