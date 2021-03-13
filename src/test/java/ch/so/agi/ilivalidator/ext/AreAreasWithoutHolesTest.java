package ch.so.agi.ilivalidator.ext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

public class AreAreasWithoutHolesTest {
    private TransferDescription td = null;
    private final static String OBJ_OID1 ="o1";
    private final static String OBJ_OID2 ="o2";
    private final static String ILI_TOPIC = "Testmodel.Topic";
    private final static String ILI_CLASSE = ILI_TOPIC+".ClassE";
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

    }
    
    @Test 
    public void area_Fail() throws Exception {
        Iom_jObject objSurface1 = new Iom_jObject(ILI_CLASSE, OBJ_OID1);
        IomObject multisurfaceValue1 = objSurface1.addattrobj("gebietseinteilung", "MULTISURFACE");
        IomObject surfaceValue1 = multisurfaceValue1.addattrobj("surface", "SURFACE");
        IomObject outerBoundary1 = surfaceValue1.addattrobj("boundary", "BOUNDARY");
        // polyline 1
        IomObject polylineValue1 = outerBoundary1.addattrobj("polyline", "POLYLINE");
        IomObject segments1 = polylineValue1.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment1 = segments1.addattrobj("segment", "COORD");
        startSegment1.setattrvalue("C1", "0.000");
        startSegment1.setattrvalue("C2", "0.000");
        IomObject endSegment1 = segments1.addattrobj("segment", "COORD");
        endSegment1.setattrvalue("C1", "0.000");
        endSegment1.setattrvalue("C2", "10.000");
        // polyline 2
        IomObject polylineValue2 = outerBoundary1.addattrobj("polyline", "POLYLINE");
        IomObject segments2 = polylineValue2.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment2 = segments2.addattrobj("segment", "COORD");
        startSegment2.setattrvalue("C1", "0.000");
        startSegment2.setattrvalue("C2", "10.000");
        IomObject endSegment2 = segments2.addattrobj("segment", "COORD");
        endSegment2.setattrvalue("C1", "10.000");
        endSegment2.setattrvalue("C2", "10.000");
        // polyline 3
        IomObject polylineValue3 = outerBoundary1.addattrobj("polyline", "POLYLINE");
        IomObject segments3 = polylineValue3.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment3 = segments3.addattrobj("segment", "COORD");
        startSegment3.setattrvalue("C1", "10.000");
        startSegment3.setattrvalue("C2", "10.000");
        IomObject endSegment3 = segments3.addattrobj("segment", "COORD");
        endSegment3.setattrvalue("C1", "10.000");
        endSegment3.setattrvalue("C2", "0.000");
        // polyline 4
        IomObject polylineValue4 = outerBoundary1.addattrobj("polyline", "POLYLINE");
        IomObject segments4 = polylineValue4.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment4 = segments4.addattrobj("segment", "COORD");
        startSegment4.setattrvalue("C1", "10.000");
        startSegment4.setattrvalue("C2", "0.000");
        IomObject endSegment4 = segments4.addattrobj("segment", "COORD");
        endSegment4.setattrvalue("C1", "0.000");
        endSegment4.setattrvalue("C2", "0.000");
        
        Iom_jObject objSurface2 = new Iom_jObject(ILI_CLASSE, OBJ_OID2);
        IomObject multisurfaceValue2 = objSurface2.addattrobj("gebietseinteilung", "MULTISURFACE");
        IomObject surfaceValue2 = multisurfaceValue2.addattrobj("surface", "SURFACE");
        IomObject outerBoundary2 = surfaceValue2.addattrobj("boundary", "BOUNDARY");
        // polyline 11
        IomObject polylineValue11 = outerBoundary2.addattrobj("polyline", "POLYLINE");
        IomObject segments11 = polylineValue11.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment11 = segments11.addattrobj("segment", "COORD");
        startSegment11.setattrvalue("C1", "0.000");
        startSegment11.setattrvalue("C2", "10.000");
        IomObject endSegment11 = segments11.addattrobj("segment", "COORD");
        endSegment11.setattrvalue("C1", "0.000");
        endSegment11.setattrvalue("C2", "20.000");
        // polyline 12
        IomObject polylineValue12 = outerBoundary2.addattrobj("polyline", "POLYLINE");
        IomObject segments12 = polylineValue12.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment12 = segments12.addattrobj("segment", "COORD");
        startSegment12.setattrvalue("C1", "0.000");
        startSegment12.setattrvalue("C2", "20.000");
        IomObject endSegment12 = segments12.addattrobj("segment", "COORD");
        endSegment12.setattrvalue("C1", "10.000");
        endSegment12.setattrvalue("C2", "20.000");
        // polyline 13
        IomObject polylineValue13 = outerBoundary2.addattrobj("polyline", "POLYLINE");
        IomObject segments13 = polylineValue13.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment13 = segments13.addattrobj("segment", "COORD");
        startSegment13.setattrvalue("C1", "10.000");
        startSegment13.setattrvalue("C2", "20.000");
        IomObject endSegment13 = segments13.addattrobj("segment", "COORD");
        endSegment13.setattrvalue("C1", "10.000");
        endSegment13.setattrvalue("C2", "10.000");
        // polyline 14
        IomObject polylineValue14 = outerBoundary2.addattrobj("polyline", "POLYLINE");
        IomObject segments14 = polylineValue14.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment14 = segments14.addattrobj("segment", "COORD");
        startSegment14.setattrvalue("C1", "10.000");
        startSegment14.setattrvalue("C2", "10.000");
        IomObject endSegment14 = segments14.addattrobj("segment", "COORD");
        endSegment14.setattrvalue("C1", "5.000");
        endSegment14.setattrvalue("C2", "11.000");
        // polyline 15
        IomObject polylineValue15 = outerBoundary2.addattrobj("polyline", "POLYLINE");
        IomObject segments15 = polylineValue15.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment15 = segments15.addattrobj("segment", "COORD");
        startSegment15.setattrvalue("C1", "5.000");
        startSegment15.setattrvalue("C2", "11.000");
        IomObject endSegment15 = segments15.addattrobj("segment", "COORD");
        endSegment15.setattrvalue("C1", "0.000");
        endSegment15.setattrvalue("C2", "10.000");
        
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.areAreasWithoutHoles", AreAreasWithoutHoles.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);        
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(objSurface1));
        validator.validate(new ObjectEvent(objSurface2));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());

    }
}
