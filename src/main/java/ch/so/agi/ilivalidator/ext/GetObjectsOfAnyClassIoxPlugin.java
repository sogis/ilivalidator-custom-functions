package ch.so.agi.ilivalidator.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxDataPool;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

public class GetObjectsOfAnyClassIoxPlugin implements InterlisFunction {
    private Settings settings = null;
    private LogEventFactory logger = null;
    private HashMap<String, Viewable> tag2class = null;
    private TransferDescription td = null;
    private ObjectPool objectPool = null;
    
    private static final String CACHE_NAME = "ch.so.agi.ilivalidator.ext.GetObjectsOfAnyClassIoxPlugin.Cache";

    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject contextObject, Value[] arguments) {
        Value argClassName = arguments[0];

        if (argClassName.skipEvaluation()) {
            return argClassName;
        }
        
        if (argClassName.isUndefined()) {
            return Value.createSkipEvaluation();
        }
        
        String classNameValue = argClassName.getValue();
        
        
        IoxDataPool pipelinePool = (IoxDataPool) settings.getTransientObject(InterlisFunction.IOX_DATA_POOL);
        if (pipelinePool.getIntermediateValue(CACHE_NAME) == null) {
            List<IomObject> objList = new ArrayList<IomObject>();
            objectPool.getBasketIds().stream().map((basketId) -> (objectPool.getObjectsOfBasketId(basketId)).valueIterator()).forEach((Iterator objectIterator) -> {
                while (objectIterator.hasNext()) {
                    IomObject iomObj = (IomObject) objectIterator.next();
                    if (iomObj != null && iomObj.getobjecttag().equals(classNameValue)) {
                        objList.add(iomObj);
                    }
                }
            });
            pipelinePool.setIntermediateValue(CACHE_NAME, objList);
            return new Value(objList);
        } else {
            List<IomObject> objList = (List<IomObject>) pipelinePool.getIntermediateValue(CACHE_NAME);
            return new Value(objList);
        }
    }

    @Override
    public String getQualifiedIliName() {
        return "SO_FunctionsExt.getObjectsOfAnyClass";
    }

    @Override
    public void init(TransferDescription td, Settings settings, IoxValidationConfig validationConfig, ObjectPool objectPool,
            LogEventFactory logEventFactory) {

        this.settings = settings;
        this.logger = logEventFactory;
        this.logger.setValidationConfig(validationConfig);
        this.objectPool = objectPool;
        this.tag2class = ch.interlis.iom_j.itf.ModelUtilities.getTagMap(td);
        this.td = td;
    }
}
