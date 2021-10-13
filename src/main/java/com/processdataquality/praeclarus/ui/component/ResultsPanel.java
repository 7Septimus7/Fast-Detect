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

package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.action.FastDetect;
import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.ui.util.NodeWriter;
import com.processdataquality.praeclarus.workspace.NodeRunner;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeRunnerListener;
import com.processdataquality.praeclarus.workspace.node.PatternNode;
import com.processdataquality.praeclarus.workspace.node.WriterNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.*;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
public class ResultsPanel extends VerticalLayout implements NodeRunnerListener {

    Tabs tabs = new Tabs();
    VerticalScrollLayout pages = new VerticalScrollLayout();
    Map<Tab, Component> tabsToPages = new HashMap<>();
    private HashMap<Integer, Map<String, Boolean>> expanded = new HashMap<>();
    private HashMap<Integer, Map<String, Grid<Row>>> detectResult = new HashMap<>();
    private HashMap<Integer, Tab> nodeTabs = new HashMap<>();
    private static final TreeData _treeData = new TreeData();
    private static final List<TreeItem> _list = _treeData.getItems();
    private HashMap<Integer, Map<String, Integer>> totalDetections = new HashMap<>();
    private HashMap<Integer, Map<String, Double>> expectedValues = new HashMap<>();

    private final MainView _parent;

    public ResultsPanel(MainView parent) {
        _parent = parent;
        setId("ResultsPanel");
        add(new H3("Results"));

        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });

        add(tabs, pages);
        removeTopMargin(pages);
        removeTopMargin(tabs);
        pages.setSizeFull();
        setFlexGrow(1f, pages);

        setSizeFull();
        tabs.setVisible(false);

        getNodeRunner().addListener(this);
    }

    @Override
    public void nodeStarted(Node node) {
    }

    @Override
    public void nodePaused(Node node) {
        addResult(node);
    }

    @Override
    public void nodeCompleted(Node node) {
        addResult(node);
    }

    @Override
    public void nodeRollback(Node node) {
        removeResult(node);
    }


    public void addResult(Node node) {
        if (node instanceof WriterNode) {        // special treatment for writers
            new NodeWriter().write(node);
            return;
        }

        if (node.getPlugin() instanceof FastDetect) {
            presentDetectResults(node);

        } else {

            Grid<Row> grid = createGrid(node);
            removeTopMargin(grid);

            VerticalScrollLayout page;
            Tab tab = getTab(node);
            if (tab != null) {
                page = (VerticalScrollLayout) tabsToPages.get(tab);
                page.removeAll();
                page.add(grid);
            } else {
                tab = new ResultTab(node);
                page = new VerticalScrollLayout(grid);
                removeTopMargin(page);
                tabsToPages.put(tab, page);
                pages.add(page);
                tabs.add(tab);
            }

            if (node instanceof PatternNode) {
                handlePatternResult(node, grid, tab);
            }

            tabs.setSelectedTab(tab);
        }
        tabs.setVisible(true);
    }

    private void presentDetectResults(Node node) {
        HashMap<String, Grid<Row>> multipleGrids = createMultipleGrids(node);
        if (!detectResult.containsKey(node.getId())) {
            detectResult.put(node.getId(), multipleGrids);
        }

        if (!expanded.containsKey(node.getId())) {
            HashMap<String, Boolean> expand = new HashMap<>();
            for (String s : multipleGrids.keySet()) {
                expand.put(s, false);
            }
            expanded.put(node.getId(), expand);
        }

        if (!totalDetections.containsKey(node.getId())) {
            getTotalDetections(node);
        }

        if (!expectedValues.containsKey(node.getId())) {
            getExpectedValues(node);
        }

        VerticalScrollLayout page;
        Tab tab;
        Grid<DetectionOutput> grid = new Grid<>(DetectionOutput.class);
        grid.setColumns("imperfectionPattern", "detectionAlgorithm", "count");
        ArrayList<DetectionOutput> detection = new ArrayList<>();
        grid.addComponentColumn(DetectionOutput::getProgressBar);
        addIcon(node, grid);
        grid.setHeightByRows(true);
        grid.getElement().getStyle().set("margi-top", "5px");

        if (nodeTabs.containsKey(node.getId())) {
            tab = nodeTabs.get(node.getId());
            page = (VerticalScrollLayout) tabsToPages.get(tab);
            page.removeAll();
            for (String s : detectResult.get(node.getId()).keySet()) {
                if (!expanded.get(node.getId()).get(s)) {
                    detection.add(new DetectionOutput(findPattern(s), s, totalDetections.get(node.getId()).get(s)
                            + " of " + node.getInputs().get(0).rowCount(), expectedValues.get(node.getId()).get(s)));
                } else {
                    detection.add(new DetectionOutput(findPattern(s), s, totalDetections.get(node.getId()).get(s)
                            + " of " + node.getInputs().get(0).rowCount(), expectedValues.get(node.getId()).get(s)));
                    grid.setItems(detection);
                    page.add(grid);
                    detection = new ArrayList<>();
                    grid = new Grid<>(DetectionOutput.class);
                    grid.setColumns("imperfectionPattern", "detectionAlgorithm", "count");
                    grid.addComponentColumn(DetectionOutput::getProgressBar);
                    addIcon(node, grid);
                    grid.setHeightByRows(true);
                    grid.getElement().getStyle().set("margi-top", "5px");
                    Grid<Row> detected = detectResult.get(node.getId()).get(s);
                    detected.setHeightByRows(true);
                    detected.getElement().getStyle().set("margin-left", "15px");
                    detected.getElement().getStyle().set("margin-top", "5px");
                    detected.getElement().getStyle().set("margin-right", "5px");
                    page.add(detected);
                }
            }
            if (!detection.isEmpty()) {
                grid.setItems(detection);
                page.add(grid);
            }
        } else {
            tab = new ResultTab(node);
            nodeTabs.put(node.getId(), tab);
            page = new VerticalScrollLayout();
            for (String s : multipleGrids.keySet()) {
                    detection.add(new DetectionOutput(findPattern(s), s, totalDetections.get(node.getId()).get(s)
                            + " of " + node.getInputs().get(0).rowCount(), expectedValues.get(node.getId()).get(s)));
            }
            grid.setItems(detection);
            page.add(grid);
            tabsToPages.put(tab, page);
            pages.add(page);
            tabs.add(tab);
        }

        tabs.setSelectedTab(tab);
    }

    private void getExpectedValues(Node node) {
        HashMap<String, Double> totalDetection = new HashMap<>();
        for (Node s : node.getMultipleOutput().keySet()) {
            totalDetection.put(s.getName(), ((ImperfectionPattern) s.getPlugin()).expectedDetections());
        }
        expectedValues.put(node.getId(), totalDetection);
    }

    private void getTotalDetections(Node node) {
        HashMap<String, Integer> totalDetection = new HashMap<>();
        for (Node s : node.getMultipleOutput().keySet()) {
            totalDetection.put(s.getName(), ((ImperfectionPattern) s.getPlugin()).imperfektionDetected());
        }
        totalDetections.put(node.getId(), totalDetection);
    }

    private void addIcon(Node node, Grid<DetectionOutput> grid) {
        grid.addComponentColumn(item -> {
            Icon icon;
            if (expanded.get(node.getId()).get(item.getDetectionAlgorithm())) {
                icon = VaadinIcon.ANGLE_UP.create();
            } else {
                icon = VaadinIcon.ANGLE_DOWN.create();
            }
                icon.setColor("black");
                icon.setId(item.getDetectionAlgorithm());
                icon.addClickListener(event -> {
                    if (expanded.get(node.getId()).get(item.getDetectionAlgorithm())) {
                        expanded.get(node.getId()).replace(item.getDetectionAlgorithm(), true, false);
                    } else {
                        expanded.get(node.getId()).replace(item.getDetectionAlgorithm(), false, true);
                    }
                    presentDetectResults(node);
                });

            return icon;
        }).setKey("icon").setFlexGrow(0).setWidth("45px");
    }

    private String findPattern(String s) {
        for (TreeItem item : _list) {
            if (item.getParent() != null) {
                if (item.getParent().getLabel() != null) {
                    if (item.getLabel() != null) {
                        if (Objects.equals(item.getLabel(), s)) {
                            return item.getParent().getLabel();
                        }
                    }
                }
            }
        }
        return "";
    }


    private Tab getTab(Node node) {
        for (ResultTab tab : getTabs(node)) {
            if (tab.resultEquals(node)) {
                return tab;
            }
        }
        return null;
    }


    private void handlePatternResult(Node node, Grid<Row> grid, Tab tab) {
        if (!node.hasCompleted()) {
            tab.setLabel(node.getName() + " - Detected");
            grid.setSelectionMode(Grid.SelectionMode.MULTI);

            Button btnRepair = new Button("Repair Selected");
            Button btnDont = new Button("Don't Repair");
            btnRepair.addClickListener(e -> {
                btnRepair.setEnabled(false);   // only allow one repair
                btnDont.setEnabled(false);
                repair(node, grid);
            });
            btnDont.addClickListener(e -> {
                btnRepair.setEnabled(false);   // only allow one repair
                btnDont.setEnabled(false);
                getNodeRunner().resume(node);
            });
            FooterRow footer = grid.appendFooterRow();
            footer.getCells().get(0).setComponent(new HorizontalLayout(btnDont, btnRepair));
        } else {
            tab.setLabel(node.getName() + " - Repaired");
        }
    }


    public void removeResult(Node node) {
        for (ResultTab tab : getTabs(node)) {
            Div div = (Div) tabsToPages.remove(tab);
            tabs.remove(tab);
            pages.remove(div);
        }
        tabs.setVisible(!tabsToPages.isEmpty());
    }


    public void clear() {
        tabs.removeAll();
        tabs.setVisible(false);
        pages.removeAll();
        tabsToPages.clear();
    }


    private Grid<Row> createGrid(Node node) {
        Table table = node.getOutput();
        return tableToGrid(table);
    }

    private HashMap<String, Grid<Row>> createMultipleGrids(Node node) {
        HashMap<String, Grid<Row>> result = new HashMap<>();
        for (Node s : node.getMultipleOutput().keySet()) {
            Grid<Row> grid = tableToGrid(node.getMultipleOutput().get(s));
            result.put(s.getName(), grid);
        }
        return result;
    }

    private Grid<Row> tableToGrid(Table table) {
        Grid<Row> grid = new Grid<>();
        for (String name : table.columnNames()) {
            ColumnType colType = table.column(name).type();
            Grid.Column<Row> column;
            if (colType == ColumnType.STRING) {
                column = grid.addColumn(row -> row.getString(name));
            } else if (colType == ColumnType.BOOLEAN) {
                column = grid.addColumn(row -> row.getBoolean(name));
            } else if (colType == ColumnType.INTEGER) {
                column = grid.addColumn(row -> row.getInt(name));
            } else if (colType == ColumnType.LONG) {
                column = grid.addColumn(row -> row.getLong(name));
            } else if (colType == ColumnType.FLOAT) {
                column = grid.addColumn(row -> row.getFloat(name));
            } else if (colType == ColumnType.DOUBLE) {
                column = grid.addColumn(row -> row.getDouble(name));
            } else if (colType == ColumnType.LOCAL_DATE) {
                column = grid.addColumn(row -> row.getDate(name));
            } else if (colType == ColumnType.LOCAL_TIME || colType == ColumnType.LOCAL_DATE_TIME) {
                column = grid.addColumn(row -> row.getDateTime(name));
            } else if (colType == ColumnType.INSTANT) {
                column = grid.addColumn(row -> row.getInstant(name));
            } else {
                column = grid.addColumn(row -> row.getObject(name));
            }

            column.setHeader(name).setAutoWidth(true);
        }

        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < table.rowCount(); i++) {
            rows.add(table.row(i));
        }
        grid.setItems(rows);
        return grid;
    }


    private void repair(Node node, Grid<Row> grid) {
        Table repairs = Table.create("Repairs").addColumns(
                StringColumn.create("Incorrect"),
                StringColumn.create("Correct"));
        for (Row row : grid.asMultiSelect().getSelectedItems()) {
            repairs.column(0).appendCell(row.getString("Label2"));
            repairs.column(1).appendCell(row.getString("Label1"));
        }
        ((PatternNode) node).setRepairs(repairs);

        NodeRunner runner = getNodeRunner();
        runner.resume(node);
    }


    private void removeTopMargin(Component c) {
        c.getElement().getStyle().set("margin-top", "0");
    }


    private NodeRunner getNodeRunner() {
        return _parent.getPipelinePanel().getWorkspace().getRunner();
    }


    private Set<ResultTab> getTabs(Node node) {
        Set<ResultTab> tabSet = new HashSet<>();
        for (int i = 0; i < tabs.getComponentCount(); i++) {
            ResultTab tab = (ResultTab) tabs.getComponentAt(i);
            if (tab.nodeEquals(node)) {
                tabSet.add(tab);
            }
        }
        return tabSet;
    }

}
