package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import tech.tablesaw.api.StringColumn;

@Plugin(
        name = "Test1",
        author = "Maximilian Harms",
        version = "1.0",
        synopsis = "Test1"
)
@Pattern(group = PatternGroup.DISTORTED_LABEL)
public class DistortedLabelTest1 extends AbstractDistortedLabel {
    @Override
    protected void detect(StringColumn column, String s1, String s2) {

    }

    @Override
    public int imperfektionDetected() {
        return 0;
    }

    @Override
    public double expectedDetections() {
        return 0;
    }
}
