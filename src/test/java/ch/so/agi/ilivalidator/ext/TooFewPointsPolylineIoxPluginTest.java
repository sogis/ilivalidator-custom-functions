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

public class TooFewPointsPolylineIoxPluginTest {
    private TransferDescription td = null;
    private final static String OBJ_OID1 ="o1";
    private final static String ILI_TOPIC = "Testmodel.Topic";
    private final static String ILI_CLASSF = ILI_TOPIC+".ClassF";
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
    public void polylineWithTooFewPoints() {
        Iom_jObject objStraights=new Iom_jObject(ILI_CLASSF, OBJ_OID1);
        IomObject polylineValue=objStraights.addattrobj("linie", "POLYLINE");
        IomObject segments=polylineValue.addattrobj("sequence", "SEGMENTS");
        IomObject coordStart=segments.addattrobj("segment", "COORD");
        coordStart.setattrvalue("C1", "10.000");
        coordStart.setattrvalue("C2", "10.000");
        
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.tooFewPointsPolyline", TooFewPointsPolylineIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);        
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(objStraights));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        assertTrue(logger.getErrs().size()==2);
    }

    @Test
    public void validPolyline() {
        Iom_jObject objStraights=new Iom_jObject(ILI_CLASSF, OBJ_OID1);
        IomObject polylineValue=objStraights.addattrobj("linie", "POLYLINE");
        IomObject segments=polylineValue.addattrobj("sequence", "SEGMENTS");
        IomObject coordStart=segments.addattrobj("segment", "COORD");
        coordStart.setattrvalue("C1", "10.000");
        coordStart.setattrvalue("C2", "10.000");
        IomObject coordEnd=segments.addattrobj("segment", "COORD");
        coordEnd.setattrvalue("C1", "20.000");
        coordEnd.setattrvalue("C2", "20.000");

        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.tooFewPointsPolyline", TooFewPointsPolylineIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);        
        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(objStraights));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        assertTrue(logger.getErrs().size()==0);
    }
}
