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

package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.annotations.Plugin;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Michael Adams
 * @date 31/3/21
 */
@Plugin(
        name = "CSV Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to a CSV file.",
        fileDescriptors = "CSV Files;text/csv;.csv"
)
public class CsvDataWriter extends AbstractDataWriter {

    public CsvDataWriter() {
        initOptions();
    }


    @Override
    public void write(Table table) throws IOException {
        table.write().usingOptions(getWriteOptions());
    }


    private void initOptions() {
        _options.addDefault("Header", true);
        _options.addDefault("Destination", "");
        _options.addDefault("Separator", ',');
        _options.addDefault("Quote", '\"');
    }


    private CsvWriteOptions.Builder createBuilder() throws IOException {
        OutputStream stream = getOutputStream();
        return stream != null ? CsvWriteOptions.builder(stream) :
                CsvWriteOptions.builder(_options.get("Destination").asString());
    }

    
    private CsvWriteOptions getWriteOptions() throws IOException {
        CsvWriteOptions.Builder builder = createBuilder();
        for (String key : _options.getChanges().keySet()) {
            if (key.equals("Header")) {
                builder.header(_options.get("Header").asBoolean());
            }
            if (key.equals("Separator")) {
                builder.separator(_options.get("Separator").asChar());
            }
            if (key.equals("Quote")) {
                builder.quoteChar(_options.get("Quote").asChar());
            }
        }
        return builder.build();
    }

}
