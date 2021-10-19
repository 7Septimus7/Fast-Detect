package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;


@Plugin(
        name = "Test",
        author = "Maximilian Harms",
        version = "1.0",
        synopsis = "test dummy"
)
@Pattern(group = PatternGroup.POLLUTED_LABEL)
public class PollutedLabelExample2 implements ImperfectionPattern{
    @Override
    public Table detect(Table table) {
        return null;
    }

    @Override
    public Table repair(Table master, Table changes) {
        return null;
    }

    @Override
    public boolean canDetect() {
        return false;
    }

    @Override
    public int imperfektionDetected() {
        return 0;
    }

    @Override
    public double criticalDetections() {
        return 0;
    }

    @Override
    public boolean canRepair() {
        return true;
    }

    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public int getMaxInputs() {
        return 0;
    }

    @Override
    public int getMaxOutputs() {
        return 0;
    }

}
