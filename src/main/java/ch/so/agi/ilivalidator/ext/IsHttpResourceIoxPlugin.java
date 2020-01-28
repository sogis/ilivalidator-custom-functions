package ch.so.agi.ilivalidator.ext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

public class IsHttpResourceIoxPlugin implements InterlisFunction {
    private LogEventFactory logger = null;

    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        if (actualArguments[0].skipEvaluation()) {
            return actualArguments[0];
        }
        if (actualArguments[0].isUndefined()) {
            return Value.createSkipEvaluation();
        }

        String urlValue = actualArguments[0].getValue();
        String prefixValue = actualArguments[1].getValue();
        
        try {
            URL siteURL = new URL(prefixValue + urlValue);
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            // HEAD does not work in a lot of environments and returns a 405 status code. 
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.connect();
    
            int responseCode = connection.getResponseCode();

            if (200 <= responseCode && responseCode <= 399) {
                return new Value(true); 
            } else {
                return new Value(false);
            }
            
        } catch (IOException e) {
            // When there is no server for a feedback, we end
            // up here.
            return new Value(false);
        } 
    }

    @Override
    public String getQualifiedIliName() {
        return "SO_FunctionsExt.isHttpResource";
    }

    @Override
    public void init(TransferDescription td, Settings settings, 
            IoxValidationConfig validationConfig, ObjectPool objectPool, 
            LogEventFactory logEventFactory) {
        logger=logEventFactory;
    }
}
