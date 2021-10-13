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

package com.processdataquality.praeclarus.workspace.node;

import com.processdataquality.praeclarus.action.Action;
import com.processdataquality.praeclarus.action.FastDetect;
import com.processdataquality.praeclarus.plugin.PDQPlugin;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class ActionNode extends Node {

    private PDQPlugin plugin;

    public ActionNode(PDQPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin instanceof FastDetect) {
            setMultipleOutput(((Action) getPlugin()).run(getInputs()));
            setOutput(getInputs().get(0));
        } else {
            setOutput(((Action) getPlugin()).run(getInputs()).values().iterator().next());
        }
        setCompleted(true);
    }
}
