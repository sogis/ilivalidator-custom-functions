package ch.so.agi.ilivalidator.ext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ch.ehi.basics.settings.Settings;
import ch.ehi.iox.objpool.impl.ObjPoolImpl2;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxDataPool;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.ObjectPoolKey;
import ch.interlis.iox_j.validator.Value;

public class AreAreasWithoutHoles implements InterlisFunction {

    private LogEventFactory logger = null;
    private HashMap<String, Viewable> tag2class = null;
    private TransferDescription td = null;

    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        if (actualArguments[0].skipEvaluation()) {
            return actualArguments[0];
        }
        if (actualArguments[0].isUndefined()) {
            return Value.createSkipEvaluation();
        }

        if (actualArguments[1].skipEvaluation()) {
            return actualArguments[1];
        }
        if (actualArguments[1].isUndefined()) {
            return Value.createSkipEvaluation();
        }
        
        String geomAttrName = actualArguments[1].getValue();
//        System.out.println(geomAttrName);
        
        
//        Collection<IomObject> collection = actualArguments[0].getComplexObjects();        
//        for (IomObject iomObj : collection) {
//            System.out.println(iomObj.getobjecttag());
//        }
        

        
        
        System.out.println("run run run");
        
        
        
        
        
        
        
        
        return new Value(false);
    }

    @Override
    public String getQualifiedIliName() {
        return "SO_FunctionsExt.areAreasWithoutHoles";
    }

    @Override
    public void init(TransferDescription td, Settings settings, 
            IoxValidationConfig validationConfig, ObjectPool objectPool, 
            LogEventFactory logEventFactory) {

        this.logger = logEventFactory;
        this.logger.setValidationConfig(validationConfig);
        this.tag2class = ch.interlis.iom_j.itf.ModelUtilities.getTagMap(td);
        this.td = td;
        
        
        // caching things
        if (settings.getTransientObject("mycache") == null) {
            System.out.println("calculate cache only once");
            settings.setTransientObject("mycache", "fubar");
        }
        
        System.out.println("my cache object: " + settings.getTransientObject("mycache"));

        // get arbitrary objects
//        Object modelElement=tag2class.get("Testmodel.Topic.ClassB");
//        System.out.println(modelElement);


        // see https://github.com/SwissTierrasColombia/iliValidator_custom_plugins/blob/master/src/main/java/co/interlis/topology/TopologyCache.java#L68
        Set<String> basketIds = objectPool.getBasketIds();
        for (String basketId : basketIds) {
            System.out.println(basketId);
            ObjPoolImpl2 objectPoolBasket = objectPool.getObjectsOfBasketId(basketId);
            
            Set<ObjectPoolKey> objPoolKeys = objectPoolBasket.keySet();
            System.out.println(objPoolKeys);
            
            for (ObjectPoolKey key : objPoolKeys) {
                System.out.println(objectPoolBasket.get(key));
                IomObject iomObj = (IomObject) objectPoolBasket.get(key);
            }
            
        }
        
        
        //IoxDataPool pipelinePool=settings.getIntermediateValue(IOX_DATA_POOL);
//        PipelinePool pipelinePool=(PipelinePool) settings.getTransientObject(IOX_DATA_POOL);
//        
//        System.out.println("*"+pipelinePool);

//        Iterator it = pipelinePool.getElements().iterator();
//        while(it.hasNext()) {
//            System.out.println(it.next());
//        }
        

        
//        if (pipelinePool.getIntermediateValue("mycache") == null) {
//            // calculate cache only once
//            System.out.println("calculate cache");
//            pipelinePool.setIntermediateValue("mycache", "fubar");
//            System.out.println(pipelinePool.getIntermediateValue("mycache"));
//        }
        
        // use cache

        
        
//        System.out.println(objectPool.getBasketIds());
//        System.out.println(objectPool.getObjectsOfBasketId("b1"));
        
    }

}
