package dev.luma.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Reader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.EnumMap;

public final class LumaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOG = LoggerFactory.getLogger("LUMA Visuals");
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("luma-visuals.json");
    public int schema = 1;
    public String palette = "mint";
    public float scale = 1;
    public float opacity = .88f;
    public boolean reducedMotion = false;
    public boolean attackSparks = true;
    public boolean jumpRing = true;
    public boolean trail = false;
    public int particleCount = 16;
    public EnumMap<HudModule, Placement> hud = new EnumMap<>(HudModule.class);

    public static final class Placement {
        public boolean enabled = true;
        public double x;
        public double y;
        public Placement(double x, double y) { this.x = x; this.y = y; }
    }
    public LumaConfig() { resetLayout(); }
    public void resetLayout() {
        for (HudModule m : HudModule.values()) {
            Placement p = hud.get(m);
            if (p == null) hud.put(m, new Placement(m.defaultX, m.defaultY));
            else { p.x = m.defaultX; p.y = m.defaultY; }
        }
    }
    public int accent() {
        return switch (palette) {
            case "ice" -> 0xFF93C9F6;
            case "lilac" -> 0xFFC4B0EF;
            default -> 0xFF8BE9D2;
        };
    }
    public void normalize() {
        if (!"mint".equals(palette) && !"ice".equals(palette) && !"lilac".equals(palette)) palette = "mint";
        scale = (float) HudMath.clamp(scale, .75, 1.3);
        opacity = (float) HudMath.clamp(opacity, .65, .96);
        particleCount = (int) HudMath.clamp(particleCount, 6, 24);
        if (hud == null) hud = new EnumMap<>(HudModule.class);
        for (HudModule m : HudModule.values()) {
            if (hud.get(m) == null) hud.put(m, new Placement(m.defaultX, m.defaultY));
            Placement p = hud.get(m);
            p.x = HudMath.clamp(p.x, 0, 1);
            p.y = HudMath.clamp(p.y, 0, 1);
        }
    }
    public static LumaConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                LumaConfig config = GSON.fromJson(reader, LumaConfig.class);
                if (config == null) throw new IOException("Empty config");
                config.normalize();
                return config;
            } catch (IOException | RuntimeException ex) {
                LOG.warn("Invalid LUMA config; using defaults and preserving the original", ex);
                try {
                    Files.copy(PATH, PATH.resolveSibling("luma-visuals.broken-" + System.currentTimeMillis() + ".json"));
                } catch (IOException backupError) { LOG.warn("Could not back up LUMA config", backupError); }
            }
        }
        return new LumaConfig();
    }
    public void save() {
        normalize();
        Path temp = PATH.resolveSibling("luma-visuals.json.tmp");
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(temp, GSON.toJson(this), StandardCharsets.UTF_8);
            try { Files.move(temp, PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temp, PATH, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ex) { LOG.warn("Could not save LUMA config", ex); }
    }
}
