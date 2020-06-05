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
import ch.interlis.iom.IomObject;
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

public class IsHttpResourceFromOerebMultilingualUriIoxPluginTest {
    private TransferDescription td=null;
    // OID
    private final static String OBJ_OID1 ="o1";
    // MODEL
    private final static String ILI_TOPIC="Testmodel.Topic";
    // CLASS
    private final static String ILI_CLASSA=ILI_TOPIC+".ClassA";
    private final static String ILI_CLASSD=ILI_TOPIC+".ClassD";
    
    // STRUCTURE
    private final static String STRUCTD = "OeREBKRM_V1_1.MultilingualUri";
    private final static String STRUCTS = "OeREBKRM_V1_1.LocalisedUri";

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
            FileEntry fileEntry = new FileEntry("src/test/data/CHBase_Part2_LOCALISATION_20110830.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
        } 
        {
            FileEntry fileEntry = new FileEntry("src/test/data/CHBase_Part3_CATALOGUEOBJECTS_20110830.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
        }        
        {
            FileEntry fileEntry = new FileEntry("src/test/data/OeREBKRM_V1_1.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
        }        
        {
            FileEntry fileEntry = new FileEntry("src/test/data/HTTP_OEREB_MultilingualUrl_Testmodel.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
        }
        td = ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        assertNotNull(td);
    }
    
    @Test 
    void isHttpResourceMultilingual_Ok() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSD, OBJ_OID1);
        Iom_jObject objStructD = (Iom_jObject) iomObjA.addattrobj("TextImWeb", STRUCTD);
        Iom_jObject objStructS1 = (Iom_jObject) objStructD.addattrobj("LocalisedText", STRUCTS);
        objStructS1.setattrvalue("Language", "de");
        objStructS1.setattrvalue("Text", "https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/65-Aedermannsdorf/Entscheide/65-5-E.pdf");
                
        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isHttpResourceFromOerebMultilingualUri", IsHttpResourceFromOerebMultilingualUriIoxPlugin.class);
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
    void isHttpResourceMultilingual_Fail() {
        Iom_jObject iomObjA = new Iom_jObject(ILI_CLASSD, OBJ_OID1);
        Iom_jObject objStructD = (Iom_jObject) iomObjA.addattrobj("TextImWeb", STRUCTD);
        Iom_jObject objStructS1 = (Iom_jObject) objStructD.addattrobj("LocalisedText", STRUCTS);
        objStructS1.setattrvalue("Language", "de");
        objStructS1.setattrvalue("Text", "https://XXXXXXXgeo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/65-Aedermannsdorf/Entscheide/65-5-E.pdf");
                
        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);
        LogCollector logger = new LogCollector();
        LogEventFactory errFactory = new LogEventFactory();
        Settings settings = new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isHttpResourceFromOerebMultilingualUri", IsHttpResourceFromOerebMultilingualUriIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        assertTrue(logger.getErrs().size()==1);
    }

}
