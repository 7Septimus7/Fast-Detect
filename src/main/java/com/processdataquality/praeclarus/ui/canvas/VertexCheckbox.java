package com.processdataquality.praeclarus.ui.canvas;

public class VertexCheckbox implements CanvasPrimitive{

    private final CanvasPrimitive parent;
    public static final double WIDTH = 13;
    public static final double HEIGHT = 13;
    private final double x;
    private final double y;
    private final boolean select;
    private final String pattern;
    private final int position;

    /**
     * @author Maximilian Harms
     * @date 28/08/21
     */
    public VertexCheckbox(CanvasPrimitive canvasPrimitive, double x, double y, boolean select, String pattern, int position) {
        parent = canvasPrimitive;
        this.x = x;
        this.y = y;
        this.select = select;
        this.pattern = pattern;
        this.position = position;
    }

    public CanvasPrimitive getParent() { return parent; }


    @Override
    public void render(Context2D ctx, CanvasPrimitive selected) {
        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.lineTo(x + WIDTH, y);
        ctx.lineTo(x + WIDTH, y + HEIGHT);
        ctx.lineTo(x, y + HEIGHT);
        ctx.lineTo(x, y);
        ctx.stroke();

        if (select) {
            ctx.beginPath();
            ctx.lineWidth(2);
            ctx.moveTo(x + 3, y + 3);
            ctx.lineTo(x + WIDTH - 3, y + HEIGHT - 3);

            ctx.moveTo(x + WIDTH - 3, y + 3);
            ctx.lineTo(x + 3, y + HEIGHT - 3);
            ctx.stroke();
            ctx.lineWidth(1);
        }
    }

    public boolean contains(double x, double y) {
            return this.x <= x && x <= (this.x + WIDTH) && this.y <= y && y <= this.y + HEIGHT;
    }

    public String getPattern() {
        return pattern;
    }

    public int getPosition() {
        return position;
    }
}
