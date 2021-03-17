package ch.so.agi.ilivalidator.ext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.LocalAttribute;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.jts.Iox2jtsException;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

public class RingSelfIntersectionIoxPlugin implements InterlisFunction {

    public static final double strokeP = 0.002;

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
        

        IomObject xtfGeom = (IomObject) actualArguments[0].getComplexObjects().toArray()[0];
        String currentObjectTag = mainObj.getobjecttag();
        String geomType = GeometryUtils.getGeometryType(xtfGeom, mainObj, tag2class, td);
        LocalAttribute localAttr = null;

        if (geomType == null) {
            logger.addEvent(logger.logErrorMsg("Given attribute is not a valid geometry type"));
            return Value.createSkipEvaluation();
        }
        
        // Find geometry attribute name
        Object modelele = tag2class.get(currentObjectTag);
        Viewable aclass = (Viewable) modelele;
        Iterator iter = aclass.getAttributes();
        while (iter.hasNext()) {
            LocalAttribute attr = (LocalAttribute) iter.next();
            String attrName = attr.getName();
            IomObject attVal = mainObj.getattrobj(attrName, 0);
            if (attVal != null && attVal.equals(xtfGeom)) {
                localAttr = attr;
                break;
            }
        }

        Geometry geometryObject;
        try {
            geometryObject = GeometryUtils.geometry2JTS(xtfGeom, localAttr, geomType, strokeP, td);
        } catch (IllegalArgumentException e) {
            logger.addEvent(logger.logErrorMsg("Given attribute is not a valid multisurface type"));
            return Value.createSkipEvaluation();
        } catch (Iox2jtsException e) {
            logger.addEvent(logger.logErrorMsg(e.getMessage()));
            return Value.createSkipEvaluation();            
        }

        GeometryFactory fact = geometryObject.getFactory();
        Coordinate[] coords = GeometryUtils.removeDuplicatePoints(((Polygon)geometryObject).getExteriorRing().getCoordinates());
        LinearRing r = fact.createLinearRing(coords);

        boolean ringSelfIntersectionFound = false;
        Map<Coordinate,Coordinate> coordinateCache = new HashMap<Coordinate,Coordinate>();
        for (int i=0; i<coords.length; i++) {
            if (i==0) {
                // Erste und letzte Koordinate sind identisch: Ignorieren der ersten
                // Koordinate.
                continue;
            }
            
            if (coordinateCache.containsKey(coords[i])) {
                logger.addEvent(logger.logErrorMsg("ring self intersection found at: " + coords[i].toString()));
                ringSelfIntersectionFound = true;
            } else {
                coordinateCache.put(coords[i], coords[i]);
            }
        }

        if (ringSelfIntersectionFound) {
            return new Value(true);

        } else {
            return new Value(false);            
        }
        
    }

    @Override
    public String getQualifiedIliName() {
        return "SO_FunctionsExt.hasRingSelfIntersection";
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
