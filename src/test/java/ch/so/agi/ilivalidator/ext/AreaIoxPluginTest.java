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

public class AreaIoxPluginTest {
    private TransferDescription td = null;
    private final static String OBJ_OID1 ="o1";
    private final static String ILI_TOPIC = "Testmodel.Topic";
    private final static String ILI_CLASSC = ILI_TOPIC+".ClassC";
    private final static String BID1 = "b1";

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
    public void area_Ok() throws Exception {
        Iom_jObject objSurface = new Iom_jObject(ILI_CLASSC, OBJ_OID1);
        IomObject multisurfaceValue = objSurface.addattrobj("flaeche", "MULTISURFACE");
        IomObject surfaceValue = multisurfaceValue.addattrobj("surface", "SURFACE");
        IomObject outerBoundary = surfaceValue.addattrobj("boundary", "BOUNDARY");
        // polyline
        IomObject polylineValue = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments = polylineValue.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment = segments.addattrobj("segment", "COORD");
        startSegment.setattrvalue("C1", "0.000");
        startSegment.setattrvalue("C2", "0.000");
        IomObject endSegment = segments.addattrobj("segment", "COORD");
        endSegment.setattrvalue("C1", "0.000");
        endSegment.setattrvalue("C2", "10.000");
        // polyline 2
        IomObject polylineValue2 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments2 = polylineValue2.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment2 = segments2.addattrobj("segment", "COORD");
        startSegment2.setattrvalue("C1", "0.000");
        startSegment2.setattrvalue("C2", "10.000");
        IomObject endSegment2 = segments2.addattrobj("segment", "COORD");
        endSegment2.setattrvalue("C1", "10.000");
        endSegment2.setattrvalue("C2", "10.000");
        // polyline 3
        IomObject polylineValue3 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments3 = polylineValue3.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment3 = segments3.addattrobj("segment", "COORD");
        startSegment3.setattrvalue("C1", "10.000");
        startSegment3.setattrvalue("C2", "10.000");
        IomObject endSegment3 = segments3.addattrobj("segment", "COORD");
        endSegment3.setattrvalue("C1", "0.000");
        endSegment3.setattrvalue("C2", "0.000");
        
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.area", AreaIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);        
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(objSurface));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());

        assertTrue(logger.getErrs().size()==0);
    }
    
    @Test
    public void area_Fail() throws Exception {
        Iom_jObject objSurface = new Iom_jObject(ILI_CLASSC, OBJ_OID1);
        IomObject multisurfaceValue = objSurface.addattrobj("flaeche", "MULTISURFACE");
        IomObject surfaceValue = multisurfaceValue.addattrobj("surface", "SURFACE");
        IomObject outerBoundary = surfaceValue.addattrobj("boundary", "BOUNDARY");
        // polyline
        IomObject polylineValue = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments = polylineValue.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment = segments.addattrobj("segment", "COORD");
        startSegment.setattrvalue("C1", "0.000");
        startSegment.setattrvalue("C2", "0.000");
        IomObject endSegment = segments.addattrobj("segment", "COORD");
        endSegment.setattrvalue("C1", "0.000");
        endSegment.setattrvalue("C2", "1.000");
        // polyline 2
        IomObject polylineValue2 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments2 = polylineValue2.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment2 = segments2.addattrobj("segment", "COORD");
        startSegment2.setattrvalue("C1", "0.000");
        startSegment2.setattrvalue("C2", "1.000");
        IomObject endSegment2 = segments2.addattrobj("segment", "COORD");
        endSegment2.setattrvalue("C1", "1.000");
        endSegment2.setattrvalue("C2", "1.000");
        // polyline 3
        IomObject polylineValue3 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments3 = polylineValue3.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment3 = segments3.addattrobj("segment", "COORD");
        startSegment3.setattrvalue("C1", "1.000");
        startSegment3.setattrvalue("C2", "1.000");
        IomObject endSegment3 = segments3.addattrobj("segment", "COORD");
        endSegment3.setattrvalue("C1", "0.000");
        endSegment3.setattrvalue("C2", "0.000");
        
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.area", AreaIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);        
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(objSurface));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());

        assertTrue(logger.getErrs().size()==2);
    }

}
