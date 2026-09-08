package dev.luma.visuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class LumaScreen extends Screen {
    private static final int W=480, H=296;
    private final Screen parent;
    private int tab;
    private float zoom=1;
    private float originX, originY;
    public LumaScreen(Screen parent) { super(Component.literal("LUMA Visuals")); this.parent=parent; }
    private LumaConfig config() { return LumaVisualsClient.config; }
    private void geometry() {
        zoom=Math.min(1f,Math.min(Math.max(1,width-16)/(float)W,Math.max(1,height-16)/(float)H));
        originX=(width-W*zoom)/2; originY=(height-H*zoom)/2;
    }
    private void button(int x,int y,int w,int h,Supplier<String> title,String desc,BooleanSupplier on,Runnable action) {
        addRenderableWidget(new LumaButton(x,y,w,h,title,desc,on,() -> { action.run(); config().save(); }));
    }
    @Override
    protected void init() {
        geometry(); clearWidgets();
        String[] tabs={"HUD", "Эффекты", "Стиль"};
        for(int i=0;i<tabs.length;i++) {
            final int t=i;
            button(14,76+i*37,94,30,() -> tabs[t],"",null,() -> { tab=t; init(); });
        }
        button(14,222,94,31,() -> "Расстановка","",null,() -> minecraft.setScreen(new HudEditorScreen(this)));
        button(425,15,39,24,() -> "ESC","",null,this::onClose);
        if(tab==0) {
            var modules=HudModule.values();
            for(int i=0;i<modules.length;i++) {
                HudModule module=modules[i];
                int column=i%2, row=i/2;
                button(126+column*171,101+row*49,162,43,() -> module.title,module.description,
                        () -> config().hud.get(module).enabled,
                        () -> config().hud.get(module).enabled=!config().hud.get(module).enabled);
            }
        } else if(tab==1) {
            button(126,101,333,41,() -> "Искры атаки","Частицы при попытке удара",() -> config().attackSparks,() -> config().attackSparks=!config().attackSparks);
            button(126,150,333,41,() -> "Кольцо прыжка","Цветная пыль у точки отрыва",() -> config().jumpRing,() -> config().jumpRing=!config().jumpRing);
            button(126,199,333,41,() -> "След движения","Частицы у ног, видны от третьего лица",() -> config().trail,() -> config().trail=!config().trail);
            button(126,247,333,22,() -> "Частиц: " + config().particleCount + "  ·  нажми, чтобы изменить","",null,
                    () -> config().particleCount=config().particleCount==8 ? 16 : config().particleCount==16 ? 24 : 8);
        } else {
            String[] ids={"mint","ice","lilac"};
            String[] names={"Mint","Ice","Lilac"};
            for(int i=0;i<3;i++) {
                final int n=i;
                button(126+i*114,101,105,32,() -> names[n],"",() -> config().palette.equals(ids[n]),() -> config().palette=ids[n]);
            }
            button(126,145,162,43,() -> String.format(Locale.ROOT,"Масштаб: %.0f%%",config().scale*100),"Изменить размер HUD",null,
                    () -> { float next=config().scale+.15f; config().scale=next>1.31f ? .85f : next; });
            button(297,145,162,43,() -> String.format(Locale.ROOT,"Плотность: %.0f%%",config().opacity*100),"Фон панелей",null,
                    () -> config().opacity=config().opacity<.75f ? .88f : config().opacity<.9f ? .96f : .68f);
            button(126,199,333,43,() -> "Меньше движения","Без анимации HP и частиц",() -> config().reducedMotion,() -> config().reducedMotion=!config().reducedMotion);
        }
    }
    @Override
    public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float delta) { /* Intentional unblurred overlay. */ }
    @Override
    public void render(GuiGraphics g,int mouseX,int mouseY,float delta) {
        geometry();
        g.fill(0,0,width,height,0xB0070E0B);
        g.pose().pushMatrix(); g.pose().translate(originX,originY); g.pose().scale(zoom,zoom);
        Paint.box(g,0,0,W,H,Paint.LINE); Paint.box(g,1,1,W-2,H-2,0xFF0D1612);
        g.fill(1,55,W-1,56,Paint.LINE); g.fill(115,56,116,H-23,Paint.LINE);
        Paint.logo(g,17,20,Paint.accent());
        Paint.heading(g,"LUMA",43,14,1.5f,Paint.TEXT);
        Paint.text(g,"VISUALS / ТВОЙ РИТМ.",44,33,Paint.MUTED);
        Paint.text(g,"КЛИЕНТСКИЙ МОД",291,26,Paint.MUTED);
        String[] titles={"Интерфейс", "Косметические эффекты", "Собственный стиль"};
        String[] hints={"Выбери, что оставить на экране.", "Только на твоём клиенте. Без изменения боя.", "Цвет, плотность и движение."};
        Paint.text(g,titles[tab],126,69,Paint.TEXT);
        Paint.text(g,hints[tab],126,84,Paint.MUTED);
        g.fill(9,76+tab*37,11,106+tab*37,Paint.accent());
        if(tab==0) Paint.text(g,"Положение блоков → Расстановка",126,254,Paint.MUTED);
        if(tab==2) Paint.text(g,"Настройки сохраняются автоматически.",126,255,Paint.MUTED);
        Paint.text(g,"0.1.0 / JAVA 1.21.11",14,281,Paint.MUTED);
        Paint.text(g,"FABRIC · CLIENTSIDE",348,281,Paint.MUTED);
        super.render(g,(int)((mouseX-originX)/zoom),(int)((mouseY-originY)/zoom),delta);
        g.pose().popMatrix();
    }
    private MouseButtonEvent local(MouseButtonEvent event) {
        return new MouseButtonEvent((event.x()-originX)/zoom,(event.y()-originY)/zoom,event.buttonInfo());
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent event,boolean doubled) { return super.mouseClicked(local(event),doubled); }
    @Override
    public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(local(event)); }
    @Override
    public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) { return super.mouseDragged(local(event),dx/zoom,dy/zoom); }
    @Override
    public void onClose() { config().save(); minecraft.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
