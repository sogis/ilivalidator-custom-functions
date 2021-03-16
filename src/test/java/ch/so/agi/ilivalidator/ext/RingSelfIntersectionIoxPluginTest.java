package ch.so.agi.ilivalidator.ext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox_j.jts.Iox2jts;
import ch.interlis.iox_j.jts.Iox2jtsException;

public class RingSelfIntersectionIoxPluginTest {
    private TransferDescription td = null;
    private final static String OBJ_OID1 ="o1";
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
    public void findRingSelfIntersection() throws Exception {
        Iom_jObject objSurface = new Iom_jObject(ILI_CLASSE, OBJ_OID1);
        IomObject multisurfaceValue = objSurface.addattrobj("gebietseinteilung", "MULTISURFACE");
        IomObject surfaceValue = multisurfaceValue.addattrobj("surface", "SURFACE");
        IomObject outerBoundary = surfaceValue.addattrobj("boundary", "BOUNDARY");
        // polyline 1
        IomObject polylineValue1 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments1 = polylineValue1.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment1 = segments1.addattrobj("segment", "COORD");
        startSegment1.setattrvalue("C1", "0.000");
        startSegment1.setattrvalue("C2", "0.000");
        IomObject endSegment1 = segments1.addattrobj("segment", "COORD");
        endSegment1.setattrvalue("C1", "0.000");
        endSegment1.setattrvalue("C2", "10.000");
        // polyline 2
        IomObject polylineValue2 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments2 = polylineValue2.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment2 = segments2.addattrobj("segment", "COORD");
        startSegment2.setattrvalue("C1", "0.000");
        startSegment2.setattrvalue("C2", "10.000");
        IomObject endSegment2 = segments2.addattrobj("segment", "COORD");
        endSegment2.setattrvalue("C1", "5.000");
        endSegment2.setattrvalue("C2", "10.000");
//        // polyline 3
//        IomObject polylineValue3 = outerBoundary.addattrobj("polyline", "POLYLINE");
//        IomObject segments3 = polylineValue3.addattrobj("sequence", "SEGMENTS");
//        IomObject startSegment3 = segments3.addattrobj("segment", "COORD");
//        startSegment3.setattrvalue("C1", "5.000");
//        startSegment3.setattrvalue("C2", "10.000");
//        IomObject endSegment3 = segments3.addattrobj("segment", "COORD");
//        endSegment3.setattrvalue("C1", "4.000");
//        endSegment3.setattrvalue("C2", "9.000");
//        // polyline 4
//        IomObject polylineValue4 = outerBoundary.addattrobj("polyline", "POLYLINE");
//        IomObject segments4 = polylineValue4.addattrobj("sequence", "SEGMENTS");
//        IomObject startSegment4 = segments4.addattrobj("segment", "COORD");
//        startSegment4.setattrvalue("C1", "4.000");
//        startSegment4.setattrvalue("C2", "9.000");
//        IomObject endSegment4 = segments4.addattrobj("segment", "COORD");
//        endSegment4.setattrvalue("C1", "6.000");
//        endSegment4.setattrvalue("C2", "9.000");
//        // polyline 5
//        IomObject polylineValue5 = outerBoundary.addattrobj("polyline", "POLYLINE");
//        IomObject segments5 = polylineValue5.addattrobj("sequence", "SEGMENTS");
//        IomObject startSegment5 = segments5.addattrobj("segment", "COORD");
//        startSegment5.setattrvalue("C1", "6.000");
//        startSegment5.setattrvalue("C2", "9.000");
//        IomObject endSegment5 = segments5.addattrobj("segment", "COORD");
//        endSegment5.setattrvalue("C1", "5.000");
//        endSegment5.setattrvalue("C2", "10.000");
        // polyline 6
        IomObject polylineValue6 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments6 = polylineValue6.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment6 = segments6.addattrobj("segment", "COORD");
        startSegment6.setattrvalue("C1", "5.000");
        startSegment6.setattrvalue("C2", "10.000");
        IomObject endSegment6 = segments6.addattrobj("segment", "COORD");
        endSegment6.setattrvalue("C1", "10.000");
        endSegment6.setattrvalue("C2", "10.000");
        // polyline 7
        IomObject polylineValue7 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments7 = polylineValue7.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment7 = segments7.addattrobj("segment", "COORD");
        startSegment7.setattrvalue("C1", "10.000");
        startSegment7.setattrvalue("C2", "10.000");
        IomObject endSegment7 = segments7.addattrobj("segment", "COORD");
        endSegment7.setattrvalue("C1", "10.000");
        endSegment7.setattrvalue("C2", "0.000");
        // polyline 8
        IomObject polylineValue8 = outerBoundary.addattrobj("polyline", "POLYLINE");
        IomObject segments8 = polylineValue8.addattrobj("sequence", "SEGMENTS");
        IomObject startSegment8 = segments8.addattrobj("segment", "COORD");
        startSegment8.setattrvalue("C1", "10.000");
        startSegment8.setattrvalue("C2", "0.000");
        IomObject endSegment8 = segments8.addattrobj("segment", "COORD");
        endSegment8.setattrvalue("C1", "0.000");
        endSegment8.setattrvalue("C2", "0.000");
        
        System.out.println(objSurface);

        //?? 
        Geometry g = Iox2jts.surface2JTS(multisurfaceValue, 0);
        System.out.println(g);
        System.out.println(g.isValid());
        
        Geometry p = new WKTReader().read("POLYGON ((0 0, 0 10, 5 10, 10 10, 10 0, 0 0))");
        System.out.println(p);
        System.out.println(p.getArea());
        System.out.println(p.isValid());
        
    }

}
