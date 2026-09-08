package dev.luma.visuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudModule dragging;
    private double grabX,grabY;
    public HudEditorScreen(Screen parent) { super(Component.literal("Расстановка LUMA HUD")); this.parent=parent; }
    @Override
    protected void init() {
        addRenderableWidget(new LumaButton(width/2-79,height-27,74,22,() -> "Готово","",null,this::onClose));
        addRenderableWidget(new LumaButton(width/2+5,height-27,74,22,() -> "Сброс","",null,() -> {
            LumaVisualsClient.config.resetLayout(); LumaVisualsClient.config.save();
        }));
    }
    @Override
    public void renderBackground(GuiGraphics g,int mx,int my,float delta) { }
    @Override
    public void render(GuiGraphics g,int mx,int my,float delta) {
        g.fill(0,0,width,height,0x5F07120C);
        HudRenderer.draw(g,width,height,true);
        float s=HudRenderer.scale(width,height);
        g.pose().pushMatrix(); g.pose().scale(s,s);
        for(var entry:HudRenderer.layout(width,height).entrySet()) {
            if(!LumaVisualsClient.config.hud.get(entry.getKey()).enabled) continue;
            var b=entry.getValue();
            if(entry.getKey()==dragging || b.contains(mx/s,my/s)) {
                int color=Paint.accent();
                g.fill(b.x(),b.y(),b.x()+b.width(),b.y()+1,color);
                g.fill(b.x(),b.y()+b.height()-1,b.x()+b.width(),b.y()+b.height(),color);
                g.fill(b.x(),b.y(),b.x()+1,b.y()+b.height(),color);
                g.fill(b.x()+b.width()-1,b.y(),b.x()+b.width(),b.y()+b.height(),color);
            }
        }
        g.pose().popMatrix();
        super.render(g,mx,my,delta);
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent event,boolean doubled) {
        if(super.mouseClicked(event,doubled)) return true;
        if(event.button()!=0) return false;
        float s=HudRenderer.scale(width,height);
        var layout=HudRenderer.layout(width,height);
        var modules=HudModule.values();
        for(int i=modules.length-1;i>=0;i--) {
            HudModule m=modules[i]; var b=layout.get(m);
            if(LumaVisualsClient.config.hud.get(m).enabled && b.contains(event.x()/s,event.y()/s)) {
                dragging=m; grabX=event.x()/s-b.x(); grabY=event.y()/s-b.y(); return true;
            }
        }
        return false;
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) {
        if(dragging==null) return super.mouseDragged(event,dx,dy);
        float s=HudRenderer.scale(width,height);
        var p=LumaVisualsClient.config.hud.get(dragging);
        p.x=HudMath.normalized(event.x()/s-grabX,(int)(width/s),dragging.width);
        p.y=HudMath.normalized(event.y()/s-grabY,(int)(height/s),dragging.height);
        return true;
    }
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if(dragging!=null) { dragging=null; LumaVisualsClient.config.save(); return true; }
        return super.mouseReleased(event);
    }
    @Override
    public void onClose() { dragging=null; LumaVisualsClient.config.save(); minecraft.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
