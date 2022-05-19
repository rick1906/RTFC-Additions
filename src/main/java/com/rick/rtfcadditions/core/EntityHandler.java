package com.rick.rtfcadditions.core;

import com.rick.rtfcadditions.Messenger;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 *
 * @author Rick
 */
public class EntityHandler
{
    public static final String MOBPROPS_KEY = "MP|Init";
    public static final String MOBPROPS_HEALTH_KEY = "MPAttr|generic|maxHealth";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingSpawn(EntityJoinWorldEvent event)
    {
        if (event.entity.worldObj.isRemote) {
            return;
        }
        if (!(event.entity instanceof EntityLivingBase) || event.entity instanceof EntityPlayer) {
            return;
        }
        if (event.entity.getEntityData().hasKey(MOBPROPS_KEY)) {
            return;
        }
        if (event.entity.getClass().getName().contains("bioxx.tfc")) {
            return;
        }

        EntityLivingBase entity = (EntityLivingBase)event.entity;
        if (entity.getMaxHealth() < 120f) {
            float ratio = getRandomHealthRatio(entity);
            IAttributeInstance attr = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth);
            double maxHealth = 10 * Math.ceil(0.1 * ratio * attr.getBaseValue());
            double healthRatio = entity.getHealth() / attr.getBaseValue();
            attr.setBaseValue(maxHealth);
            entity.getEntityData().setDouble(MOBPROPS_HEALTH_KEY, maxHealth);
            entity.setHealth((float)(healthRatio * maxHealth));
            return;
        }
        if (entity.getMaxHealth() < 180f) {
            logEntity(entity, "Spawned suspicious entity");
        }
    }

    private float getRandomHealthRatio(EntityLivingBase entity)
    {
        int min, max;
        String className = entity.getClass().getName();
        if (className.contains(".randomthings.Entity.EntitySpirit")) {
            min = 10;
            max = 15;
        } else if (className.contains(".passive")) {
            min = 20;
            max = 40;
        } else {
            min = 50;
            max = 70;
        }
        return min + (max - min) * entity.worldObj.rand.nextFloat();
    }

    private void logEntity(EntityLivingBase entity, String message)
    {
        Messenger.debug(message + ": " + entity + " | " + entity.getClass().getName() + ", health=" + entity.getMaxHealth());
    }
}
