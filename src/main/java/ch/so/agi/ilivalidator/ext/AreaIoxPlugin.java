package ch.so.agi.ilivalidator.ext;

import java.util.HashMap;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Geometry;

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

// For the geometry handling see: https://github.com/AgenciaImplementacion/iliValidator_custom_plugins/blob/master/src/main/java/co/interlis/topology/ContainsIoxPlugin.java
public class AreaIoxPlugin implements InterlisFunction {    
    
    public static final double strokeP = 0.002;

    private LogEventFactory logger = null;
    private HashMap tag2class = null;
    private TransferDescription td = null;

    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        if (actualArguments[0].skipEvaluation()) {
            return actualArguments[0];
        }
        if (actualArguments[0].isUndefined()) {
            return Value.createSkipEvaluation();
        }

        // TODO:
        // Ich denke, das ist nicht korrekt. Es wird eine Menge von Objekte einer Klasse übergeben.
        // Ich verwende aber nur das erste?!
        IomObject xtfGeom = (IomObject) actualArguments[0].getComplexObjects().toArray()[0];
        String currentObjectTag = mainObj.getobjecttag();
        String geomType = GeometryUtils.getGeometryType(xtfGeom, mainObj, tag2class, td);
        LocalAttribute localAttr = null;

        if (geomType == null) {
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
            logger.addEvent(logger.logErrorMsg("Given attribute is not a valid geometry"));
            return Value.createSkipEvaluation();
        } catch (Iox2jtsException e) {
            logger.addEvent(logger.logErrorMsg(e.getMessage()));
            return Value.createSkipEvaluation();            
        }

        return new Value(geometryObject.getArea());
    }
    
    @Override
    public String getQualifiedIliName() {
        return "SO_FunctionsExt.area";
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
