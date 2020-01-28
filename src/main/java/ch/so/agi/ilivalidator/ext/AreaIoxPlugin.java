package ch.so.agi.ilivalidator.ext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.CompositionType;
import ch.interlis.ili2c.metamodel.CoordType;
import ch.interlis.ili2c.metamodel.LocalAttribute;
import ch.interlis.ili2c.metamodel.SurfaceType;
import ch.interlis.ili2c.metamodel.Table;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.ili2c.metamodel.Type;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.jts.Iox2jts;
import ch.interlis.iox_j.jts.Iox2jtsException;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;
import ch.interlis.iox_j.wkb.Iox2wkb;
import ch.interlis.iox_j.wkb.Iox2wkbException;
import ch.ehi.ili2db.mapping.MultiSurfaceMappings;

import java.util.logging.Level;
import java.util.logging.Logger;

// For the geometry handling see: https://github.com/AgenciaImplementacion/iliValidator_custom_plugins/blob/master/src/main/java/co/interlis/topology/ContainsIoxPlugin.java
public class AreaIoxPlugin implements InterlisFunction {
    public static final String POLYLINE_TYPE = "PolylineType";
    public static final String SURFACE_OR_AREA_TYPE = "SurfaceOrAreaType";
    public static final String MULTI_SURFACE_TYPE = "MultiSurfaceType";
    public static final String COORD_TYPE = "CoordType";

    public static final String METAATTR_MAPPING = "ili2db.mapping";
    public static final String METAATTR_MAPPING_MULTISURFACE = "MultiSurface";
    public static final String METAATTR_MAPPING_MULTILINE = "MultiLine";
    public static final String METAATTR_MAPPING_MULTIPOINT = "MultiPoint";
    public static final String METAATTR_MAPPING_ARRAY = "ARRAY";
    public static final String METAATTR_DISPNAME = "ili2db.dispName";
    
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

        IomObject xtfGeom = (IomObject) actualArguments[0].getComplexObjects().toArray()[0];
        String currentObjectTag = mainObj.getobjecttag();
        String geomType = getGeometryType(xtfGeom, mainObj);
        LocalAttribute localAttr = null;

        if (geomType == null) {
            // TODO: Is there a better handling of this case?
            return new Value(0.0);
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

        Geometry geometryObject = geometry2JTS(xtfGeom, localAttr, geomType, strokeP);

//        System.out.println(localAttr.toString());
//        System.out.println(geometryObject.getArea());
       
        

        
        return new Value(geometryObject.getArea());
    }
    
    public Geometry geometry2JTS(IomObject object, LocalAttribute attr, String geometryType, double p) {
        Geometry geometry = null;
        try {
            switch (geometryType) {
                case POLYLINE_TYPE:
                    geometry = new GeometryFactory().createLineString(Iox2jts.polyline2JTS(object, false, 0).toCoordinateArray());
                    break;
                case SURFACE_OR_AREA_TYPE:
                    geometry = Iox2jts.surface2JTS(object, p);
                    break;
                case COORD_TYPE:
                    geometry = new GeometryFactory().createPoint(Iox2jts.coord2JTS(object));
                    break;
                case MULTI_SURFACE_TYPE:
                    Type type = attr.getDomain();
                    if (isMultiSurfaceAttr(td, attr)) {
                        MultiSurfaceMappings multiSurfaceAttrs = new MultiSurfaceMappings();
                        multiSurfaceAttrs.addMultiSurfaceAttr(attr);
                        ch.ehi.ili2db.mapping.MultiSurfaceMapping attrMapping = multiSurfaceAttrs.getMapping(attr);

                        IomObject iomMultisurface = null;
                        if (object != null) {

                            int surfacec = object.getattrvaluecount(attrMapping.getBagOfSurfacesAttrName());
                            for (int surfacei = 0; surfacei < surfacec; surfacei++) {

                                IomObject iomSurfaceStructure = object.getattrobj(attrMapping.getBagOfSurfacesAttrName(), surfacei);
                                IomObject iomPoly = iomSurfaceStructure.getattrobj(attrMapping.getSurfaceAttrName(), 0);
                                IomObject iomSurface = iomPoly.getattrobj("surface", 0);
                                if (iomMultisurface == null) {
                                    iomMultisurface = new ch.interlis.iom_j.Iom_jObject("MULTISURFACE", null);
                                }
                                iomMultisurface.addattrobj("surface", iomSurface);

                                try {
                                    Geometry g = Iox2jts.surface2JTS(iomSurface, 0);
                                } catch (Iox2jtsException e) {
                                    Logger.getLogger(AreaIoxPlugin.class.getName()).log(Level.SEVERE, null, e);
                                }
                            }
                        }

                        if (iomMultisurface != null) {
                            try {
                                AttributeDef surfaceAttr = getMultiSurfaceAttrDef(type, attrMapping);
                                SurfaceType surface = ((SurfaceType) surfaceAttr.getDomainResolvingAliases());
                                CoordType coord = (CoordType) surface.getControlPointDomain().getType();
                                boolean is3D = coord.getDimensions().length == 3;

                                Iox2wkb conv = new Iox2wkb(is3D ? 3 : 2);
                                Object geomObj = conv.multisurface2wkb(iomMultisurface, surface.getLineAttributeStructure() != null, strokeP);
                                byte bv[] = (byte[]) geomObj;
                                geometry = new WKBReader().read(bv);
                            } catch (Iox2wkbException | ParseException ex) {
                                Logger.getLogger(AreaIoxPlugin.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            logger.addEvent(logger.logErrorMsg("Given attribute is not a valid multisurface type"));
                        }
                    }
                    break;
            }
        } catch (Iox2jtsException e) {
            logger.addEvent(logger.logErrorMsg(e.getMessage()));
        }
        return geometry;
    }

    private AttributeDef getMultiSurfaceAttrDef(Type type, ch.ehi.ili2db.mapping.MultiSurfaceMapping attrMapping) {
        Table multiSurfaceType = ((CompositionType) type).getComponentType();
        Table surfaceStructureType = ((CompositionType) ((AttributeDef) multiSurfaceType.getElement(AttributeDef.class, attrMapping.getBagOfSurfacesAttrName())).getDomain()).getComponentType();
        AttributeDef surfaceAttr = (AttributeDef) surfaceStructureType.getElement(AttributeDef.class, attrMapping.getSurfaceAttrName());
        return surfaceAttr;
    }

    private String getGeometryType(IomObject xtfGeom, IomObject mainObj) {
        String geomType = null;
        
        boolean isPolygon = xtfGeom.getattrvaluecount("surface") > 0;
        boolean isLine = xtfGeom.getattrvaluecount("sequence") > 0;

        if (isPolygon) {
            geomType = SURFACE_OR_AREA_TYPE;
        } else if (isLine) {
            geomType = POLYLINE_TYPE;
        } else {
            String c1 = xtfGeom.getattrvalue("C1");
            String c2 = xtfGeom.getattrvalue("C2");
            if (c1 != null && c2 != null) {
                geomType = COORD_TYPE;
            } else {
                String currentObjectTag = mainObj.getobjecttag();
                Object modelele = tag2class.get(currentObjectTag);
                Viewable aclass = (Viewable) modelele;
                Iterator iter = aclass.getAttributes();
                while (iter.hasNext()) {
                    LocalAttribute attr = (LocalAttribute) iter.next();
                    String attrName = attr.getName();
                    IomObject attVal = mainObj.getattrobj(attrName, 0);
                    if (attVal != null && attVal.equals(xtfGeom)) {
                        Type type = attr.getDomain();
                        if (isMultiSurfaceAttr(td, attr)) {
                            geomType = MULTI_SURFACE_TYPE;
                            break;
                        }
                    }
                }
                if (geomType == null) {
                    logger.addEvent(logger.logErrorMsg("Given attribute is not a valid geometry type"));
                    return null;
                }
            }
        }
        return geomType;
    }

    private boolean isMultiSurfaceAttr(TransferDescription td, AttributeDef attr) {
        Type typeo = attr.getDomain();
        if (typeo instanceof CompositionType) {
            CompositionType type = (CompositionType) attr.getDomain();
            if (type.getCardinality().getMaximum() == 1) {
                Table struct = type.getComponentType();
                if (METAATTR_MAPPING_MULTISURFACE.equals(struct.getMetaValue(METAATTR_MAPPING))) {
                    return true;
                }
            }
        }
        return false;
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
