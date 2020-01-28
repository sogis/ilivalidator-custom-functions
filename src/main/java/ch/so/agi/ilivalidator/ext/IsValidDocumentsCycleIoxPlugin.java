package ch.so.agi.ilivalidator.ext;

import java.util.List;
import java.util.Set;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

public class IsValidDocumentsCycleIoxPlugin implements InterlisFunction {
    private LogEventFactory logger = null;
    
    private ObjectPool objectPool = null;
    
    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        if (actualArguments[0].skipEvaluation()) {
            return actualArguments[0];
        }
        if (actualArguments[0].isUndefined()) {
            return Value.createSkipEvaluation();
        }
        
        try {
            LinkGraphCache lc = new LinkGraphCache(actualArguments[0].getComplexObjects());
            List<String> duplicateEdges = lc.getDuplicateEdges();
            
            // Duplicate edge: two (or more) associations from document A to document B.
            if (duplicateEdges.contains(mainObj.getobjectoid())) {
                logger.addEvent(logger.logErrorMsg("duplicate edge found: " + mainObj.getobjectoid(), mainObj.getobjectoid()));
                return new Value(false);
            }
            
            String startOid = mainObj.getattrobj("Ursprung", 0).getobjectrefoid();
            String endOid = mainObj.getattrobj("Hinweis", 0).getobjectrefoid();
            
            // Self loop: An association the points from document A to document A.
            if (startOid.equals(endOid)) {
                logger.addEvent(logger.logErrorMsg("self loop found: " + startOid, startOid));
                return new Value(false);
            }   
            
            // Cycle: Document A -> Document B -> Document C -> Document A.
            // There is not ONE wrong edge but always several possible wrong ones.
            // It is up to the user to decide.
            Set<String> vertices = lc.getCycleVertices();
            if (vertices.size() == 0) {
                return new Value(true);
            } else {
                if (vertices.contains(startOid) && vertices.contains(endOid)) {
                    String cycles = String.join(",", vertices);
                    logger.addEvent(logger.logErrorMsg("("+startOid +" <-> "+endOid+") is part of a cycle: "+cycles+".", mainObj.getobjectoid()));
                    return new Value(false);
                }
            }
        } catch (Exception e) {
            logger.addEvent(logger.logErrorMsg(e.getMessage()));
            return new Value(false);
        }
        return new Value(true);
    }

    @Override
    public String getQualifiedIliName() {
        return "SO_FunctionsExt.isValidDocumentsCycle";
    }

    @Override
    public void init(TransferDescription td, Settings settings, 
            IoxValidationConfig validationConfig, ObjectPool objectPool, 
            LogEventFactory logEventFactory) {
                
        logger = logEventFactory;
        logger.setValidationConfig(validationConfig);
        
        this.objectPool = objectPool;
    }
}
