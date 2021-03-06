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

package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.ReadOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public abstract class AbstractFileDataReader implements FileDataReader {

    protected Options _options;
    protected String _path;
    protected InputStream _stream;
    
    protected abstract ReadOptions getReadOptions();

    @Override
    public Table read() throws IOException {
        return Table.read().usingOptions(getReadOptions());
    }


    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options(new CommonReadOptions().toMap());
        }
        return _options;
    }


    @Override
    public int getMaxInputs() {
        return 0;
    }

    @Override
    public int getMaxOutputs() {
        return 1;
    }


    @Override
    public void setFilePath(String path) {
        _path = path;
    }

    @Override
    public String getFilePath() {
        return _path;
    }


    protected String getSource(String def) {
        if (_path != null) {
            File f = new File(_path);
            if (f.exists()) {
                return _path;
            }
        }
        return def;
    }
}
