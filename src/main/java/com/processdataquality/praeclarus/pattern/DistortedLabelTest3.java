package com.processdataquality.praeclarus.pattern;


import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import tech.tablesaw.api.StringColumn;

@Plugin(
        name = "Test3",
        author = "Maximilian Harms",
        version = "1.0",
        synopsis = "Test3 dummy"
)
@Pattern(group = PatternGroup.DISTORTED_LABEL)
public class DistortedLabelTest3 extends AbstractDistortedLabel {
    @Override
    protected void detect(StringColumn column, String s1, String s2) {

    }

    @Override
    public int imperfektionDetected() {
        return 0;
    }

    @Override
    public double criticalDetections() {
        return 0;
    }
}
