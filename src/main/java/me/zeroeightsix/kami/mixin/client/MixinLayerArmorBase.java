package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.ArmourHide;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase {

    private static final FloatBuffer color = GLAllocation.createDirectFloatBuffer(16);
    private static boolean texture2d = false;
    private static boolean colorLock = false;

    @Inject(method = "renderArmorLayer", at = @At("HEAD"), cancellable = true)
    public void renderArmorLayerPre(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        if (ArmourHide.INSTANCE.isEnabled()) {
            if ((ArmourHide.INSTANCE.getPlayer().getValue()) && entityLivingBaseIn instanceof EntityPlayer) {
                if (ArmourHide.shouldHidePiece(slotIn)) ci.cancel();
            } else if ((ArmourHide.INSTANCE.getArmourStand().getValue()) && entityLivingBaseIn instanceof EntityArmorStand) {
                if (ArmourHide.shouldHidePiece(slotIn)) ci.cancel();
            } else if ((ArmourHide.INSTANCE.getMobs().getValue()) && entityLivingBaseIn instanceof EntityMob) {
                if (ArmourHide.shouldHidePiece(slotIn)) ci.cancel();
            }
        }

        if (!ci.isCancelled()) {
            colorLock = GlStateUtils.getColorLock();
            texture2d = glGetBoolean(GL_TEXTURE_2D);
            if (colorLock) {
                glGetFloat(GL_CURRENT_COLOR, color);
                GlStateUtils.colorLock(false);
            }
            if (!texture2d) {
                GlStateManager.enableTexture2D();
            }
        }
    }

    @Inject(method = "renderArmorLayer", at = @At("RETURN"))
    public void renderArmorLayerPost(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        if (colorLock) {
            GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
            GlStateUtils.colorLock(true);
        }
        if (!texture2d) {
            GlStateManager.disableTexture2D();
        }
    }

}