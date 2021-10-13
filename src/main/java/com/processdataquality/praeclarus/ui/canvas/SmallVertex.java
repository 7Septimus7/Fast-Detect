/*
 * Copyright (c) 2021 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.ui.canvas;

import com.processdataquality.praeclarus.workspace.node.Node;


import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Maximilian Harms
 * @date 24/08/21
 */
public class SmallVertex implements CanvasPrimitive {

    public static final double WIDTH = 85;
    public static final double HEIGHT = 35;
    public static final double CORNER_RADIUS = 10;

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private final int _id;

    private final double _x;
    private final double _y;
    private String _label;
    private final Node _node;
    private final CanvasPrimitive _parent;
    private final int _position;
    private final String _pattern;



    public SmallVertex(double x, double y, Node node, CanvasPrimitive parent, String pattern, int position) {
        this(x, y, node, parent, pattern, position, ID_GENERATOR.incrementAndGet());
    }


    public SmallVertex(double x, double y, Node node, CanvasPrimitive parent,String pattern, int position , int id) {
        _x = x;
        _y = y;
        _id = id;
        _label = node.getName();
        _node = node;
        _parent = parent;
        _pattern = pattern;
        _position = position;
    }

    public CanvasPrimitive getParent() {
        return _parent;
    }

    public Node getNode() {
        return _node;
    }

    public double x() { return _x; }

    public double y() { return  _y; }

    public int getID() { return _id; }

    public void setLabel(String label) { _label = label; }

    public String getLabel() { return _label; }

    public boolean isTrue() {
        return ((ExpandedVertex) _parent).getCheckboxes().get(_pattern)[_position];
    }

    public boolean contains(double pX, double pY) {
        return pX > _x && pX < _x + WIDTH && pY > _y && pY < _y + HEIGHT;
    }


    public void render(Context2D ctx, CanvasPrimitive selected) {
        ctx.strokeStyle("black");
        ctx.beginPath();
        renderVertex(ctx);
        if (this.equals(selected)) {
            ctx.fillStyle("#D0D0D0");
            ctx.fill();
        }
        ctx.stroke();

        renderLabel(ctx);
        renderCheckbox(ctx);

    }

    private void renderCheckbox(Context2D ctx) {
        VertexCheckbox vertexCheckbox = new VertexCheckbox(this, _x + WIDTH - 20, _y + HEIGHT/2 - VertexCheckbox.HEIGHT/2 - 1, ((ExpandedVertex) _parent).getCheckboxes().get(_pattern)[_position], _pattern, _position);
        vertexCheckbox.render(ctx, this);
        Set<VertexCheckbox> vertexCheckboxSet = ((ExpandedVertex) _parent).getVertexCheckbox();
        vertexCheckboxSet.add(vertexCheckbox);
        ((ExpandedVertex) _parent).setVertexCheckbox(vertexCheckboxSet);
    }


    private void renderVertex(Context2D ctx) {
        ctx.moveTo(_x + CORNER_RADIUS, _y);
        ctx.lineTo(_x + WIDTH - CORNER_RADIUS, _y);
        ctx.quadraticCurveTo(_x + WIDTH, _y, _x + WIDTH, _y + CORNER_RADIUS);
        ctx.lineTo(_x + WIDTH, _y + HEIGHT - CORNER_RADIUS);
        ctx.quadraticCurveTo(_x + WIDTH, _y + HEIGHT, _x + WIDTH - CORNER_RADIUS, _y + HEIGHT);
        ctx.lineTo(_x + CORNER_RADIUS, _y + HEIGHT);
        ctx.quadraticCurveTo(_x, _y + HEIGHT, _x, _y + HEIGHT - CORNER_RADIUS);
        ctx.lineTo(_x, _y + CORNER_RADIUS);
        ctx.quadraticCurveTo(_x, _y, _x + CORNER_RADIUS, _y);
        ctx.closePath();
    }


    private void renderLabel(Context2D ctx) {
        double innerX = _x + 5;
        double innerY = _y + 5;
        int fontSize = 10;
        double lineHeight=fontSize*1.286;


        ctx.textBaseline("top");
        ctx.font("10px Arial");
        ctx.beginPath();
        ctx.fillStyle("Black");
        for (String word : _label.split(" ")) {
            ctx.fillText(word, innerX, innerY, WIDTH - 20);
            innerY+=lineHeight;
        }
        ctx.stroke();
    }

}
