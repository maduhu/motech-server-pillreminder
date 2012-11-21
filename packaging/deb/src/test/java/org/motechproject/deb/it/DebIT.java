package org.motechproject.deb.it;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.motechproject.testing.utils.BasePkgTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static junit.framework.Assert.assertEquals;

public class DebIT extends BasePkgTest {

    @Test
    public void testInstall() throws IOException, InterruptedException {
        int retVal = runScript("test-install.sh");
        assertEquals("Non-zero exit code returned", 0, retVal);
    }
}