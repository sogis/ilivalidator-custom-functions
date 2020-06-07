package ch.so.agi.ilivalidator.ext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.metamodel.AssociationPath;
import ch.interlis.ili2c.metamodel.AttributeRef;
import ch.interlis.ili2c.metamodel.CompositionType;
import ch.interlis.ili2c.metamodel.EnumerationType;
import ch.interlis.ili2c.metamodel.LocalAttribute;
import ch.interlis.ili2c.metamodel.ObjectPath;
import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.PathElAbstractClassRole;
import ch.interlis.ili2c.metamodel.PathElAssocRole;
import ch.interlis.ili2c.metamodel.RoleDef;
import ch.interlis.ili2c.metamodel.StructAttributeRef;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Type;
import ch.interlis.ili2c.metamodel.TypeAlias;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.ili2c.parser.Ili23Parser;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

public class IsHttpResourceFromOerebMultilingualUriIoxPlugin implements InterlisFunction {
    private LogEventFactory logger = null;

    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        if (actualArguments[0].skipEvaluation()) {
            return actualArguments[0];
        }
        if (actualArguments[0].isUndefined()) {
            return Value.createSkipEvaluation();
        }
                
        Iterator<IomObject> it = actualArguments[0].getComplexObjects().iterator();
        // There is only one structure.
        IomObject multilingualUriStruct = it.next();

        // TODO: So stimmt das noch nicht, da die Schleife gar nicht durchlaufen wird,
        // sondern beim ersten Objekt gleich returned wird.
        for (int i=0; i<multilingualUriStruct.getattrvaluecount("LocalisedText"); i++) {            
            IomObject localisedUri = multilingualUriStruct.getattrobj("LocalisedText", i);
            try {
                
                String encodedUrl = localisedUri.getattrvalue("Text").trim();

                // TODO: Nicht wirklich sicher, ob das ganz korrekt ist.
                String decodedUrl = java.net.URLDecoder.decode(encodedUrl, "UTF-8").replace("&amp;", "&"); 
                System.out.println("decodedUrl: " + decodedUrl);
                System.err.println("decodedUrl: " + decodedUrl);
                
                URL siteURL = new URL(decodedUrl);
                HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
                // HEAD does not work in a lot of environments and returns a 405 status code.
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.connect();

                int responseCode = connection.getResponseCode();

                if (200 <= responseCode && responseCode <= 399) {
                    return new Value(true);
                } else {
                    logger.addEvent(logger.logErrorMsg("Document ("+decodedUrl+") not found. TID: " + mainObj.getobjectoid(), mainObj.getobjectoid()));                    
                    return new Value(false);
                }
                
            } catch (IOException e) {
                // When there is no server for a feedback, we end
                // up here.
                logger.addEvent(logger.logErrorMsg("Document not found. TID: " + mainObj.getobjectoid(), mainObj.getobjectoid()));                    
                return new Value(false);
            }  
        }
        return new Value(false);
    }
    
    @Override
    public String getQualifiedIliName() {
        return "SO_FunctionsExt.isHttpResourceFromOerebMultilingualUri";
    }

    @Override
    public void init(TransferDescription td, Settings settings, 
            IoxValidationConfig validationConfig, ObjectPool objectPool, 
            LogEventFactory logEventFactory) {
        logger=logEventFactory;
    }    
}
