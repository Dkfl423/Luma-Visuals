package dev.luma.visuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public final class LumaVisualsClient implements ClientModInitializer {
    public static final String ID = "luma_visuals";
    public static LumaConfig config;
    public static final VisualState state = new VisualState();
    private KeyMapping menuKey;

    @Override
    public void onInitializeClient() {
        config = LumaConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.luma_visuals.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(ID, "hud"), (graphics, tick) -> HudRenderer.render(graphics));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) {
                if (client.player != null && client.screen == null) client.setScreen(new LumaScreen(null));
            }
            state.tick(client);
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            Minecraft client = Minecraft.getInstance();
            if (player == client.player && world == client.level) state.attacked(client, entity);
            // Never cancel, automate or modify an attack. Rendering only.
            return InteractionResult.PASS;
        });
    }
}
