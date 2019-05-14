package me.nethersoul.skinoverride;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SkinEventHandler {

    public static final String database = "http://nethersoul.altervista.org/skin_mod/skins.data";

    private CustomPlayerRenderer cpr;

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(SkinOverride.MODID)) {
            ConfigManager.sync(SkinOverride.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    @Subscribe
    @Mod.EventHandler
    public void onArmRender(RenderSpecificHandEvent event) {
        if (this.cpr == null) return;

        if (FMLCommonHandler.instance().getSide().isClient()) {
            try {
                if (event.getItemStack().getItem() == Items.AIR) {

                    if (FMLClientHandler.instance().getClientPlayerEntity() == null ||
                            FMLClientHandler.instance().getClientPlayerEntity().isInvisible())
                        return;

                    EntityPlayerSP player = FMLClientHandler.instance().getClientPlayerEntity();
                    ItemRenderer ir = Minecraft.getMinecraft().getItemRenderer();

                    EntityPlayerSP sp = new EntityPlayerSP(Minecraft.getMinecraft(), Minecraft.getMinecraft().world, Minecraft.getMinecraft().getConnection(), player.getStatFileWriter(), player.getRecipeBook()) {
                        @Override
                        public ResourceLocation getLocationSkin() {
                            return cpr.getEntityTexture(this);
                        }
                    };

                    Minecraft.getMinecraft().player = sp;
                    sp.setPrimaryHand(player.getPrimaryHand());
                    event.setCanceled(true);

                    ir.renderItemInFirstPerson(sp, Minecraft.getMinecraft().getRenderPartialTicks(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress());

                    Minecraft.getMinecraft().player = player;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Subscribe
    @Mod.EventHandler
    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (this.cpr == null) {
            this.cpr = new CustomPlayerRenderer(event.getRenderer().getRenderManager(), event.getRenderer());
        }

        if (FMLCommonHandler.instance().getSide().isClient()) {
            event.setCanceled(true);
            this.cpr.doRender((AbstractClientPlayer) event.getEntity(),
                    event.getX(), event.getY(), event.getZ(),
                    event.getEntity().getRotationYawHead(),
                    event.getPartialRenderTick());
        }
    }
}