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
import ch.so.agi.ilivalidator.ext.LogCollector;

public class IsValidDocumentsCycleIoxPluginTest {
    private TransferDescription td=null;
    // OID
    private final static String OBJ_OID1 ="o1";
    private final static String OBJ_OID2 ="o2";
    private final static String OBJ_OID3 ="o3";
    private final static String OBJ_OID4 ="o4";
    private final static String OBJ_OID5 ="o5";
    private final static String OBJ_OID6 ="o6";
    private final static String OBJ_OID7 ="o7";
    private final static String OBJ_OID8 ="o8";
    private final static String OBJ_OID9 ="o9";
    // MODEL
    private final static String ILI_TOPIC="OEREB.Vorschriften";
    // CLASS
    private final static String ILI_CLASSA=ILI_TOPIC+".Dokument";
    // ASSOCIATION
    private final static String ILI_ASSOC_A_A=ILI_TOPIC+".HinweisWeitereDokumente";
    private final static String ILI_ASSOC_AA_A_URSPRUNG = "Ursprung"; 
    private final static String ILI_ASSOC_AA_A_HINWEIS = "Hinweis"; 
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
            FileEntry fileEntry = new FileEntry("src/test/data/OEREB-mini.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry); 
        }
        td = ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        assertNotNull(td);
    }
    
    @Test
    public void noSubgraph_Ok() {
        Iom_jObject iomObjA_1=new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA_1.setattrvalue("TextImWeb", "doc_1.pdf");
        iomObjA_1.setattrvalue("Titel", "RRB");        
        Iom_jObject iomObjA_2=new Iom_jObject(ILI_CLASSA, OBJ_OID2);
        iomObjA_2.setattrvalue("TextImWeb", "doc_2.pdf");
        iomObjA_2.setattrvalue("Titel", "RRB");                
        Iom_jObject iomObjA_3=new Iom_jObject(ILI_CLASSA, OBJ_OID3);
        iomObjA_3.setattrvalue("TextImWeb", "doc_3.pdf");
        iomObjA_3.setattrvalue("Titel", "RRB");     
                
        Iom_jObject o1Ref=new Iom_jObject("REF", null);
        o1Ref.setobjectrefoid(OBJ_OID1);
        Iom_jObject o2Ref=new Iom_jObject("REF", null);
        o2Ref.setobjectrefoid(OBJ_OID2);
        Iom_jObject o3Ref=new Iom_jObject("REF", null);
        o3Ref.setobjectrefoid(OBJ_OID3);

        Iom_jObject iomLinkAA_12=new Iom_jObject(ILI_ASSOC_A_A, null);        
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o1Ref);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o2Ref);
        
        Iom_jObject iomLinkAA_23=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o2Ref);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o3Ref);

        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isValidDocumentsCycle", IsValidDocumentsCycleIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);

        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA_1));
        validator.validate(new ObjectEvent(iomObjA_2));
        validator.validate(new ObjectEvent(iomObjA_3));
        validator.validate(new ObjectEvent(iomLinkAA_12));
        validator.validate(new ObjectEvent(iomLinkAA_23));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());

        assertTrue(logger.getErrs().size()==0);
    }
    
    @Test
    public void twoSubgraph_Ok() {
        Iom_jObject iomObjA_1=new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA_1.setattrvalue("TextImWeb", "doc_1.pdf");
        iomObjA_1.setattrvalue("Titel", "RRB");        
        Iom_jObject iomObjA_2=new Iom_jObject(ILI_CLASSA, OBJ_OID2);
        iomObjA_2.setattrvalue("TextImWeb", "doc_2.pdf");
        iomObjA_2.setattrvalue("Titel", "RRB");                
        Iom_jObject iomObjA_3=new Iom_jObject(ILI_CLASSA, OBJ_OID3);
        iomObjA_3.setattrvalue("TextImWeb", "doc_3.pdf");
        iomObjA_3.setattrvalue("Titel", "RRB");    
        
        Iom_jObject iomObjA_4=new Iom_jObject(ILI_CLASSA, OBJ_OID4);
        iomObjA_4.setattrvalue("TextImWeb", "doc_4.pdf");
        iomObjA_4.setattrvalue("Titel", "RRB");                                
        Iom_jObject iomObjA_5=new Iom_jObject(ILI_CLASSA, OBJ_OID5);
        iomObjA_5.setattrvalue("TextImWeb", "doc_5.pdf");
        iomObjA_5.setattrvalue("Titel", "RRB");                                
        Iom_jObject iomObjA_6=new Iom_jObject(ILI_CLASSA, OBJ_OID6);
        iomObjA_6.setattrvalue("TextImWeb", "doc_5.pdf");
        iomObjA_6.setattrvalue("Titel", "RRB");                                

        Iom_jObject o1Ref=new Iom_jObject("REF", null);
        o1Ref.setobjectrefoid(OBJ_OID1);
        Iom_jObject o2Ref=new Iom_jObject("REF", null);
        o2Ref.setobjectrefoid(OBJ_OID2);
        Iom_jObject o3Ref=new Iom_jObject("REF", null);
        o3Ref.setobjectrefoid(OBJ_OID3);
        Iom_jObject o4Ref=new Iom_jObject("REF", null);
        o4Ref.setobjectrefoid(OBJ_OID4);
        Iom_jObject o5Ref=new Iom_jObject("REF", null);
        o5Ref.setobjectrefoid(OBJ_OID5);
        Iom_jObject o6Ref=new Iom_jObject("REF", null);
        o6Ref.setobjectrefoid(OBJ_OID6);

        Iom_jObject iomLinkAA_12=new Iom_jObject(ILI_ASSOC_A_A, null);        
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o1Ref);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o2Ref);
        
        Iom_jObject iomLinkAA_23=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o2Ref);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o3Ref);

        Iom_jObject iomLinkAA_45=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_45.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o4Ref);
        iomLinkAA_45.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o5Ref);

        Iom_jObject iomLinkAA_56=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_56.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o5Ref);
        iomLinkAA_56.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o6Ref);

        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isValidDocumentsCycle", IsValidDocumentsCycleIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);

        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA_1));
        validator.validate(new ObjectEvent(iomObjA_2));
        validator.validate(new ObjectEvent(iomObjA_3));
        validator.validate(new ObjectEvent(iomObjA_4));
        validator.validate(new ObjectEvent(iomObjA_5));
        validator.validate(new ObjectEvent(iomObjA_6));        
        validator.validate(new ObjectEvent(iomLinkAA_12));
        validator.validate(new ObjectEvent(iomLinkAA_23));
        validator.validate(new ObjectEvent(iomLinkAA_45));
        validator.validate(new ObjectEvent(iomLinkAA_56));        
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());

        assertTrue(logger.getErrs().size()==0);
    }
    
    // This case is handled with built-in ilivalidator capabilities.
    @Test
    public void duplicateEdge_Fail() {
        Iom_jObject iomObjA_1=new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA_1.setattrvalue("TextImWeb", "doc_1.pdf");
        iomObjA_1.setattrvalue("Titel", "RRB");        
        Iom_jObject iomObjA_2=new Iom_jObject(ILI_CLASSA, OBJ_OID2);
        iomObjA_2.setattrvalue("TextImWeb", "doc_2.pdf");
        iomObjA_2.setattrvalue("Titel", "RRB");                
        Iom_jObject iomObjA_3=new Iom_jObject(ILI_CLASSA, OBJ_OID3);
        iomObjA_3.setattrvalue("TextImWeb", "doc_3.pdf");
        iomObjA_3.setattrvalue("Titel", "RRB");                        

        Iom_jObject o1Ref=new Iom_jObject("REF", null);
        o1Ref.setobjectrefoid(OBJ_OID1);
        Iom_jObject o2Ref=new Iom_jObject("REF", null);
        o2Ref.setobjectrefoid(OBJ_OID2);
        Iom_jObject o3Ref=new Iom_jObject("REF", null);
        o3Ref.setobjectrefoid(OBJ_OID3);
        Iom_jObject o3RefDuplicate=new Iom_jObject("REF", null);
        o3RefDuplicate.setobjectrefoid(OBJ_OID3);

        Iom_jObject iomLinkAA_12=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o1Ref);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o2Ref);
        
        Iom_jObject iomLinkAA_23=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o2Ref);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o3Ref);

        Iom_jObject iomLinkAA_23_duplicate=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_23_duplicate.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o2Ref);
        iomLinkAA_23_duplicate.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o3RefDuplicate);

        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isValidDocumentsCycle", IsValidDocumentsCycleIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);

        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA_1));
        validator.validate(new ObjectEvent(iomObjA_2));
        validator.validate(new ObjectEvent(iomObjA_3));
        validator.validate(new ObjectEvent(iomLinkAA_12));
        validator.validate(new ObjectEvent(iomLinkAA_23));
        validator.validate(new ObjectEvent(iomLinkAA_23_duplicate));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        assertTrue(logger.getErrs().size()==1);
        assertTrue(logger.getErrs().get(0).getEventMsg().equals("OID OEREB.Vorschriften.HinweisWeitereDokumente:o2:o3 of object OEREB.Vorschriften.HinweisWeitereDokumente already exists in OEREB.Vorschriften.HinweisWeitereDokumente."));        
    }
    
    @Test
    public void selfLoop_Fail() {
        Iom_jObject iomObjA_1=new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA_1.setattrvalue("TextImWeb", "doc_1.pdf");
        iomObjA_1.setattrvalue("Titel", "RRB");        
        Iom_jObject iomObjA_2=new Iom_jObject(ILI_CLASSA, OBJ_OID2);
        iomObjA_2.setattrvalue("TextImWeb", "doc_2.pdf");
        iomObjA_2.setattrvalue("Titel", "RRB");                
        Iom_jObject iomObjA_3=new Iom_jObject(ILI_CLASSA, OBJ_OID3);
        iomObjA_3.setattrvalue("TextImWeb", "doc_3.pdf");
        iomObjA_3.setattrvalue("Titel", "RRB");    
        
        Iom_jObject o1Ref=new Iom_jObject("REF", null);
        o1Ref.setobjectrefoid(OBJ_OID1);
        Iom_jObject o2Ref=new Iom_jObject("REF", null);
        o2Ref.setobjectrefoid(OBJ_OID2);

        Iom_jObject iomLinkAA_12=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o1Ref);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o2Ref);
        
        Iom_jObject iomLinkAA_22=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_22.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o2Ref);
        iomLinkAA_22.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o2Ref);

        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isValidDocumentsCycle", IsValidDocumentsCycleIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);

        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA_1));
        validator.validate(new ObjectEvent(iomObjA_2));
        validator.validate(new ObjectEvent(iomObjA_3));
        validator.validate(new ObjectEvent(iomLinkAA_12));
        validator.validate(new ObjectEvent(iomLinkAA_22));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());

        assertTrue(logger.getErrs().size()==2);
        assertTrue(logger.getErrs().get(0).getEventMsg().equals("self loop found: o2"));
        assertTrue(logger.getErrs().get(1).getEventMsg().equals("Set Constraint OEREB.Vorschriften.HinweisWeitereDokumente.DocumentsCycleCheck is not true."));
    }
    
    @Test
    public void cycle_Fail() {
        Iom_jObject iomObjA_1=new Iom_jObject(ILI_CLASSA, OBJ_OID1);
        iomObjA_1.setattrvalue("TextImWeb", "doc_1.pdf");
        iomObjA_1.setattrvalue("Titel", "RRB");        
        Iom_jObject iomObjA_2=new Iom_jObject(ILI_CLASSA, OBJ_OID2);
        iomObjA_2.setattrvalue("TextImWeb", "doc_2.pdf");
        iomObjA_2.setattrvalue("Titel", "RRB");                
        Iom_jObject iomObjA_3=new Iom_jObject(ILI_CLASSA, OBJ_OID3);
        iomObjA_3.setattrvalue("TextImWeb", "doc_3.pdf");
        iomObjA_3.setattrvalue("Titel", "RRB");                        
        Iom_jObject iomObjA_4=new Iom_jObject(ILI_CLASSA, OBJ_OID4);
        iomObjA_4.setattrvalue("TextImWeb", "doc_4.pdf");
        iomObjA_4.setattrvalue("Titel", "RRB");                                
        Iom_jObject iomObjA_5=new Iom_jObject(ILI_CLASSA, OBJ_OID5);
        iomObjA_5.setattrvalue("TextImWeb", "doc_5.pdf");
        iomObjA_5.setattrvalue("Titel", "RRB");                                
        Iom_jObject iomObjA_6=new Iom_jObject(ILI_CLASSA, OBJ_OID6);
        iomObjA_6.setattrvalue("TextImWeb", "doc_5.pdf");
        iomObjA_6.setattrvalue("Titel", "RRB");                                
        Iom_jObject iomObjA_7=new Iom_jObject(ILI_CLASSA, OBJ_OID7);
        iomObjA_7.setattrvalue("TextImWeb", "doc_7.pdf");
        iomObjA_7.setattrvalue("Titel", "RRB");    
        
        Iom_jObject o1Ref=new Iom_jObject("REF", null);
        o1Ref.setobjectrefoid(OBJ_OID1);
        Iom_jObject o2Ref=new Iom_jObject("REF", null);
        o2Ref.setobjectrefoid(OBJ_OID2);
        Iom_jObject o3Ref=new Iom_jObject("REF", null);
        o3Ref.setobjectrefoid(OBJ_OID3);
        Iom_jObject o4Ref=new Iom_jObject("REF", null);
        o4Ref.setobjectrefoid(OBJ_OID4);
        Iom_jObject o5Ref=new Iom_jObject("REF", null);
        o5Ref.setobjectrefoid(OBJ_OID5);
        Iom_jObject o6Ref=new Iom_jObject("REF", null);
        o6Ref.setobjectrefoid(OBJ_OID6);
        Iom_jObject o7Ref=new Iom_jObject("REF", null);
        o7Ref.setobjectrefoid(OBJ_OID7);

        Iom_jObject iomLinkAA_12=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o1Ref);
        iomLinkAA_12.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o2Ref);
        
        Iom_jObject iomLinkAA_23=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o2Ref);
        iomLinkAA_23.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o3Ref);
        
        Iom_jObject iomLinkAA_34=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_34.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o3Ref);
        iomLinkAA_34.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o4Ref);

        Iom_jObject iomLinkAA_45=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_45.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o4Ref);
        iomLinkAA_45.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o5Ref);

        Iom_jObject iomLinkAA_56=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_56.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o5Ref);
        iomLinkAA_56.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o6Ref);
        
        Iom_jObject iomLinkAA_52=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_52.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o5Ref);
        iomLinkAA_52.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o2Ref);

        Iom_jObject iomLinkAA_37=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_37.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o3Ref);
        iomLinkAA_37.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o7Ref);

        Iom_jObject iomLinkAA_24=new Iom_jObject(ILI_ASSOC_A_A, null);
        iomLinkAA_24.addattrobj(ILI_ASSOC_AA_A_URSPRUNG, o2Ref);
        iomLinkAA_24.addattrobj(ILI_ASSOC_AA_A_HINWEIS, o4Ref);

        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        
        Map<String,Class> newFunctions = new HashMap<String,Class>();
        newFunctions.put("SO_FunctionsExt.isValidDocumentsCycle", IsValidDocumentsCycleIoxPlugin.class);
        settings.setTransientObject(Validator.CONFIG_CUSTOM_FUNCTIONS, newFunctions);

        Validator validator=new Validator(td, modelConfig, logger, errFactory, new PipelinePool(), settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC,BID1));
        validator.validate(new ObjectEvent(iomObjA_1));
        validator.validate(new ObjectEvent(iomObjA_2));
        validator.validate(new ObjectEvent(iomObjA_3));
        validator.validate(new ObjectEvent(iomObjA_4));
        validator.validate(new ObjectEvent(iomObjA_5));
        validator.validate(new ObjectEvent(iomObjA_6));
        validator.validate(new ObjectEvent(iomObjA_7));
        validator.validate(new ObjectEvent(iomLinkAA_12));
        validator.validate(new ObjectEvent(iomLinkAA_23));
        validator.validate(new ObjectEvent(iomLinkAA_34));
        validator.validate(new ObjectEvent(iomLinkAA_45));
        validator.validate(new ObjectEvent(iomLinkAA_56));
        validator.validate(new ObjectEvent(iomLinkAA_52));
        validator.validate(new ObjectEvent(iomLinkAA_37));
        validator.validate(new ObjectEvent(iomLinkAA_24));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        
        // Error count is different with newer iox-ili versions:
        // Set Contraint message appears only once.        
        assertTrue(logger.getErrs().size()==6);
        assertTrue(logger.getErrs().get(0).getEventMsg().equals("(o5 <-> o2) is part of a cycle: o2,o3,o4,o5."));
        assertTrue(logger.getErrs().get(1).getEventMsg().equals("Set Constraint OEREB.Vorschriften.HinweisWeitereDokumente.DocumentsCycleCheck is not true."));
    }
}
