package me.nethersoul.skinoverride;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.nethersoul.skinoverride.utils.FileUtils;
import me.nethersoul.skinoverride.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class CustomPlayerRenderer extends RenderLivingBase<AbstractClientPlayer> {

    public static Map<String, Object[]> skins = Maps.newHashMap();

    private static Timer timer = new Timer();

    private final RenderPlayer oldPlayerRender;

    @Override
    public ResourceLocation getEntityTexture(AbstractClientPlayer entity) {
        if (timer.hasReach(5000)) {
            List<String> toRemove = Lists.newArrayList();

            for (String name : skins.keySet()) {
                if (name.equals(entity.getName())) {
                    if (!skins.get(name)[1].equals(getSkinUrl(entity.getName()))) {
                        toRemove.add(name);
                    }
                }
            }

            toRemove.forEach(skins::remove);

            timer.reset();
        }

        if (!skins.containsKey(entity.getName())) {
            ResourceLocation rl = new ResourceLocation("skins", entity.getName() + ".png");
            ThreadDownloadImageData texture = new ThreadDownloadImageData(null, getSkinUrl(entity.getName()), null, null);
            Minecraft.getMinecraft().getTextureManager().loadTexture(rl, texture);
            skins.put(entity.getName(), new Object[]{rl, getSkinUrl(entity.getName())});
        }

        return (ResourceLocation) skins.get(entity.getName())[0];
    }

    /**
     * Retrieves a list of Strings from a txt file database
     * Then compares the nicknames and returns the corresponding skin url
     *
     * @param name
     * @return
     */
    public String getSkinUrl(String name) {
        try {
            List<String> list = SkinOverride.getSkins();//FileUtils.readUrlResponseFull(new URL(SkinEventHandler.database));

            for (int i = list.size() - 1; i >= 0; i--) {
                String s = list.get(i);
                String split[] = s.split(":");

                if (split[0].equalsIgnoreCase(name)) {
                    return s.substring(split[0].length() + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "https://i.imgur.com/g0H5QRq.png";
    }

    /**
     * this field is used to indicate the 3-pixel wide arms
     */
    private final boolean smallArms;

    public CustomPlayerRenderer(RenderManager renderManager, RenderPlayer oldPlayerRender) {
        this(renderManager, false, oldPlayerRender);
    }

    public CustomPlayerRenderer(RenderManager renderManager, boolean useSmallArms, RenderPlayer oldPlayerRender) {
        super(renderManager, oldPlayerRender.getMainModel(), 0.5F);
        this.oldPlayerRender = oldPlayerRender;

        this.smallArms = useSmallArms;
        this.addLayer(new LayerBipedArmor(this.oldPlayerRender));
        this.addLayer(new LayerHeldItem(this.oldPlayerRender));
        this.addLayer(new LayerArrow(this.oldPlayerRender));
        this.addLayer(new LayerDeadmau5Head(this.oldPlayerRender));
        this.addLayer(new LayerCape(this.oldPlayerRender));
        this.addLayer(new LayerCustomHead(this.oldPlayerRender.getMainModel().bipedHead));
        this.addLayer(new LayerElytra(this.oldPlayerRender));
        this.addLayer(new LayerEntityOnShoulder(renderManager));
    }

    public ModelPlayer getMainModel() {
        return (ModelPlayer) super.getMainModel();
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!entity.isUser() || this.renderManager.renderViewEntity == entity) {
            double d0 = y;

            if (entity.isSneaking()) {
                d0 = y - 0.125D;
            }

            this.setModelVisibilities(entity);
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            super.doRender(entity, x, d0, z, entityYaw, partialTicks);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        }
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer) {
        ModelPlayer modelplayer = this.getMainModel();

        if (clientPlayer.isSpectator()) {
            modelplayer.setVisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        } else {
            ItemStack itemstack = clientPlayer.getHeldItemMainhand();
            ItemStack itemstack1 = clientPlayer.getHeldItemOffhand();
            modelplayer.setVisible(true);
            modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.isSneak = clientPlayer.isSneaking();
            ModelBiped.ArmPose modelbiped$armpose = ModelBiped.ArmPose.EMPTY;
            ModelBiped.ArmPose modelbiped$armpose1 = ModelBiped.ArmPose.EMPTY;

            if (!itemstack.isEmpty()) {
                modelbiped$armpose = ModelBiped.ArmPose.ITEM;

                if (clientPlayer.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemstack.getItemUseAction();

                    if (enumaction == EnumAction.BLOCK) {
                        modelbiped$armpose = ModelBiped.ArmPose.BLOCK;
                    } else if (enumaction == EnumAction.BOW) {
                        modelbiped$armpose = ModelBiped.ArmPose.BOW_AND_ARROW;
                    }
                }
            }

            if (!itemstack1.isEmpty()) {
                modelbiped$armpose1 = ModelBiped.ArmPose.ITEM;

                if (clientPlayer.getItemInUseCount() > 0) {
                    EnumAction enumaction1 = itemstack1.getItemUseAction();

                    if (enumaction1 == EnumAction.BLOCK) {
                        modelbiped$armpose1 = ModelBiped.ArmPose.BLOCK;
                    }
                    // FORGE: fix MC-88356 allow offhand to use bow and arrow animation
                    else if (enumaction1 == EnumAction.BOW) {
                        modelbiped$armpose1 = ModelBiped.ArmPose.BOW_AND_ARROW;
                    }
                }
            }

            if (clientPlayer.getPrimaryHand() == EnumHandSide.RIGHT) {
                modelplayer.rightArmPose = modelbiped$armpose;
                modelplayer.leftArmPose = modelbiped$armpose1;
            } else {
                modelplayer.rightArmPose = modelbiped$armpose1;
                modelplayer.leftArmPose = modelbiped$armpose;
            }
        }
    }

    public void transformHeldFull3DItemLayer() {
        oldPlayerRender.transformHeldFull3DItemLayer();
    }

    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime) {
        float f = 0.9375F;
        GlStateManager.scale(f, f, f);
    }

    protected void renderEntityName(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq) {
        if (distanceSq < 100.0D) {
            Scoreboard scoreboard = entityIn.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null) {
                Score score = scoreboard.getOrCreateScore(entityIn.getName(), scoreobjective);
                this.renderLivingLabel(entityIn, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
                y += (double) ((float) this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F);
            }
        }

        super.renderEntityName(entityIn, x, y, z, name, distanceSq);
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z) {
        if (entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.isPlayerSleeping()) {
            super.renderLivingAt(entityLivingBaseIn, x + (double) entityLivingBaseIn.renderOffsetX, y + (double) entityLivingBaseIn.renderOffsetY, z + (double) entityLivingBaseIn.renderOffsetZ);
        } else {
            super.renderLivingAt(entityLivingBaseIn, x, y, z);
        }
    }

    protected void applyRotations(AbstractClientPlayer entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        if (entityLiving.isEntityAlive() && entityLiving.isPlayerSleeping()) {
            GlStateManager.rotate(entityLiving.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
        } else if (entityLiving.isElytraFlying()) {
            super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
            float f = (float) entityLiving.getTicksElytraFlying() + partialTicks;
            float f1 = MathHelper.clamp(f * f / 100.0F, 0.0F, 1.0F);
            GlStateManager.rotate(f1 * (-90.0F - entityLiving.rotationPitch), 1.0F, 0.0F, 0.0F);
            Vec3d vec3d = entityLiving.getLook(partialTicks);
            double d0 = entityLiving.motionX * entityLiving.motionX + entityLiving.motionZ * entityLiving.motionZ;
            double d1 = vec3d.x * vec3d.x + vec3d.z * vec3d.z;

            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (entityLiving.motionX * vec3d.x + entityLiving.motionZ * vec3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
                double d3 = entityLiving.motionX * vec3d.z - entityLiving.motionZ * vec3d.x;
                GlStateManager.rotate((float) (Math.signum(d3) * Math.acos(d2)) * 180.0F / (float) Math.PI, 0.0F, 1.0F, 0.0F);
            }
        } else {
            super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
        }
    }
}