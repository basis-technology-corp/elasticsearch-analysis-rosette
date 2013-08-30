/******************************************************************************
 ** Copyright (c) 2013 Basis Technology Corporation 
 ** Permission is hereby granted, free of charge, to any person obtaining a 
 ** copy of this software and associated documentation files (the "Software"), 
 ** to deal in the Software without restriction, including without limitation 
 ** the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 ** and/or sell copies of the Software, and to permit persons to whom the 
 ** Software is furnished to do so, subject to the following conditions: 
 **
 ** The above copyright notice and this permission notice shall be included in 
 ** all copies or substantial portions of the Software.
 ** 
 ** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 ** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 ** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 ** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 ** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 ** FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 ** DEALINGS IN THE SOFTWARE.
 **
 ** Except as contained in this notice, the name(s) of the above copyright 
 ** holders shall not  be used in advertising or otherwise to promote the sale, 
 ** use or other dealings in this Software without prior written authorization.
 ******************************************************************************/

package com.basistech.elasticsearch.plugin.analysis.rosette;

import com.basistech.elasticsearch.index.analysis.rosette.RosetteAnalysisBinderProcessor;
import com.basistech.util.Pathnames;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.loader.YamlSettingsLoader;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

import java.io.*;
import java.util.Map;

/**
 *
 */
public class AnalysisRosettePlugin extends AbstractPlugin {
    //bt.root is the root directory of the Basis RLP tree.
    // Note: "BT" = Basis Technology
    private static final String BT_ROOT_ENV_VAR = "BT_ROOT";
    private static final String BT_ROOT_PARAM = "bt.root";
    private static final String PLUGIN_NAME = "analysis-rosette";
    private ESLogger logger = null;

    private boolean setBTRoot(String path) {
        boolean rc = false;
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Pathnames.setBTRootDirectory(path);
                logger.info(String.format("BT root directory set to '%s'", path));
                rc = true;
            } else {
                logger.info(String.format("BT root directory '%s' does not exist.", path));
            }
        }
        return rc;
    }

    private boolean setBTRootFromSettings(Settings settings) {
        logger.info(String.format("Attempting to set BT root directory from the '%s' setting.", BT_ROOT_PARAM));
        String btRootDir = settings.get(BT_ROOT_PARAM);
        return setBTRoot(btRootDir);
    }

    private boolean setBTRootFromEnvironment() {
        logger.info(String.format("Attempting to set BT root directory from the '%s' environment variable.", BT_ROOT_ENV_VAR));
        String btRootDir = System.getenv(BT_ROOT_ENV_VAR);
        return setBTRoot(btRootDir);
    }

    private boolean setBTRootFromConfig() {
        boolean rc = false;

        // NB: This file name construction only works if this code is run from our plugin's
        // jar file in the standard elascticsearch directory tree.  If you are running this
        // in some other way, either pass -Dbt.root=<BT root path> to the VM or set the
        // BT_ROOT environment variable.
        Environment environment = new Environment();
        File myJarPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        File myConfigFile = new File(String.format("%s/../../%s/bt/%s-config.yml", myJarPath.getParent(), environment.configFile().getName(), PLUGIN_NAME));

        logger.info(String.format("Attempting to set BT root directory from the configuration file '%s'.", myConfigFile.getAbsolutePath()));
        try {
            YamlSettingsLoader sl = new YamlSettingsLoader();
            Map<String, String> settings = sl.load(readFile(myConfigFile));
            String btRoot = settings.get(BT_ROOT_PARAM);
            rc = setBTRoot(btRoot);
        } catch (IOException e) {
            logger.error(String.format("Error reading config file %s\n Caused by: %s", myConfigFile.getAbsolutePath(), e.getStackTrace()));
        }
        return rc;
    }

    private String readFile(File file) {
        final String CFG_FILE_ENCODING = "UTF8";
        String line;
        String ls = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        // Note: Careful error handling because we are called from a construtor.
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, CFG_FILE_ENCODING);
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
        } catch (FileNotFoundException fnfe) {
            logger.info(String.format("File '%s' does not exist.", file.getAbsolutePath()));
        } catch (UnsupportedEncodingException usee) {
            logger.error(String.format("'%s', expected in the configuration file (%s), is not a supported encoding. Really??", CFG_FILE_ENCODING, file.getAbsolutePath()));
        } catch (IOException ioe) {
            logger.warn(String.format("IOException reading '%s': %s", file.getAbsolutePath(), ioe.getMessage()));
        }

        try {
            if (fis != null)
                fis.close();
        } catch (IOException e) { logger.info(e.getMessage()); }
        try {
            if (isr != null)
                isr.close();
        } catch (IOException e) { logger.info(e.getMessage()); }
        try {
            if (br != null)
                br.close();
        } catch (IOException e) { logger.info(e.getMessage()); }

        return stringBuilder.toString();
    }

    public AnalysisRosettePlugin(Settings settings) {
        // Set the BT root directory so that the RLP JNI layer knows where to find the RLP binaries.

        // We'll try to take the value from settings first and from the environment second.
        // This allows a user to override what we provide in our own configuration file.
        // If neither a setting, environment variable, nor a config file yields a root, the underlying
        // RLP JNI library will attempt to get it from the Java system property "bt.root".
        logger = Loggers.getLogger(PLUGIN_NAME);

        if (!setBTRootFromSettings(settings)) {
            if (!setBTRootFromEnvironment()) {
                if (!setBTRootFromConfig()) {
                    logger.error("Could not find name of BT root directory.");
                }
            }
        }
    }

    @Override
    public String name() {
        return PLUGIN_NAME;
    }

    @Override
    public String description() {
        String PLUGIN_DESCRIPTION = "Rosette (Basis Technology) analysis support";
        return PLUGIN_DESCRIPTION;
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new RosetteAnalysisBinderProcessor());
    }
}
