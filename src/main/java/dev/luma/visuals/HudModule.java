package dev.luma.visuals;

public enum HudModule {
    WATERMARK("Ватермарка", "LUMA / FPS / задержка", 238, 32, .025, .025),
    INFO("Навигация", "Координаты и скорость", 170, 49, .025, .20),
    KEYS("Клавиши", "Движение, прыжок, мышь", 90, 108, .025, .72),
    ARMOR("Броня", "Слоты и прочность", 130, 54, .975, .88),
    TARGET("Цель", "Только видимая цель", 182, 66, .88, .60),
    EFFECTS("Эффекты", "Активные зелья и время", 168, 85, .975, .22);

    public final String title;
    public final String description;
    public final int width;
    public final int height;
    public final double defaultX;
    public final double defaultY;
    HudModule(String title, String description, int width, int height, double x, double y) {
        this.title = title;
        this.description = description;
        this.width = width;
        this.height = height;
        this.defaultX = x;
        this.defaultY = y;
    }
}
