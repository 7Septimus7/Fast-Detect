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

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Michael Adams
 * @date 31/3/21
 */
public interface DataWriter extends PDQPlugin {

    /**
     * Writes the data in a Table object to a sink (file, stream, etc)
     * @throws IOException if anything goes wrong
     */
    void write(Table table) throws IOException;

    OutputStream getOutputStream();

    void setOutputStream(OutputStream stream);

}
