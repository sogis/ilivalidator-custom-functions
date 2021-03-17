package ch.so.agi.ilivalidator.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import ch.ehi.ili2db.mapping.MultiSurfaceMappings;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.CompositionType;
import ch.interlis.ili2c.metamodel.CoordType;
import ch.interlis.ili2c.metamodel.LocalAttribute;
import ch.interlis.ili2c.metamodel.SurfaceType;
import ch.interlis.ili2c.metamodel.Table;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Type;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.jts.Iox2jts;
import ch.interlis.iox_j.jts.Iox2jtsException;
import ch.interlis.iox_j.wkb.Iox2wkb;
import ch.interlis.iox_j.wkb.Iox2wkbException;

public class GeometryUtils {
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

    public static Coordinate[] removeDuplicatePoints(Coordinate[] coord)
    {
      List uniqueCoords = new ArrayList();
      Coordinate lastPt = null;
      for (int i = 0; i < coord.length; i++) {
        if (lastPt == null || ! lastPt.equals(coord[i])) {
          lastPt = coord[i];
          uniqueCoords.add(new Coordinate(lastPt));
        }
      }
      return (Coordinate[]) uniqueCoords.toArray(new Coordinate[0]);
    }
    
    public static String getGeometryType(IomObject xtfGeom, IomObject mainObj, HashMap<String, Viewable> tag2class, TransferDescription td) {
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
                    return null;
                }
            }
        }
        return geomType;
    }
    
    public static Geometry geometry2JTS(IomObject object, LocalAttribute attr, String geometryType, double strokeP, TransferDescription td) throws Iox2jtsException {
        Geometry geometry = null;
        try {
            switch (geometryType) {
                case POLYLINE_TYPE:
                    geometry = new GeometryFactory().createLineString(Iox2jts.polyline2JTS(object, false, 0).toCoordinateArray());
                    break;
                case SURFACE_OR_AREA_TYPE:
                    geometry = Iox2jts.surface2JTS(object, strokeP);
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
                            throw new IllegalArgumentException("Given attribute is not a valid multisurface type");
                        }
                    }
                    break;
            }
        } catch (Iox2jtsException e) {
            throw new Iox2jtsException(e.getMessage());
        }
        return geometry;
    }
    
    private static boolean isMultiSurfaceAttr(TransferDescription td, AttributeDef attr) {
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
    
    private static AttributeDef getMultiSurfaceAttrDef(Type type, ch.ehi.ili2db.mapping.MultiSurfaceMapping attrMapping) {
        Table multiSurfaceType = ((CompositionType) type).getComponentType();
        Table surfaceStructureType = ((CompositionType) ((AttributeDef) multiSurfaceType.getElement(AttributeDef.class, attrMapping.getBagOfSurfacesAttrName())).getDomain()).getComponentType();
        AttributeDef surfaceAttr = (AttributeDef) surfaceStructureType.getElement(AttributeDef.class, attrMapping.getSurfaceAttrName());
        return surfaceAttr;
    }

}
