package dev.luma.visuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** A keyboard-focusable, narrated button using the 1.21.11 event-object API. */
public final class LumaButton extends AbstractWidget {
    private final Supplier<String> label;
    private final String description;
    private final BooleanSupplier selected;
    private final Runnable action;
    public LumaButton(int x, int y, int width, int height, Supplier<String> label,
                      String description, BooleanSupplier selected, Runnable action) {
        super(x, y, width, height, Component.literal(label.get()));
        this.label=label; this.description=description; this.selected=selected; this.action=action;
    }
    @Override
    protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
        boolean on = selected != null && selected.getAsBoolean();
        boolean hovered = isHoveredOrFocused();
        int x=getX(), y=getY(), w=getWidth(), h=getHeight();
        Paint.box(g,x,y,w,h,hovered ? 0xFF38574B : (on ? 0xFF304C40 : Paint.LINE));
        Paint.box(g,x+1,y+1,w-2,h-2,on ? 0xFF192E25 : (hovered ? 0xFF202F29 : Paint.SURFACE));
        int reserve = selected == null ? 18 : 42;
        int titleY = description.isEmpty() ? y+(h-8)/2 : y+9;
        Paint.clipped(g,label.get(),x+9,titleY,w-reserve,on ? Paint.accent() : Paint.TEXT);
        if (!description.isEmpty()) Paint.clipped(g,description,x+9,y+25,w-18,Paint.MUTED);
        if (selected != null) {
            int sy = description.isEmpty() ? y+(h-10)/2 : y+8;
            Paint.box(g,x+w-29,sy,20,10,on ? Paint.accent() : 0xFF4C5D55);
            Paint.box(g,x+w-(on ? 19 : 27),sy+2,6,6,on ? 0xFF0F241B : 0xFFD5DED8);
        }
        if (isFocused()) g.fill(x+8,y+h-3,x+w-8,y+h-2,Paint.accent());
        setMessage(Component.literal(label.get() + (selected == null ? "" : (on ? ", включено" : ", выключено"))));
    }
    @Override
    public void onClick(MouseButtonEvent event, boolean doubled) { action.run(); }
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (active && visible && (event.key()==GLFW.GLFW_KEY_ENTER || event.key()==GLFW.GLFW_KEY_SPACE || event.key()==GLFW.GLFW_KEY_KP_ENTER)) {
            action.run(); return true;
        }
        return super.keyPressed(event);
    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) { defaultButtonNarrationText(out); }
}
