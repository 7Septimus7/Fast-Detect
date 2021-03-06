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

import com.processdataquality.praeclarus.workspace.node.Node;
import com.vaadin.flow.component.tabs.Tab;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 11/6/21
 */
public class ResultTab extends Tab {

    private final Node _node;
    private final Table _table;


    public ResultTab(Node node) {
        super(node.getName());
        _node = node;
        _table = _node.getOutput();
    }

    public Node getNode() { return _node; }

    public boolean nodeEquals(Node node) { return _node == node; }


    public boolean resultEquals(Node node) {
        return nodeEquals(node) && node.getOutput().equals(_table);
    }

}
