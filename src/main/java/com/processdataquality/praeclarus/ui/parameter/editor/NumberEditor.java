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

package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.textfield.NumberField;

/**
 * @author Michael Adams
 * @date 5/5/21
 */

public class NumberEditor extends AbstractEditor {

    public NumberEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
    }


    protected NumberField createField(PluginParameter param) {
        NumberField field = new NumberField();
        field.setWidth("75%");
        field.setHasControls(true);
        field.setValue((double) param.getValue());
        field.addValueChangeListener(e -> {
            param.setValue(e.getValue());
           updateProperties(param);
        });
        return field;
    }


}