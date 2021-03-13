package ch.so.agi.ilivalidator.ext;

import java.util.HashMap;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
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
    }

}
