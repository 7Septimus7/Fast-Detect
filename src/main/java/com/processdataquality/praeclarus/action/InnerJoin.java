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

package com.processdataquality.praeclarus.action;

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.workspace.node.Node;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.List;

/**
 * @author Michael Adams
 * @date 21/5/21
 */
@Plugin(
        name = "Inner Join",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Performs an inner join on a set of tables."
)
public class InnerJoin implements Action {

    private final Options options = new Options();

    private Node node;

    public InnerJoin() { }

    @Override
    public HashMap<Node, Table> run(List<Table> inputList) {
        HashMap<Node, Table> result = new HashMap<>();
        if (inputList.size() < 2) {
            throw new IllegalArgumentException("This action requires at least two tables as input.");
        }
        Table t1 = inputList.remove(0);
        String colNames = getOptions().get("Columns").asString();
        result.put(node, t1.joinOn(colNames).inner(true,
                inputList.toArray(new Table[] {})));
        return result;
    }


    @Override
    public Options getOptions() {
        if (options.isEmpty()) {
            options.add("Columns", "");
        }
        return options;
    }


    @Override
    public int getMaxInputs() {
        return 2;
    }

    @Override
    public int getMaxOutputs() {
        return 1;
    }

    @Override
    public boolean isExpandable() { return false; }

    public void setNode(Node node) {
        this.node = node;
    }
}
