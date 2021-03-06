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

package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.Options;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 11/5/21
 */
@Plugin(
        name = "Jaro-Winkler",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Calculates activity label similarity using JaroWinkler Distance"
)
@Pattern(group = PatternGroup.DISTORTED_LABEL)
public class DistortedLabelJaroWinkler extends AbstractDistortedLabel {

    private final JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();

    public DistortedLabelJaroWinkler() { }

    
    @Override
    protected void detect(StringColumn column, String s1, String s2) {
        double threshold = getOptions().get("Threshold").asDouble();
        double distance = jaroWinkler.apply(s1, s2);
        if (distance > threshold && distance < 1.0) {
            addResult(column, s1, s2);
        }
    }


    @Override
    public Options getOptions() {
        Options options = super.getOptions();
        if (!options.containsKey("Threshold")) {
            options.addDefault("Threshold", 0.7);
        }
        return options;
    }

    
    public static void main(String[] args) {
        Table master = Table.create("Master").addColumns(StringColumn.create("Label1"));
        master.column(0).appendCell("house");
        master.column(0).appendCell("hose");
        master.column(0).appendCell("house");
        master.column(0).appendCell("house");
        master.column(0).appendCell("roust");
        master.column(0).appendCell("rusty");
        master.column(0).appendCell("nomatch");
        DistortedLabelJaroWinkler jw = new DistortedLabelJaroWinkler();
        Options options = jw.getOptions();
        options.add("Column Name", "Label1");
        jw.setOptions(options);
        Table result = jw.detect(master);
        System.out.println(result.toString());

        Table changes = Table.create("Repair").addColumns(
                StringColumn.create("Replace"),StringColumn.create("Replacement")
        );
        changes.column(0).appendCell("hose");
        changes.column(1).appendCell("house");
        changes.column(0).appendCell("roust");
        changes.column(1).appendCell("house");
        Table newMaster = jw.repair(master, changes);
        System.out.println(newMaster.toString());

    }

    @Override
    public int imperfektionDetected() {
        return 20; //TODO just a dummy
    }

    @Override
    public double criticalDetections() {
        return 0.1; //TODO just a dummy
    }
}
