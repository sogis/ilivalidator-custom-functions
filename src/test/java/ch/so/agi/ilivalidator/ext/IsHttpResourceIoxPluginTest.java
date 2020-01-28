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
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.ObjectEvent;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.ValidationConfig;
import ch.interlis.iox_j.validator.Validator;

public class IsHttpResourceIoxPluginTest {
    private TransferDescription td=null;
    // OID
    private final static String OBJ_OID1 ="o1";
    // MODEL
    private final static String ILI_TOPIC="Testmodel.Topic";
    // CLASS
    private final static String ILI_CLASSA=ILI_TOPIC+".ClassA";
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
            FileEntry fileEntry = new FileEntry("src/test/data/Testmodel.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
        }
        td = ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        assertNotNull(td);
    }

    @Test
    public void isHttpResource_Ok() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA.setattrvalue("attr2", "");
        iomObjA.setattrvalue("attr3", "https://www.google.ch");
        iomObjA.setattrvalue("attr4", "https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/65-Aedermannsdorf/Entscheide/65-5-E.pdf");
        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isHttpResource", IsHttpResourceIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
                
        assertTrue(logger.getErrs().size()==0);
    }
    
    @Test
    public void isHttpResource_WithPrefix_Ok() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA.setattrvalue("attr2", "https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/");
        iomObjA.setattrvalue("attr3", "65-Aedermannsdorf/Entscheide/65-5-E.pdf");
        iomObjA.setattrvalue("attr4", "65-Aedermannsdorf/Entscheide/65-5-E.pdf");
        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isHttpResource", IsHttpResourceIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        // size=1 because of 'invalid format of INTERLIS.URI value <65-Aedermannsdorf/Entscheide/65-5-E.pdf> in attribute attr4'
        // This was introduced after ilivalidator 1.8.1
        assertTrue(logger.getErrs().size()==1);
    }
    
    @Test
    public void isHttpResource_Fail() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA.setattrvalue("attr2", "");
        iomObjA.setattrvalue("attr3", "https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/65-Aedermannsdorf/Entscheide/65-5-FOO.pdf");
        iomObjA.setattrvalue("attr4", "https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/65-Aedermannsdorf/Entscheide/65-5-BAR.pdf");
        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isHttpResource", IsHttpResourceIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        assertTrue(logger.getErrs().size()==2); 
    }
    
    @Test
    public void isHttpResource_WithPrefix_Fail() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA.setattrvalue("attr2", "http://");
        iomObjA.setattrvalue("attr3", "65-Aedermannsdorf/Entscheide/65-5-E.pdf");
        iomObjA.setattrvalue("attr4", "65-Aedermannsdorf/Entscheide/65-5-E.pdf");
        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isHttpResource", IsHttpResourceIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());

        // size=3 because of 'invalid format of INTERLIS.URI value <65-Aedermannsdorf/Entscheide/65-5-E.pdf> in attribute attr4'
        // This was introduced after ilivalidator 1.8.1
        assertTrue(logger.getErrs().size()==3); 
    }
}
