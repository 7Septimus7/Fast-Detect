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

package com.processdataquality.praeclarus.ui;

import com.processdataquality.praeclarus.ui.component.PipelinePanel;
import com.processdataquality.praeclarus.ui.component.PluginsPanel;
import com.processdataquality.praeclarus.ui.component.PropertiesPanel;
import com.processdataquality.praeclarus.ui.component.ResultsPanel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

/**
 * @author Michael Adams
 * @date 14/4/21
 */
@Route
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class MainView extends VerticalLayout {

    private final PropertiesPanel _propsPanel = new PropertiesPanel();
    private final PipelinePanel _pipelinePanel;
    private final ResultsPanel _resultsPanel;


    public MainView() {
        _pipelinePanel = new PipelinePanel(this);
        _resultsPanel = new ResultsPanel(this);
        add(new H1("Praeclarus PDQ"));
        SplitLayout masterLayout = new SplitLayout();
        masterLayout.addToPrimary(leftPanel());
        masterLayout.addToSecondary(centrePanel());
        add(masterLayout);
        masterLayout.setSizeFull();
        setSizeFull();
    }

    public PropertiesPanel getPropertiesPanel() { return _propsPanel; }

    public ResultsPanel getResultsPanel() { return _resultsPanel; }


    private SplitLayout leftPanel() {
        SplitLayout leftLayout = new SplitLayout();
        leftLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        leftLayout.addToPrimary(new PluginsPanel());
        leftLayout.addToSecondary(_propsPanel);
        leftLayout.setWidth("25%");
        return leftLayout;
    }


    private SplitLayout centrePanel() {
        SplitLayout centreLayout = new SplitLayout();
        centreLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        centreLayout.addToPrimary(_pipelinePanel);
        centreLayout.addToSecondary(_resultsPanel);
        centreLayout.setWidth("75%");
        centreLayout.setSplitterPosition(40);
        centreLayout.addSplitterDragendListener(e -> _pipelinePanel.onResize());
        return centreLayout;
    }


    private void showOutput(String text, Component content,
            HasComponents outputContainer) {
        HtmlComponent p = new HtmlComponent(Tag.P);
        p.getElement().setText(text);
        outputContainer.add(p);
        outputContainer.add(content);
    }
}