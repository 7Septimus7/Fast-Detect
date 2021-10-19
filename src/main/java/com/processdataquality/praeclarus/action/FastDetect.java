package com.processdataquality.praeclarus.action;

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.workspace.node.Node;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Maximilian Harms
 * @date 21/5/21
 */
@Plugin(
        name = "Fast Detect",
        author = "Maximilian Harms",
        version = "1.0",
        synopsis = "Adapter for automatic quality check of event logs."
)
public class FastDetect implements Action{

    private final Options options = new Options();

    private Set<Node> _detectNodes = new HashSet<>();

    @Override
    public HashMap<Node, Table> run(List<Table> inputSet) {
        HashMap<Node, Table> fastDetect = new HashMap<>();
        for (Node node : _detectNodes) {
            if (node.getPlugin() instanceof ImperfectionPattern) {
                Table nodeDetect = ((ImperfectionPattern) node.getPlugin()).detect(inputSet.get(0));
                fastDetect.put(node,nodeDetect);
            }
        }
        return fastDetect;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public int getMaxOutputs() {
        return 1;
    }

    @Override
    public boolean isExpandable() { return true; }

    public Set<Node> getDetectNodes() {
        return _detectNodes;
    }

    public void setDetectNodes(Set<Node> _detectNodes) {
        this._detectNodes = _detectNodes;
    }
}
