package ch.so.agi.ilivalidator.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import ch.interlis.iom.IomObject;

public class LinkGraphCache {
    private Graph<String, DefaultEdge> graph;

    private List<String> duplicateEdges;

    private Set<String> cycleVertices;

    public LinkGraphCache(Collection<IomObject> collection) {
        graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        duplicateEdges = new ArrayList<String>();

        for (IomObject iomObj : collection) {
            String startOid = iomObj.getattrobj("Ursprung", 0).getobjectrefoid();
            String endOid = iomObj.getattrobj("Hinweis", 0).getobjectrefoid();

            try {
                DefaultEdge e = null;
                e = Graphs.addEdgeWithVertices(graph, startOid, endOid);
                if (e == null) {
                    duplicateEdges.add(iomObj.getobjectoid());
                }
            } catch (IllegalArgumentException e) {
                // Self loops throw an IllegalArgumentException.
                // Self loops will be handled in the InterlisFunction by
                // comparing the OIDs.
            }
        }
        
        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        cycleVertices = cycleDetector.findCycles();
    }

    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }

    public List<String> getDuplicateEdges() {
        return duplicateEdges;
    }
    
    public Set<String> getCycleVertices() {
        return cycleVertices;
    }
}
