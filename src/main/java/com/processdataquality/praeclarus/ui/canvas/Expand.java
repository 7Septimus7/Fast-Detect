package com.processdataquality.praeclarus.ui.canvas;

public class Expand implements CanvasPrimitive{

    private final CanvasPrimitive parent;

    public Expand(CanvasPrimitive canvasPrimitive) {
        parent = canvasPrimitive;
    }

    public double x() {
        if (parent instanceof Vertex) {
            return ((Vertex) parent).x() + Vertex.WIDTH - 25;
        } else if (parent instanceof ExpandedVertex) {
            return ((ExpandedVertex) parent).x() + ExpandedVertex.WIDTH - 25;
        }
        return 0; //TODO
    }

    public double y() {
        if (parent instanceof Vertex) {
            return ((Vertex) parent).y() + 15;
        } else if (parent instanceof ExpandedVertex) {
            return ((ExpandedVertex) parent).y() + 15;
        }
        return 0; //TODO
    }

    public CanvasPrimitive getParent() { return parent; }


    @Override
    public void render(Context2D ctx, CanvasPrimitive selected) {
        String colour = "black";

        if (getParent() instanceof Vertex) {
            ctx.beginPath();
            ctx.moveTo(x(), y() - 4);
            ctx.lineTo(x() + 8, y() + 4);
            ctx.lineTo(x() + 16, y() - 4);
            ctx.stroke();
        } else if (getParent() instanceof ExpandedVertex) {
            ctx.beginPath();
            ctx.moveTo(x(), y());
            ctx.lineTo(x() + 8, y() - 8);
            ctx.lineTo(x() + 16, y());
            ctx.stroke();
        }
    }

    public boolean contains(double x, double y) {
        if(getParent() instanceof ExpandedVertex) {
            return x() <= x && x <= (x() + 16) && y() - 8 <= y && y <= y();
        } else if (getParent() instanceof Vertex){
            return x() <= x && x <= (x() + 16) && y() - 4 <= y && y <= y() + 4;
        }
        return false;
    }
}