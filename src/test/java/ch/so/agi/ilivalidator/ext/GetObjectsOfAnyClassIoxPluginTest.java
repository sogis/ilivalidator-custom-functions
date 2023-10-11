package ch.so.agi.ilivalidator.ext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox.IoxLogEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.ObjectEvent;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.ValidationConfig;
import ch.interlis.iox_j.validator.Validator;

public class GetObjectsOfAnyClassIoxPluginTest {
    private TransferDescription td=null;
    // OID
    private final static String OBJ_OID1 ="o1";
    private final static String OBJ_OID2 ="o2";
    private final static String OBJ_OID3 ="o3";
    private final static String OBJ_OID4 ="o4";
    // MODEL
    private final static String ILI_TOPIC="Testmodel2.TopicA";
    // CLASS
    private final static String ILI_CLASSB=ILI_TOPIC+".ClassB";
    private final static String ILI_CLASSC=ILI_TOPIC+".ClassC";
    
    // START BASKET EVENT
    private final static String BID1="b1";
    
    @BeforeEach
    public void setUp() throws Exception {
        Configuration ili2cConfig = new Configuration();
        {
            FileEntry fileEntry = new FileEntry("src/test/data/SO_FunctionsExt.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
        }
        {
            FileEntry fileEntry = new FileEntry("src/test/data/Testmodel2.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
        }
        td = ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        assertNotNull(td);
    }
    
    @Test
    public void getObjects_Ok() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSB, OBJ_OID1);
        iomObjA.setattrvalue("attr1", "foo");
        iomObjA.setattrvalue("attr2", "5");
        Iom_jObject iomObjB = new Iom_jObject(ILI_CLASSB, OBJ_OID2);
        iomObjB.setattrvalue("attr1", "foo");
        iomObjB.setattrvalue("attr2", "5");
        Iom_jObject iomObjC = new Iom_jObject(ILI_CLASSC, OBJ_OID3);
        iomObjC.setattrvalue("attr3", "foo");
        Iom_jObject iomObjD = new Iom_jObject(ILI_CLASSC, OBJ_OID4);
        iomObjC.setattrvalue("attr3", "bar");

        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.getObjectsOfAnyClass", GetObjectsOfAnyClassIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA));
        validator.validate(new ObjectEvent(iomObjB));
        validator.validate(new ObjectEvent(iomObjC));
        validator.validate(new ObjectEvent(iomObjD));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        assertTrue(logger.getErrs().size()==0);
    }

    @Test
    public void getObjects_Fail() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSB, OBJ_OID1);
        iomObjA.setattrvalue("attr1", "foo");
        iomObjA.setattrvalue("attr2", "5");
        Iom_jObject iomObjC = new Iom_jObject(ILI_CLASSC, OBJ_OID3);
        iomObjC.setattrvalue("attr3", "foo");
        Iom_jObject iomObjD = new Iom_jObject(ILI_CLASSC, OBJ_OID4);
        iomObjC.setattrvalue("attr3", "bar");

        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.getObjectsOfAnyClass", GetObjectsOfAnyClassIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA));
        validator.validate(new ObjectEvent(iomObjC));
        validator.validate(new ObjectEvent(iomObjD));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
//        for (IoxLogEvent ev : logger.getErrs()) {
//            System.out.println(ev.getEventMsg());
//        }
        
        assertTrue(logger.getErrs().size()==2);
    }

}
