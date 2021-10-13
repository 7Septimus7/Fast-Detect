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

import com.processdataquality.praeclarus.ui.component.PipelinePanel;
import com.processdataquality.praeclarus.ui.component.VertexLabelDialog;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeRunnerListener;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.*;

/**
 * @author Michael Adams
 * @date 19/5/21
 */
public class Workflow implements CanvasEventListener, NodeRunnerListener {

    private enum State { VERTEX_DRAG, ARC_DRAW, NONE }

    private final PipelinePanel _parent;
    private final Context2D _ctx;
    private final Set<Vertex> _vertices = new HashSet<>();
    private final Set<Connector> _connectors = new HashSet<>();
    private final Set<Expand> _expand = new HashSet<>();
    private final Set<ExpandedVertex> _expandedVertices = new HashSet<>();
    private final Set<SmallVertex> _smallVertices = new HashSet<>();
    private final Set<VertexCheckbox> _vertexCheckbox = new HashSet<>();

    private ActiveLine activeLine;
    private CanvasPrimitive selected;
    private State state = State.NONE;
    private boolean _loading = false;

    HashMap<Integer, Map<String, boolean[]>> expandedVertexCheckboxes = new HashMap<>();
    HashMap<Integer, Map<String, Node>> expandedSmallNodes = new HashMap<>();



    public Workflow(PipelinePanel parent, Context2D context) {
        _parent = parent;
        _ctx = context;
        _parent.getWorkspace().getRunner().addListener(this);
    }

    @Override
    public void mouseDown(double x, double y) {
        Port port = getPortAt(x, y);
        if (port != null && port.isOutput()) {
            activeLine = new ActiveLine(x, y);
            state = State.ARC_DRAW;
        }
        else {
            Vertex vertex = getVertexAt(x, y);
            ExpandedVertex expandedVertex = getExpandedVertexAt(x, y);
            if (vertex != null) {
                selected = vertex;
                vertex.setDragOffset(x, y);
                state = State.VERTEX_DRAG;
            } else if (expandedVertex != null) {
                selected = expandedVertex;
                expandedVertex.setDragOffset(x, y);
                state = State.VERTEX_DRAG;
            }
        }
    }

    @Override
    public void mouseMove(double x, double y) {
        if (state == State.NONE) return;

        if (state == State.ARC_DRAW) {
            render();
            activeLine.lineTo(_ctx, x, y);
        }
        else if (state == State.VERTEX_DRAG) {
            if (selected instanceof Vertex) {
                ((Vertex) selected).moveTo(x, y);
            } else if (selected instanceof ExpandedVertex) {
                ((ExpandedVertex) selected).moveTo(x, y);
            }
            render();
        }
    }

    @Override
    public void mouseUp(double x, double y) {
        if (state == State.NONE) return;

        if (state == State.ARC_DRAW) {
            Point start = activeLine.getStart();
            Port source = getPortAt(start.x, start.y);
            Port target = getPortAt(x, y);
            if (source != null && target != null) {
                if (source.isOutput() && target.isInput()) {
                    addConnector(new Connector(source, target));
                }
                else {
                    Notification.show("Output port cannot be the target of a connection");
                }
            }
        }

        state = State.NONE;     // if dragging nothing more to do
        render();
    }

    @Override
    public void mouseClick(double x, double y) {
        selected = setSelected(x, y);
        expand();
        if(selected instanceof VertexCheckbox) {
            changeCheckbox();
        }
        if(selected instanceof SmallVertex) {
            _parent.changedSelectedSmallVertex(getSelectedNode());
        }
        _parent.changedSelected(getSelectedNode());
        render();
    }

    @Override
    public void mouseDblClick(double x, double y) {
        mouseClick(x, y);
        Vertex selectedVertex = getSelectedVertex();
        if (selectedVertex != null) {
            new VertexLabelDialog(this, selectedVertex).open();
        }
    }

    @Override
    public void fileLoaded(String jsonStr) {
        WorkflowLoader loader = new WorkflowLoader(this, _parent.getWorkspace());
        try {
            loader.load(jsonStr);
        }
        catch (JSONException je) {
            Notification.show("Failed to load file: " + je.getMessage());
        }
    }


    @Override
    public void nodeStarted(Node node) {
        changeStateIndicator(node, VertexStateIndicator.State.RUNNING);
    }


    @Override
    public void nodeCompleted(Node node) {
        changeStateIndicator(node, VertexStateIndicator.State.COMPLETED);
    }


    @Override
    public void nodeRollback(Node node) {
        changeStateIndicator(node, VertexStateIndicator.State.DORMANT);
    }


    @Override
    public void nodePaused(Node node) { }

    public void clear() {
        _vertices.clear();
        _connectors.clear();
        _expand.clear();
        _expandedVertices.clear();
        render();
    }


    public void setLoading(boolean b) {
        _loading = b;
        if (! _loading) render();
    }


    public boolean hasContent() {
        return ! _vertices.isEmpty();
    }


    public CanvasPrimitive getSelected() { return selected; }

    public void setSelected(CanvasPrimitive primitive) {
        selected = primitive;
        render();
    }

    public Node getSelectedNode() {
        Vertex vertex = getSelectedVertex();
        SmallVertex smallVertex = getSelectedSmallVertex();
        ExpandedVertex expandedVertex = getSelectedExpandedVertex();
        if (vertex != null) {
            return vertex.getNode();
        } else if (smallVertex != null) {
            return smallVertex.getNode();
        } else if (expandedVertex != null) {
            return expandedVertex.getNode();
        }
        return null;
    }

    private SmallVertex getSelectedSmallVertex() {
        return selected instanceof SmallVertex ? ((SmallVertex) selected) : null;
    }

    public Vertex getSelectedVertex() {
        return selected instanceof Vertex ? ((Vertex) selected) : null;
    }

    public ExpandedVertex getSelectedExpandedVertex() {
        return selected instanceof ExpandedVertex ? ((ExpandedVertex) selected) : null;
    }


    public void setSelectedNode(Node node) {
        for (Vertex vertex : _vertices) {
            if (vertex.getNode().equals(node)) {
                setSelected(vertex);
                _parent.changedSelected(node);
                break;
            }
        }
        for (ExpandedVertex expandedVertex : _expandedVertices) {
            if (expandedVertex.getNode().equals(node)) {
                setSelected(expandedVertex);
                _parent.changedSelected(node);
                break;
            }
        }
        for (SmallVertex smallVertex : _smallVertices) {
            if (smallVertex.getNode().equals(node)) {
                setSelected(smallVertex);
                _parent.changedSelected(node);
                break;
            }
        }
    }


     public void removeSelected() {
        if (selected instanceof Vertex) {
            removeVertex((Vertex) selected);
            selected = null;
        }
        else if (selected instanceof Connector) {
            removeConnector((Connector) selected);
            selected = null;
        }
        else if (selected instanceof ExpandedVertex) {
            removeExpandedVertex((ExpandedVertex) selected);
            selected = null;
        }
        render();
    }



    public void addVertex(Vertex vertex) {
        _vertices.add(vertex);
        selected = vertex;
        render();
    }


    public void addVertex(Node node) {
        Point p = getSuitableInsertPoint();
        addVertex(new Vertex(p.x, p.y, node));
    }

    public void addVertex(Node node, Point p) {
        addVertex(new Vertex(p.x, p.y, node));
    }


    public void removeVertex(Vertex vertex) {
        if (vertex != null) {
            _vertices.remove(vertex);
            removeConnectors(vertex);
        }
    }


    private void addExpandedVertex(Node node, Double x, Double y) {
        ExpandedVertex expandedVertex = new ExpandedVertex(x, y, node, this);
        if (!(expandedVertexCheckboxes.get(expandedVertex.getNode().getId()) == null)) {
            expandedVertex.setCheckboxes((HashMap<String, boolean[]>) expandedVertexCheckboxes.get(expandedVertex.getNode().getId()));
        }
        if(!(expandedVertexCheckboxes.get(expandedVertex.getNode().getId()) == null)) {
            expandedVertex.setSmallNodes((HashMap<String, Node>) expandedSmallNodes.get(expandedVertex.getNode().getId()));
        }
        addExpandedVertex(expandedVertex);
    }

    private void addExpandedVertex(ExpandedVertex expandedVertex) {
        _expandedVertices.add(expandedVertex);
        selected = expandedVertex;
        _smallVertices.addAll(expandedVertex.getSmallVertices());
    }

    private void removeExpandedVertex(ExpandedVertex expandedVertex) {
        if (expandedVertex != null) {
            expandedVertexCheckboxes.put(expandedVertex.getNode().getId(), expandedVertex.getCheckboxes());
            expandedSmallNodes.put(expandedVertex.getNode().getId(), expandedVertex.getSmallNodes());
            _expandedVertices.remove(expandedVertex);
            Set<SmallVertex> smallVertices = expandedVertex.getSmallVertices();
            for (SmallVertex smallVertex : smallVertices){
                _smallVertices.remove(smallVertex);
            }
            removeConnectors(expandedVertex);
        }
    }


    public void addConnector(Connector c) {
        _connectors.add(c);
        if (c.getSource() instanceof Vertex && c.getTarget() instanceof Vertex) {
            Node source = ((Vertex) c.getSource()).getNode();
            Node target = ((Vertex) c.getTarget()).getNode();
            _parent.getWorkspace().connect(source, target);
            render();
        } else if (c.getSource() instanceof Vertex && c.getTarget() instanceof ExpandedVertex) {
            Node source = ((Vertex) c.getSource()).getNode();
            Node target = ((ExpandedVertex) c.getTarget()).getNode();
            _parent.getWorkspace().connect(source, target);
            render();
        } else if (c.getSource() instanceof ExpandedVertex && c.getTarget() instanceof Vertex) {
            Node source = ((ExpandedVertex) c.getSource()).getNode();
            Node target = ((Vertex) c.getTarget()).getNode();
            _parent.getWorkspace().connect(source, target);
            render();
        } else if (c.getSource() instanceof ExpandedVertex && c.getTarget() instanceof ExpandedVertex) {
            Node source = ((ExpandedVertex) c.getSource()).getNode();
            Node target = ((ExpandedVertex) c.getTarget()).getNode();
            _parent.getWorkspace().connect(source, target);
            render();
        }
    }

    public void removeConnector(Connector c) {
        boolean success = _connectors.remove(c);
        if (success) {
            if (c.getSource() instanceof Vertex && c.getTarget() instanceof Vertex) {
                Node previous = ((Vertex) c.getSource()).getNode();
                Node next = ((Vertex) c.getTarget()).getNode();
                _parent.getWorkspace().disconnect(previous, next);
                render();
            } else if (c.getSource() instanceof Vertex && c.getTarget() instanceof ExpandedVertex) {
                Node previous = ((Vertex) c.getSource()).getNode();
                Node next = ((ExpandedVertex) c.getTarget()).getNode();
                _parent.getWorkspace().disconnect(previous, next);
                render();
            } else if (c.getSource() instanceof ExpandedVertex && c.getTarget() instanceof Vertex) {
                Node previous = ((ExpandedVertex) c.getSource()).getNode();
                Node next = ((Vertex) c.getTarget()).getNode();
                _parent.getWorkspace().disconnect(previous, next);
                render();
            } else if (c.getSource() instanceof ExpandedVertex && c.getTarget() instanceof ExpandedVertex) {
                Node previous = ((ExpandedVertex) c.getSource()).getNode();
                Node next = ((ExpandedVertex) c.getTarget()).getNode();
                _parent.getWorkspace().disconnect(previous, next);
                render();
            }
        }
    }

    public void addExpand(Node n) {
        for (Vertex v : _vertices) {
            if (v.getNode() == n){
                _expand.add(v.getExpand());
            }
        }
        for (ExpandedVertex expandedVertex : _expandedVertices) {
            if (expandedVertex.getNode() == n){
                _expand.add(expandedVertex.getExpand());
            }
        }
    }

    public boolean removeExpand(Vertex v) {
        return _expand.remove(v.getExpand());
    }

    public boolean removeExpand(ExpandedVertex expandedVertex) {
        return _expand.remove(expandedVertex.getExpand());
    }


    public JSONObject asJson() throws JSONException {
        JSONArray vertexArray = new JSONArray();
        for (Vertex vertex : _vertices) {
            vertexArray.put(vertex.asJson());
        }
        JSONArray connectorArray = new JSONArray();
        for (Connector connector : _connectors) {
            connectorArray.put(connector.asJson());
        }
        JSONObject json = new JSONObject();
        json.put("vertices", vertexArray);
        json.put("connectors", connectorArray);
        return json;
    }
    

    private void removeConnectors(Vertex vertex) {
        Set<Connector> removeSet = new HashSet<>();
        for (Connector c : _connectors) {
            if (c.connects(vertex)) {
                removeSet.add(c);
            }
        }
        for (Connector c : removeSet) {
            removeConnector(c);
        }
        render();
    }

    private void removeConnectors(ExpandedVertex expandedVertex) {
        Set<Connector> removeSet = new HashSet<>();
        for (Connector c : _connectors) {
            if (c.connects(expandedVertex)) {
                removeSet.add(c);
            }
        }
        for (Connector c : removeSet) {
            removeConnector(c);
        }
    }


    private Port getPortAt(double x, double y) {
        for (Vertex vertex : _vertices) {
            Port port = vertex.getPortAt(x, y);
             if (port != null) {
                 return port;
             }
        }
        for (ExpandedVertex expandedVertex : _expandedVertices) {
            Port port = expandedVertex.getPortAt(x, y);
            if (port != null) {
                return port;
            }
        }
        return null;
    }

    private Vertex getVertexAt(double x, double y) {
        for (Vertex vertex : _vertices) {
            if (vertex.contains(x, y)) {
                return vertex;
            }
        }
        return null;
    }

    private ExpandedVertex getExpandedVertexAt(double x, double y) {
        for (ExpandedVertex expandedVertex : _expandedVertices) {
            if (expandedVertex.contains(x, y)) {
                return expandedVertex;
            }
        }
        return null;
    }

    private SmallVertex getSmallVertexAt(double x, double y) {
        for (SmallVertex smallVertex : _smallVertices) {
            if (smallVertex.contains(x, y)) {
                return smallVertex;
            }
        }
        return null;
    }

    private VertexCheckbox getVertexCheckboxAt(double x, double y) {
        for (VertexCheckbox vertexCheckbox : _vertexCheckbox) {
            if (vertexCheckbox.contains(x, y)) {
                return vertexCheckbox;
            }
        }
        return null;
    }

    private void changeCheckbox() {
        if (((VertexCheckbox) selected).getParent() instanceof ExpandedVertex) {
            HashMap<String, boolean[]> checkboxes = ((ExpandedVertex) ((VertexCheckbox) selected).getParent()).getCheckboxes();
            boolean[] checkboxBoolean = checkboxes.get(((VertexCheckbox) selected).getPattern());
            if (((VertexCheckbox) selected).getPosition() == 0) {
                if (checkboxBoolean[0]) {
                    Arrays.fill(checkboxBoolean, false);
                } else if (!checkboxBoolean[((VertexCheckbox) selected).getPosition()]) {
                    Arrays.fill(checkboxBoolean, true);
                }
            }
            checkboxes.put(((VertexCheckbox) selected).getPattern(), checkboxBoolean);
            ((ExpandedVertex) ((VertexCheckbox) selected).getParent()).setCheckboxes(checkboxes);
        } else if (((VertexCheckbox) selected).getParent() instanceof SmallVertex) {
            HashMap<String, boolean[]> checkboxes = ((ExpandedVertex) ((SmallVertex) ((VertexCheckbox) selected).getParent()).getParent()).getCheckboxes();
            boolean[] checkboxBoolean = checkboxes.get(((VertexCheckbox) selected).getPattern());

            if (checkboxBoolean[((VertexCheckbox) selected).getPosition()]) {
                checkboxBoolean[((VertexCheckbox) selected).getPosition()] = false;
            } else if (!checkboxBoolean[((VertexCheckbox) selected).getPosition()]) {
                checkboxBoolean[((VertexCheckbox) selected).getPosition()] = true;
            }
            boolean headMustTrue = true;
            for (int i = 1; i < checkboxBoolean.length; i++) {
                if (!checkboxBoolean[i]) {
                    headMustTrue = false;
                    break;
                }
            }
            if (headMustTrue) {
                checkboxBoolean[0] = true;
            }
            boolean headMustFalse = false;
            for (int i = 1; i < checkboxBoolean.length; i++) {
                if (!checkboxBoolean[i]) {
                    headMustFalse = true;
                    break;
                }
            }
            if (headMustFalse) {
                checkboxBoolean[0] = false;
            }

            checkboxes.put(((VertexCheckbox) selected).getPattern(), checkboxBoolean);
            ((ExpandedVertex) ((SmallVertex) ((VertexCheckbox) selected).getParent()).getParent()).setCheckboxes(checkboxes);
        }
    }

    private void expand() {
        if (selected instanceof Expand){
            if (((Expand) selected).getParent() instanceof Vertex) {
                Set<Connector> cloneConnectors = cloneConnectorSet();
                CanvasPrimitive storage = ((Expand) selected).getParent();
                removeExpandedConnectors(storage);
                _expand.remove((Expand) selected);
                addExpandedVertex(((Vertex) ((Expand) selected).getParent()).getNode(), ((Vertex) ((Expand) selected).getParent()).x(), ((Vertex) ((Expand) selected).getParent()).y());
                transformConnection(storage, selected, cloneConnectors);
                removeVertex((Vertex) storage);
                if (selected instanceof ExpandedVertex){
                    addExpand(((ExpandedVertex) selected).getNode());
                }
            } else if (((Expand) selected).getParent() instanceof ExpandedVertex) {
                Set<Connector> cloneConnectors = cloneConnectorSet();
                CanvasPrimitive storage = ((Expand) selected).getParent();
                removeExpandedConnectors(storage);
                _expand.remove((Expand) selected);
                addVertex(((ExpandedVertex) ((Expand) selected).getParent()).getNode() , new Point(((ExpandedVertex) ((Expand) selected).getParent()).x(), ((ExpandedVertex) ((Expand) selected).getParent()).y()));
                transformConnection(storage, selected, cloneConnectors);
                removeExpandedVertex((ExpandedVertex) storage);
                if (selected instanceof Vertex){
                    addExpand(((Vertex) selected).getNode());
                }
            }
        }
    }

    private Set<Connector> cloneConnectorSet() {
        return new HashSet<>(_connectors);
    }

    private void removeExpandedConnectors(CanvasPrimitive selectedParent){
        Set<Connector> clone = cloneConnectorSet();
        for (Connector c : clone) {
            if (selectedParent instanceof Vertex) {
                if (c.connects((Vertex) selectedParent)) {
                    removeConnector(c);
                }
            } else if (selectedParent instanceof ExpandedVertex) {
                if (c.connects((ExpandedVertex) selectedParent)) {
                    removeConnector(c);
                }
            }
        }
    }

    private void transformConnection(CanvasPrimitive selectedParent, CanvasPrimitive selected, Set<Connector> cloneConnectors) {
        if(selectedParent instanceof Vertex && selected instanceof ExpandedVertex) {
            for (Connector c : cloneConnectors) {
                if (c.connects((Vertex) selectedParent)) {
                    if (c.getSource().equals(selectedParent) && c.getTarget() instanceof Vertex) {
                        addConnector(new Connector(((ExpandedVertex) selected).getOutputPort(), ((Vertex) c.getTarget()).getInputPort()));
                    } else if (c.getSource().equals(selectedParent) && c.getTarget() instanceof ExpandedVertex) {
                        addConnector(new Connector(((ExpandedVertex) selected).getOutputPort(), ((ExpandedVertex) c.getTarget()).getInputPort()));
                    } else if (c.getTarget().equals(selectedParent) && c.getSource() instanceof Vertex) {
                        addConnector(new Connector(((Vertex) c.getSource()).getOutputPort(), ((ExpandedVertex) selected).getInputPort()));
                    } else if (c.getTarget().equals(selectedParent) && c.getSource() instanceof ExpandedVertex) {
                        addConnector(new Connector(((ExpandedVertex) c.getSource()).getOutputPort(), ((ExpandedVertex) selected).getInputPort()));
                    }
                }
            }
        } else if(selectedParent instanceof ExpandedVertex && selected instanceof Vertex) {
            for (Connector c : cloneConnectors) {
                if (c.connects((ExpandedVertex) selectedParent)) {
                    if (c.getSource().equals(selectedParent) && c.getTarget() instanceof Vertex) {
                        addConnector(new Connector(((Vertex) selected).getOutputPort(), ((Vertex) c.getTarget()).getInputPort()));
                    } else if (c.getSource().equals(selectedParent) && c.getTarget() instanceof ExpandedVertex) {
                        addConnector(new Connector(((Vertex) selected).getOutputPort(), ((ExpandedVertex) c.getTarget()).getInputPort()));
                    } else if (c.getTarget().equals(selectedParent) && c.getSource() instanceof Vertex) {
                        addConnector(new Connector(((Vertex) c.getSource()).getOutputPort(), ((Vertex) selected).getInputPort()));
                    } else if (c.getTarget().equals(selectedParent) && c.getSource() instanceof ExpandedVertex) {
                        addConnector(new Connector(((ExpandedVertex) c.getSource()).getOutputPort(), ((Vertex) selected).getInputPort()));
                    }
                }
            }
        }
    }


    public void render() {
        if (_loading) return;                 // don't render while loading from file
        _ctx.clear();
        for (Vertex vertex : _vertices) {
            vertex.render(_ctx, selected);
        }
        for (Connector connector : _connectors) {
            connector.render(_ctx, selected);
        }
        for (ExpandedVertex expandedVertex : _expandedVertices) {
            for (SmallVertex smallVertex : cloneSmallVertices()) {
                if (smallVertex.getParent().equals(expandedVertex)) {
                    _smallVertices.remove(smallVertex);
                }
            }
            for (VertexCheckbox vertexCheckbox : cloneVertexCheckboxes()) {
                if (vertexCheckbox.getParent() instanceof ExpandedVertex) {
                    if (vertexCheckbox.getParent().equals(expandedVertex)) {
                        _vertexCheckbox.remove(vertexCheckbox);
                    }
                } else if (vertexCheckbox.getParent() instanceof SmallVertex) {
                    if (((SmallVertex)vertexCheckbox.getParent()).getParent().equals(expandedVertex)) {
                        _vertexCheckbox.remove(vertexCheckbox);
                    }
                }
            }
            expandedVertex.render(_ctx, selected);
            _smallVertices.addAll(expandedVertex.getSmallVertices());
            _vertexCheckbox.addAll(expandedVertex.getVertexCheckbox());
        }
    }

    private Set<VertexCheckbox> cloneVertexCheckboxes() {
        return new HashSet<>(_vertexCheckbox);
    }

    private Set<SmallVertex> cloneSmallVertices() {
        return new HashSet<>(_smallVertices);
    }


    private CanvasPrimitive setSelected(double x, double y) {
        for (Expand expand : _expand) {
            if (expand.contains(x, y)) {
                return expand;
            }
        }
        for (Vertex vertex : _vertices) {
            if (vertex.contains(x, y)) {
                return vertex;
            }
        }
        for (Connector connector : _connectors) {
            if (connector.contains(x, y)) {
                return connector;
            }
        }
        for (VertexCheckbox vertexCheckbox : _vertexCheckbox) {
            if (vertexCheckbox.contains(x, y)) {
                return vertexCheckbox;
            }
        }
        for (SmallVertex smallVertex : _smallVertices) {
            if (smallVertex.contains(x, y)) {
                return smallVertex;
            }
        }
        for (ExpandedVertex expandedVertex : _expandedVertices) {
            if (expandedVertex.contains(x, y)) {
                return expandedVertex;
            }
        }
        return null;
    }


    private void changeStateIndicator(Node node, VertexStateIndicator.State state) {
        setSelectedNode(node);
        if (selected instanceof Vertex) {
            getSelectedVertex().setRunState(state);
        } else if (selected instanceof ExpandedVertex) {
            getSelectedExpandedVertex().setRunState(state);
        }
        render();
    }


    private Point getSuitableInsertPoint() {
        double x = 50;
        double sepSpace = 100;
        double y = 50;
        boolean overlap;
        do {
            overlap = false;
            for (Vertex vertex : _vertices) {
                if (! vertex.contains(x + 10, y + 10)) continue;

                overlap = true;
                x = x + Vertex.WIDTH + sepSpace;
            }
            for (ExpandedVertex expandedVertex : _expandedVertices) {
                if (! expandedVertex.contains(x + 10, y + 10)) continue;

                overlap = true;
                x = x + Vertex.WIDTH + sepSpace; //TODO
            }
        } while (overlap);

        return new Point(x, y);
    }

    public PipelinePanel getParent() {
        return _parent;
    }
}
