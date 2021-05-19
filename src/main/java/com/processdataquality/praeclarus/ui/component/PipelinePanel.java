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

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginService;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.ui.component.canvas.Canvas;
import com.processdataquality.praeclarus.workspace.Workspace;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
@CssImport("./styles/pdq-styles.css")
@JsModule("./src/test.js")
public class PipelinePanel extends VerticalLayout {

    private final Workspace _workspace;
    private final List<String> _pipeLabels = new ArrayList<>();
    private final List<PDQPlugin> _pipeItems = new ArrayList<>();
    private final MainView _parent;
    private final RunnerButtons _runnerButtons;
    private final ListBox<String> _pipelineList;
    private final Canvas _canvas = new Canvas(1000, 500);


    private int _selectedIndex;

    public PipelinePanel(MainView parent) {
        _parent = parent;
        _workspace = new Workspace();
        _pipelineList = new ListBox<>();
        _runnerButtons = new RunnerButtons(_workspace, _parent.getResultsPanel());
        _runnerButtons.addButton(createRemoveButton());
        add(new H3("Pipeline"));
        add(createCanvas());
        add(_runnerButtons);
        setSizeFull();
        addAttachListener(e -> _canvas.setDimensions());

//        _canvas.setDimensions();
//        add(new Html("<input type='file' id='file-input' onchange='inputselect()'/>"));


 //       UI.getCurrent().getPage().executeJs("window.posn()");
    }

 //   private ListBox<String> createCanvas() {

    private Div createCanvas() {
//        HorizontalLayout hl = new HorizontalLayout();
  //      _pipelineList.setSizeFull();

        _pipelineList.addValueChangeListener(e -> {
            String selected = e.getValue();
            _selectedIndex = 0;
            for (int i = _selectedIndex; i < _pipeLabels.size(); i++) {
                if (_pipeLabels.get(i).equals(selected)) break;
            }

            // get props for selected
            showPluginProperties(_selectedIndex);
        });

        DropTarget<Canvas> dropTarget = DropTarget.create(_canvas);
//        DropTarget<ListBox<String>> dropTarget = DropTarget.create(_pipelineList);
        dropTarget.setDropEffect(DropEffect.COPY);
        dropTarget.addDropListener(event -> {
            if (event.getDropEffect() == DropEffect.COPY) {
                if (event.getDragData().isPresent()) {
                    List<TreeItem> droppedItems = (List<TreeItem>) event.getDragData().get();
                    TreeItem item = droppedItems.get(0);     // only one is dropped
                    _pipeLabels.add(item.getName());
                    _pipelineList.setItems(_pipeLabels);
                    addPluginInstance(item);
                    _canvas.drawNode(_workspace.getNodeCount());
                    //UI.getCurrent().getPage().executeJs("window.drawStep($0)", item.getName());
                }
            }
        });

 //       return _pipelineList;

 //       hl.add(_pipelineList, _canvas);
 //       _canvas.setSizeFull();
 //       hl.setSizeFull();
        Div div = new Div();
        div.add(_canvas);
        div.setSizeFull();
        return div;
    }


    private void showPluginProperties(int selected) {
        PDQPlugin plugin = _workspace.getNode(selected).getPlugin();
        _parent.getPropertiesPanel().setPlugin(plugin);
    }


    private void addPluginInstance(TreeItem item) {
        String pTypeName = item.getParent().getName();
        PDQPlugin instance = null;
        if (pTypeName.equals("Readers")) {
            instance = PluginService.readers().newInstance(item.getName());
        }
        if (pTypeName.equals("Writers")) {
            instance = PluginService.writers().newInstance(item.getName());
        }
        if (pTypeName.equals("Patterns")) {
            instance = PluginService.patterns().newInstance(item.getName());
        }
        _workspace.appendPlugin(instance);
        _runnerButtons.enable();
        _pipeItems.add(instance);
        showPluginProperties(_pipeItems.size() -1);
    }


    private Button createRemoveButton() {
        Icon icon = VaadinIcon.TRASH.create();
        icon.setSize("24px");
        return new Button(icon, e -> {
            String removed = _pipeLabels.remove(_selectedIndex);
            _pipelineList.setItems(_pipeLabels);
            _workspace.dropNode(removed);
        });
    }

    public void onResize() {
        _canvas.redraw();
    }
}