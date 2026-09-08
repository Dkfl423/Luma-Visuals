package dev.luma.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Locale;

public final class HudRenderer {
    private static long lastFrame = System.nanoTime();
    private static float displayHealth;
    private static int targetId = Integer.MIN_VALUE;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private HudRenderer() { }
    public static float scale(int w, int h) { return HudMath.effectiveScale(w, h, LumaVisualsClient.config.scale); }
    public static EnumMap<HudModule, HudMath.Box> layout(int width, int height) {
        var boxes = new EnumMap<HudModule, HudMath.Box>(HudModule.class);
        float s = scale(width, height);
        int cw = (int) (width/s), ch = (int) (height/s);
        for (HudModule m : HudModule.values()) {
            LumaConfig.Placement p = LumaVisualsClient.config.hud.get(m);
            boxes.put(m, new HudMath.Box(HudMath.position(p.x, cw, m.width), HudMath.position(p.y, ch, m.height), m.width, m.height));
        }
        return boxes;
    }
    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || (mc.screen != null && !(mc.screen instanceof ChatScreen))) return;
        draw(g, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), false);
    }
    public static void draw(GuiGraphics g, int width, int height, boolean editor) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float s = scale(width, height);
        g.pose().pushMatrix(); g.pose().scale(s, s);
        var boxes = layout(width, height);
        for (HudModule m : HudModule.values()) {
            if (!LumaVisualsClient.config.hud.get(m).enabled) continue;
            var b = boxes.get(m);
            if (m == HudModule.TARGET && !editor && LumaVisualsClient.state.target(mc) == null) continue;
            if (m == HudModule.EFFECTS && !editor && mc.player.getActiveEffects().isEmpty()) continue;
            Paint.panel(g, b.x(), b.y(), b.width(), b.height());
            switch (m) {
                case WATERMARK -> watermark(g, b.x(), b.y(), mc);
                case INFO -> info(g, b.x(), b.y(), mc);
                case KEYS -> keys(g, b.x(), b.y(), mc);
                case ARMOR -> armor(g, b.x(), b.y(), mc);
                case TARGET -> target(g, b.x(), b.y(), mc, editor);
                case EFFECTS -> effects(g, b.x(), b.y(), mc);
            }
        }
        g.pose().popMatrix();
    }
    private static void watermark(GuiGraphics g, int x, int y, Minecraft mc) {
        Paint.logo(g, x+9, y+10, Paint.accent());
        Paint.text(g, "LUMA", x+32, y+6, Paint.TEXT);
        Paint.text(g, "VISUALS", x+32, y+18, Paint.MUTED);
        g.fill(x+81,y+8,x+82,y+24,Paint.LINE);
        Paint.clipped(g, mc.getFps() + " FPS", x+92,y+12,64,Paint.accent());
        String ping = "LOCAL";
        if (!mc.hasSingleplayerServer() && mc.getConnection() != null) {
            var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            ping = info == null ? "-- ms" : info.getLatency() + " ms";
        }
        g.fill(x+163,y+8,x+164,y+24,Paint.LINE);
        Paint.clipped(g,ping,x+176,y+12,55,Paint.MUTED);
    }
    private static void info(GuiGraphics g, int x, int y, Minecraft mc) {
        Paint.text(g,"НАВИГАЦИЯ",x+9,y+8,Paint.MUTED);
        var p = mc.player;
        String pos = p.getBlockX() + " / " + p.getBlockY() + " / " + p.getBlockZ();
        Paint.clipped(g,pos,x+9,y+22,151,Paint.TEXT);
        String speed = String.format(Locale.ROOT,"%.1f b/s", LumaVisualsClient.state.speed);
        Paint.text(g,speed,x+9,y+35,Paint.accent());
        Paint.text(g,p.getDirection().getName().toUpperCase(Locale.ROOT),x+92,y+35,Paint.MUTED);
    }
    private static void key(GuiGraphics g, int x, int y, int w, int h, String label, boolean pressed) {
        Paint.box(g,x,y,w,h,pressed ? Paint.accent() : 0xFF1C2A26);
        String text = Minecraft.getInstance().font.plainSubstrByWidth(label,w-6);
        Paint.centered(g,text,x+w/2,y+(h-8)/2,pressed ? 0xFF10221C : Paint.MUTED);
    }
    private static String keyName(KeyMapping key) { return key.getTranslatedKeyMessage().getString().toUpperCase(Locale.ROOT); }
    private static void keys(GuiGraphics g, int x, int y, Minecraft mc) {
        var o = mc.options;
        Paint.text(g,"INPUT",x+7,y+6,Paint.MUTED);
        key(g,x+34,y+20,22,20,keyName(o.keyUp),o.keyUp.isDown());
        key(g,x+8,y+44,22,20,keyName(o.keyLeft),o.keyLeft.isDown());
        key(g,x+34,y+44,22,20,keyName(o.keyDown),o.keyDown.isDown());
        key(g,x+60,y+44,22,20,keyName(o.keyRight),o.keyRight.isDown());
        key(g,x+8,y+68,74,15,keyName(o.keyJump),o.keyJump.isDown());
        key(g,x+8,y+87,35,15,"ATK",o.keyAttack.isDown());
        key(g,x+47,y+87,35,15,"USE",o.keyUse.isDown());
    }
    private static void armor(GuiGraphics g, int x, int y, Minecraft mc) {
        Paint.text(g,"БРОНЯ",x+9,y+7,Paint.MUTED);
        for (int i=0; i<4; i++) {
            int sx = x+9+i*29;
            var stack = mc.player.getItemBySlot(ARMOR[i]);
            if (stack.isEmpty()) {
                Paint.box(g,sx,y+21,24,19,0xFF1C2A26);
                Paint.centered(g,"—",sx+12,y+27,Paint.MUTED);
                continue;
            }
            g.renderItem(stack,sx+4,y+20);
            float durability = stack.isDamageableItem() ? 1f - (float) stack.getDamageValue()/Math.max(1,stack.getMaxDamage()) : 1;
            Paint.bar(g,sx,y+44,24,durability,durability < .2f ? 0xFFF3AD84 : Paint.accent());
        }
    }
    private static void target(GuiGraphics g, int x, int y, Minecraft mc, boolean editor) {
        LivingEntity entity = LumaVisualsClient.state.target(mc);
        if (entity == null && editor) entity = mc.player;
        if (entity == null) return;
        long frame = System.nanoTime();
        double dt = (frame-lastFrame)/1_000_000_000.0; lastFrame = frame;
        if (targetId != entity.getId()) { targetId = entity.getId(); displayHealth=entity.getHealth(); }
        displayHealth = (float) HudMath.smooth(displayHealth,entity.getHealth(),dt,LumaVisualsClient.config.reducedMotion);
        Paint.box(g,x+9,y+10,29,29,0xFF233C33);
        Paint.logo(g,x+15,y+18,Paint.accent());
        Paint.clipped(g,entity.getDisplayName().getString(),x+47,y+10,125,Paint.TEXT);
        Paint.clipped(g,String.format(Locale.ROOT,"%.1f HP / %.1f",entity.getHealth(),entity.getMaxHealth()),x+47,y+25,125,Paint.MUTED);
        Paint.bar(g,x+10,y+45,162,displayHealth/Math.max(1,entity.getMaxHealth()),Paint.accent());
        Paint.text(g,editor ? "ПРЕДПРОСМОТР" : "В ПОЛЕ ЗРЕНИЯ",x+10,y+53,Paint.MUTED);
    }
    private static void effects(GuiGraphics g, int x, int y, Minecraft mc) {
        var effects = new ArrayList<>(mc.player.getActiveEffects());
        effects.sort(Comparator.comparing(e -> e.getEffect().value().getDisplayName().getString()));
        Paint.text(g,"ЭФФЕКТЫ" + (effects.size()>4 ? " +"+(effects.size()-4) : ""),x+9,y+8,Paint.MUTED);
        if (effects.isEmpty()) { Paint.text(g,"Нет активных эффектов",x+9,y+28,Paint.MUTED); return; }
        int count = Math.min(4,effects.size());
        for (int i=0; i<count; i++) {
            MobEffectInstance effect=effects.get(i);
            int row=y+24+i*13;
            String name=effect.getEffect().value().getDisplayName().getString();
            if (effect.getAmplifier()>0) name += " " + (effect.getAmplifier()+1);
            Paint.clipped(g,name,x+9,row,107,Paint.TEXT);
            int sec=Math.max(0,effect.getDuration()/20);
            String time=effect.isInfiniteDuration() ? "∞" : (sec/60)+":"+String.format(Locale.ROOT,"%02d",sec%60);
            Paint.clipped(g,time,x+123,row,37,Paint.accent());
        }
    }
}
