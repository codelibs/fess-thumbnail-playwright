/*
 * Copyright 2012-2024 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.thumbnail.playwright;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.codelibs.fess.helper.SystemHelper;
import org.codelibs.fess.util.ComponentUtil;
import org.dbflute.utflute.lastaflute.LastaFluteTestCase;

import com.microsoft.playwright.BrowserType;

public class PlaywrightThumbnailGeneratorTest extends LastaFluteTestCase {

    private PlaywrightThumbnailGenerator generator;

    @Override
    protected String prepareConfigFile() {
        return "test_app.xml";
    }

    @Override
    protected boolean isSuppressTestCaseTransaction() {
        return true;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        generator = new PlaywrightThumbnailGenerator() {
            @Override
            protected void updateProperties() {
            }
        };
        final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(true);
        generator.addCondition("mimetype", "text/html");
        generator.setLaunchOptions(launchOptions);
        generator.createWorker();
        ComponentUtil.register(new SystemHelper(), "systemHelper");
    }

    @Override
    public void tearDown() throws Exception {
        generator.destroy();
        ComponentUtil.setFessConfig(null);
        super.tearDown();
    }

    public void test_createScreenshot() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-", ".png");
        generator.createScreenshot("https://fess.codelibs.org/ja/10.0/install/install.html", 200, 200, pngFile);
        assertTrue(pngFile.getAbsolutePath() + " exists?", pngFile.exists());
        final BufferedImage img = ImageIO.read(pngFile);
        assertEquals(200, img.getWidth());
        assertEquals(200, img.getHeight());

    }
}
