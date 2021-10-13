package com.processdataquality.praeclarus.ui.canvas;

import com.processdataquality.praeclarus.action.FastDetect;
import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginService;
import com.processdataquality.praeclarus.ui.component.TreeData;
import com.processdataquality.praeclarus.ui.component.TreeItem;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeFactory;
import de.invation.code.toval.types.HashList;


import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpandedVertex implements CanvasPrimitive{

    public final Workflow _parent;

    public static final double WIDTH = 400;
    public double HEIGHT = setHeight();

    public static final double PATTERN_WIDTH = WIDTH - 20;
    public static final double PATTERN_HEIGHT = 45;
    public static final double CORNER_RADIUS = 10;
    public static final double LINE_WIDTH = 1;

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private final VertexStateIndicator _indicator = new VertexStateIndicator(this);
    private final int _id;
    private final Node _node;

    private double _x;
    private double _y;
    private String _label;
    private Port _inPort;
    private Port _outPort;
    private Point _dragOffset;
    private Expand _expand;
    private HashMap<String, Node> _smallNodes = setSmallNodes();

    private Set<SmallVertex> _smallVertices = new HashSet<>();
    private Set<VertexCheckbox> _vertexCheckbox = new HashSet<>();
    private HashMap<String, boolean[]> checkboxes = new HashMap<>();

    private static final TreeData _treeData = new TreeData();
    private static final List<TreeItem> _list = _treeData.getItems();

    /**
     * @author Maximilian Harms
     * @date 22/08/21
     */
    public ExpandedVertex(double x, double y, Node node, Workflow parent) {
        this(parent, x, y, node, ID_GENERATOR.incrementAndGet());
    }


    public ExpandedVertex(Workflow parent, double x, double y, Node node, int id) {
        _parent = parent;
        _x = x;
        _y = y;
        _id = id;
        _label = node.getName();
        _node = node;
        if (node.allowsInput()) {
            _inPort = new Port(this, Port.Style.INPUT);
        }
        if (node.allowsOutput()) {
            _outPort = new Port(this, Port.Style.OUTPUT);
        }
        if (node.allowsExpand()){
            _expand = new Expand(this);
        }
    }


    public double x() { return _x; }

    public double y() { return  _y; }

    public int getID() { return _id; }


    public void setLabel(String label) { _label = label; }

    public String getLabel() { return _label; }


    public Node getNode() { return _node; }


    public boolean contains(double pX, double pY) {
        return pX > _x && pX < _x + WIDTH && pY > _y && pY < _y + HEIGHT;
    }


    public Port getPortAt(double x, double y) {
        if (_inPort != null && _inPort.contains(x, y)) {
            return _inPort;
        }
        if (_outPort != null && _outPort.contains(x, y)) {
            return _outPort;
        }
        return null;
    }


    public Port getInputPort() { return _inPort; }

    public Port getOutputPort() { return _outPort; }

    public Expand getExpand() { return _expand; }

    public void setRunState(VertexStateIndicator.State state) {
        _indicator.setState(state);
    }

    public Set<SmallVertex> getSmallVertices() {
        return _smallVertices;
    }

    public void setDragOffset(double x, double y) {
        double dx = x - _x;
        double dy = y - _y;
        _dragOffset = new Point(dx, dy);
    }


    public void moveTo(double x, double y) {
        _x = x - _dragOffset.x;
        _y = y - _dragOffset.y;
    }

    public double setHeight() {
        double x = 40.0;
            for (TreeItem item : _list) {
                if (item.getParent() != null) {
                    if (item.getParent().getLabel() != null) {
                        if (item.getParent().getLabel().equals("Patterns") && item.getLabel() != null && getDetectChildren(item).size() > 0) {
                            int scaleSize = (getDetectChildren(item).size()/4) + 1;
                            x = x + scaleSize * PATTERN_HEIGHT + 25;
                        }
                    }
                }
            }
            return x;
    }

    private HashMap<String, Node> setSmallNodes() {
        HashMap<String, Node> smallNodes = new HashMap<>();
        for (TreeItem item : _list) {
            if (item.getParent() != null) {
                if (item.getParent().getLabel() != null) {
                    if (item.getParent().getLabel().equals("Patterns") && item.getLabel() != null && getDetectChildren(item).size() > 0) {
                        for (TreeItem child : getDetectChildren(item)) {
                            if (child.getLabel() != null) {
                                PDQPlugin instance = PluginService.patterns().newInstance(child.getName());
                                smallNodes.put(child.getLabel(), NodeFactory.create(instance));
                            }
                        }
                    }
                }
            }
        }
        return smallNodes;
    }

    public double getHeight() {
        return HEIGHT;
    }


    public void render(Context2D ctx, CanvasPrimitive selected) {
        _vertexCheckbox.clear();
        String colour = this.equals(selected) ? "blue" : "gray";
        ctx.strokeStyle("black");
        ctx.lineWidth(this.equals(selected) ? LINE_WIDTH * 3 : LINE_WIDTH);
        ctx.beginPath();
        renderExpandedVertex(ctx);
        if (this.equals(selected)) {
            ctx.fillStyle("#D0D0D0");
            ctx.fill();
        }
        ctx.stroke();
        _indicator.render(x(), y(), ctx);

        renderPorts(ctx, selected);
        renderLabel(ctx, colour);
        renderExpand(ctx, selected);
        renderSmallVertex(ctx);

        ((FastDetect) _node.getPlugin()).setDetectNodes(getSelectedNodes());
    }



    private void renderExpandedVertex(Context2D ctx) {
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


    private void renderExpand(Context2D ctx, CanvasPrimitive selected) {
        if (_expand != null) {
            _expand.render(ctx, selected);
        }
    }


    private void renderPorts(Context2D ctx, CanvasPrimitive selected) {
        if (_inPort != null) {
            _inPort.render(ctx, selected);
        }
        if (_outPort != null) {
            _outPort.render(ctx, selected);
        }
    }


    private void renderLabel(Context2D ctx, String colour) {
        double innerX = _x + 5;
        double innerY = _y + 5;

        ctx.textBaseline("top");
        ctx.font("14px Arial");
        ctx.beginPath();
        ctx.fillStyle(colour);
        ctx.fillText(_label, innerX, innerY, WIDTH - 20);

        ctx.stroke();
    }

    private void renderSmallVertex(Context2D ctx) {
        _smallVertices.clear();
        double lineX = x() + 10;
        double lineY = y() + 25;
        double borderX = x() + 10;
        double borderY = y() + 40;
        double vertexX = borderX +5;
        double vertexY = borderY +5;

        for (TreeItem item : _list) {
            if (item.getParent() != null) {
                if (item.getParent().getLabel() != null) {
                    if (item.getParent().getLabel().equals("Patterns") && item.getLabel() != null && getDetectChildren(item).size()>0) {
                        int scaleSize = (getDetectChildren(item).size()/4) + 1;
                        if (!checkboxes.containsKey(item.getLabel())) {
                            boolean[] checkbox = new boolean[getDetectChildren(item).size() + 1];
                            for (int i = 0; i < getDetectChildren(item).size() + 1; i++) {
                                checkbox[i] = false;
                            }
                            checkboxes.put(item.getLabel(), checkbox);
                        }
                        renderPatternContainer(item.getLabel(), lineX, lineY, borderX, borderY, scaleSize, item.getLabel(), ctx);
                        int i = 0;
                        for (TreeItem child : getDetectChildren(item)) {
                            i++;
                            if (i % 5 == 0) {
                                vertexY = vertexY + PATTERN_HEIGHT;
                                vertexX = borderX + 5;
                            }
                            if (child.getLabel() != null) {
                                SmallVertex smallVertex = new SmallVertex(vertexX, vertexY, _smallNodes.get(child.getLabel()), this, item.getLabel(), i);
                                smallVertex.render(ctx, this);
                                if(!isInSmallVertexSet(smallVertex)) {
                                    _smallVertices.add(smallVertex);
                                }
                                vertexX = vertexX + SmallVertex.WIDTH + 10;
                            }
                        }
                        lineY = lineY + scaleSize * PATTERN_HEIGHT + 25;
                        borderY = borderY + scaleSize * PATTERN_HEIGHT + 25;
                        vertexY = vertexY + PATTERN_HEIGHT + 25;
                        vertexX = borderX + 5;
                    }
                }
            }
        }
    }


    private boolean isInSmallVertexSet(SmallVertex smallVertex) {
        if (_smallVertices.size() > 0) {
            for (SmallVertex smallVertex1 : _smallVertices) {
                if (smallVertex1.equals(smallVertex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void renderPatternContainer(String label, Double lineX, Double lineY, Double borderX, Double borderY,  int scaleSize, String pattern, Context2D ctx) {
        ctx.textBaseline("top");
        ctx.font("12px Arial");
        ctx.beginPath();
        ctx.fillStyle("Black");
        ctx.fillText(label, lineX, lineY, WIDTH - 20);
        ctx.stroke();

        renderBorder(borderX, borderY, scaleSize, ctx);
        VertexCheckbox vertexCheckbox = new VertexCheckbox(this, lineX + WIDTH - 38, lineY - 3, checkboxes.get(pattern)[0], pattern, 0);
        vertexCheckbox.render(ctx, this);
        _vertexCheckbox.add(vertexCheckbox);
    }

    private void renderBorder(Double borderX, Double borderY, int scaleSize, Context2D ctx) {
        ctx.beginPath();
        ctx.moveTo(borderX + CORNER_RADIUS, borderY);
        ctx.lineTo(borderX + PATTERN_WIDTH - CORNER_RADIUS, borderY);
        ctx.quadraticCurveTo(borderX + PATTERN_WIDTH, borderY, borderX + PATTERN_WIDTH, borderY + CORNER_RADIUS);
        ctx.lineTo(borderX + PATTERN_WIDTH, borderY + scaleSize * PATTERN_HEIGHT - CORNER_RADIUS);
        ctx.quadraticCurveTo(borderX + PATTERN_WIDTH, borderY + scaleSize * PATTERN_HEIGHT, borderX + PATTERN_WIDTH - CORNER_RADIUS, borderY + scaleSize * PATTERN_HEIGHT);
        ctx.lineTo(borderX + CORNER_RADIUS, borderY + scaleSize * PATTERN_HEIGHT);
        ctx.quadraticCurveTo(borderX, borderY + scaleSize * PATTERN_HEIGHT, borderX, borderY + scaleSize * PATTERN_HEIGHT - CORNER_RADIUS);
        ctx.lineTo(borderX, borderY + CORNER_RADIUS);
        ctx.quadraticCurveTo(borderX, borderY, borderX + CORNER_RADIUS, borderY);
        ctx.stroke();
        ctx.closePath();
    }

    private  List<TreeItem> getDetectChildren(TreeItem item){
        List<TreeItem> detectChildren = new HashList<>();
        if (item.getParent() != null) {
            if (item.getParent().getLabel() != null) {
                if (item.getParent().getLabel().equals("Patterns") && item.getLabel() != null && _treeData.getChildItems(item).size() > 0) {
                    for (TreeItem treeItem : _treeData.getChildItems(item)){
                        ImperfectionPattern instance = PluginService.patterns().newInstance(treeItem.getName());
                        if (instance != null) {
                            if (instance.canDetect()) {
                                detectChildren.add(treeItem);
                            }
                        }
                    }
                }
            }
        }
        return detectChildren;
    }

    public Set<Node> getSelectedNodes() {
        Set<Node> nodesToRun = new HashSet<>();
        for (SmallVertex smallVertex : _smallVertices) {
            if (smallVertex.isTrue()) {
                nodesToRun.add(smallVertex.getNode());
            }
        }
        return nodesToRun;
    }

    public HashMap<String, boolean[]> getCheckboxes() {
        return checkboxes;
    }

    public void setCheckboxes(HashMap<String, boolean[]> checkboxes) {
            this.checkboxes = checkboxes;
    }

    public HashMap<String, Node> getSmallNodes() {
        return _smallNodes;
    }

    public void setSmallNodes(HashMap<String, Node> _smallNodes) {
        this._smallNodes = _smallNodes;
    }

    public Set<VertexCheckbox> getVertexCheckbox() {
        return _vertexCheckbox;
    }

    public void setVertexCheckbox(Set<VertexCheckbox> _vertexCheckbox) {
        this._vertexCheckbox = _vertexCheckbox;
    }
}
