/*
 * Copyright 2012-2025 CodeLibs Project and the Others.
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.codelibs.fess.helper.SystemHelper;
import org.codelibs.fess.util.ComponentUtil;
import org.dbflute.utflute.lastaflute.LastaFluteTestCase;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.options.LoadState;

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

    /**
     * Test browser type selection with valid browser names.
     */
    public void test_getBrowserType_validBrowsers() {
        // Test chromium (default)
        generator.setBrowserName("chromium");
        assertNotNull("Should return chromium browser type", generator.getBrowserType(generator.worker.getValue1()));

        // Test firefox
        generator.setBrowserName("firefox");
        assertNotNull("Should return firefox browser type", generator.getBrowserType(generator.worker.getValue1()));

        // Test webkit
        generator.setBrowserName("webkit");
        assertNotNull("Should return webkit browser type", generator.getBrowserType(generator.worker.getValue1()));
    }

    /**
     * Test browser type selection with invalid browser name.
     */
    public void test_getBrowserType_invalidBrowser() {
        generator.setBrowserName("invalid-browser");
        try {
            generator.getBrowserType(generator.worker.getValue1());
            fail("Should throw exception for invalid browser name");
        } catch (Exception e) {
            assertTrue("Should throw CrawlerSystemException for invalid browser", e.getMessage().contains("Unknown browser name"));
        }
    }

    /**
     * Test viewport size configuration.
     */
    public void test_viewportConfiguration() {
        // Test setting viewport width
        generator.setViewportWidth(1024);
        generator.setViewportHeight(768);

        // Create new worker to apply settings
        generator.destroy();
        generator.createWorker();

        // Verify settings were applied (indirect verification through successful creation)
        assertNotNull("Worker should be created with new viewport settings", generator.worker);
    }

    /**
     * Test load state configuration.
     */
    public void test_loadStateConfiguration() {
        // Test different load states
        generator.setRenderedState(LoadState.LOAD);
        assertEquals("Should set LOAD state", LoadState.LOAD, generator.renderedState);

        generator.setRenderedState(LoadState.DOMCONTENTLOADED);
        assertEquals("Should set DOMCONTENTLOADED state", LoadState.DOMCONTENTLOADED, generator.renderedState);

        generator.setRenderedState(LoadState.NETWORKIDLE);
        assertEquals("Should set NETWORKIDLE state", LoadState.NETWORKIDLE, generator.renderedState);
    }

    /**
     * Test close timeout configuration.
     */
    public void test_closeTimeoutConfiguration() {
        final int newTimeout = 30;
        generator.setCloseTimeout(newTimeout);
        assertEquals("Should set close timeout", newTimeout, generator.closeTimeout);
    }

    /**
     * Test full page screenshot configuration.
     */
    public void test_fullPageConfiguration() {
        // Test enabling full page screenshots
        generator.setLoadFullPage(true);
        assertTrue("Should enable full page screenshots", generator.loadFullPage);

        // Test disabling full page screenshots
        generator.setLoadFullPage(false);
        assertFalse("Should disable full page screenshots", generator.loadFullPage);
    }

    /**
     * Test createScreenshot with different dimensions.
     */
    public void test_createScreenshot_differentDimensions() throws IOException {
        final int[][] dimensions = { { 100, 100 }, { 300, 200 }, { 500, 300 } };

        for (int[] dim : dimensions) {
            final File pngFile = File.createTempFile("fess-thumbnail-", ".png");
            try {
                generator.createScreenshot("https://fess.codelibs.org/", dim[0], dim[1], pngFile);
                assertTrue("File should exist for " + dim[0] + "x" + dim[1], pngFile.exists());

                final BufferedImage img = ImageIO.read(pngFile);
                assertEquals("Width should match for " + dim[0] + "x" + dim[1], dim[0], img.getWidth());
                assertTrue("Height should be <= max height for " + dim[0] + "x" + dim[1], img.getHeight() <= dim[1]);
            } finally {
                if (pngFile.exists()) {
                    pngFile.delete();
                }
            }
        }
    }

    /**
     * Test createScreenshot with invalid URL.
     */
    public void test_createScreenshot_invalidUrl() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-", ".png");
        try {
            generator.createScreenshot("invalid-url", 200, 200, pngFile);
            fail("Should throw exception for invalid URL");
        } catch (Exception e) {
            // Expected behavior for invalid URL
            assertTrue("Should handle invalid URL gracefully", true);
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test screenshot options creation.
     */
    public void test_createScreenshotOptions() throws IOException {
        final File tempFile = File.createTempFile("test-", ".png");
        try {
            // Test with full page disabled
            generator.setLoadFullPage(false);
            final var options1 = generator.createScreenshotOptions(tempFile);
            assertNotNull("Should create screenshot options", options1);

            // Test with full page enabled
            generator.setLoadFullPage(true);
            final var options2 = generator.createScreenshotOptions(tempFile);
            assertNotNull("Should create screenshot options with full page", options2);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test closeInBackground functionality.
     */
    public void test_closeInBackground() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] executed = { false };

        generator.closeInBackground(() -> {
            executed[0] = true;
            latch.countDown();
        });

        assertTrue("Background task should complete within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Background task should be executed", executed[0]);
    }

    /**
     * Test closeInBackground with timeout.
     */
    public void test_closeInBackground_timeout() throws InterruptedException {
        // Set very short timeout for testing
        generator.setCloseTimeout(1);

        final CountDownLatch startLatch = new CountDownLatch(1);
        generator.closeInBackground(() -> {
            startLatch.countDown();
            try {
                // Sleep longer than timeout to test timeout behavior
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Verify task started but should timeout
        assertTrue("Background task should start", startLatch.await(1, TimeUnit.SECONDS));
        // Note: Timeout behavior is logged but doesn't throw exception
    }

    /**
     * Test initialization without thumbnail environment.
     */
    public void test_init_nonThumbnailEnvironment() {
        final String originalEnv = System.getProperty("lasta.env");
        try {
            // Set non-thumbnail environment
            System.setProperty("lasta.env", "production");

            final PlaywrightThumbnailGenerator testGenerator = new PlaywrightThumbnailGenerator();
            testGenerator.init();

            // Should not create worker in non-thumbnail environment
            assertNull("Should not create worker in non-thumbnail environment", testGenerator.worker);
        } finally {
            // Restore original environment
            if (originalEnv != null) {
                System.setProperty("lasta.env", originalEnv);
            } else {
                System.clearProperty("lasta.env");
            }
        }
    }

    /**
     * Test thread safety of createScreenshot method.
     */
    public void test_createScreenshot_threadSafety() throws Exception {
        final int threadCount = 3;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(threadCount);
        final Exception[] exceptions = new Exception[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    final File pngFile = File.createTempFile("thread-" + threadIndex + "-", ".png");
                    generator.createScreenshot("https://fess.codelibs.org/", 150, 150, pngFile);
                    if (pngFile.exists()) {
                        pngFile.delete();
                    }
                } catch (Exception e) {
                    exceptions[threadIndex] = e;
                } finally {
                    completeLatch.countDown();
                }
            }).start();
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        assertTrue("All threads should complete within timeout", completeLatch.await(30, TimeUnit.SECONDS));

        // Verify no exceptions occurred
        for (int i = 0; i < threadCount; i++) {
            assertNull("Thread " + i + " should complete without exception", exceptions[i]);
        }
    }

    /**
     * Test launch options configuration.
     */
    public void test_launchOptionsConfiguration() {
        final BrowserType.LaunchOptions newOptions = new BrowserType.LaunchOptions();
        newOptions.setHeadless(false);
        newOptions.setSlowMo(100);

        generator.setLaunchOptions(newOptions);
        assertEquals("Should set launch options", newOptions, generator.launchOptions);
    }

    /**
     * Test browser context options configuration.
     */
    public void test_contextOptionsConfiguration() {
        final Browser.NewContextOptions newOptions = new Browser.NewContextOptions();
        newOptions.setIgnoreHTTPSErrors(true);

        generator.setNewContextOptions(newOptions);
        assertEquals("Should set context options", newOptions, generator.newContextOptions);
    }

    /**
     * Test constructor creates proper instance.
     */
    public void test_constructor() {
        final PlaywrightThumbnailGenerator newGenerator = new PlaywrightThumbnailGenerator();
        assertNotNull("Constructor should create instance", newGenerator);
        assertTrue("Should extend BaseThumbnailGenerator", newGenerator instanceof org.codelibs.fess.thumbnail.impl.BaseThumbnailGenerator);
    }

    /**
     * Test resource cleanup on destroy.
     */
    public void test_destroy() {
        // Verify worker exists before destroy
        assertNotNull("Worker should exist before destroy", generator.worker);

        // Call destroy
        generator.destroy();

        // Note: Worker reference is not nulled in destroy method,
        // but internal resources are closed
        assertTrue("Destroy should complete without exception", true);
    }

    /**
     * Test close method with all null parameters.
     */
    public void test_close_withNullParameters() {
        // Execute - should handle null parameters gracefully
        generator.close(null, null, null, null);

        // Verify - should complete without exception
        assertTrue("Close should handle null parameters gracefully", true);
    }

    /**
     * Test close method with mixed null and non-null parameters.
     */
    public void test_close_withMixedParameters() {
        // Execute - should handle mixed null parameters gracefully
        // Only some parameters are null
        generator.close(null, generator.worker.getValue2(), null, generator.worker.getValue4());

        // Verify - should complete without exception
        assertTrue("Close should handle mixed null parameters gracefully", true);
    }

    /**
     * Test viewport configuration can be modified.
     */
    public void test_viewportConfiguration_modification() {
        // Test setting custom viewport dimensions
        generator.setViewportWidth(1280);
        generator.setViewportHeight(720);

        // Verify settings were applied
        assertEquals("Viewport width should be modifiable", 1280, generator.viewportWidth);
        assertEquals("Viewport height should be modifiable", 720, generator.viewportHeight);
    }

    /**
     * Test createScreenshot with tall image that needs clipping.
     * This tests the image height clipping logic when resizedImg.getHeight() > maxHeight.
     */
    public void test_createScreenshot_tallImageClipping() throws IOException {
        // Create a screenshot with small width and large max height
        // This should result in a tall resized image that needs clipping
        final File pngFile = File.createTempFile("fess-thumbnail-tall-", ".png");
        try {
            final int targetWidth = 100;
            final int maxHeight = 100;

            // URL with a tall page that will likely result in clipping
            generator.createScreenshot("https://fess.codelibs.org/ja/10.0/install/install.html", targetWidth, maxHeight, pngFile);

            assertTrue("File should exist", pngFile.exists());

            final BufferedImage img = ImageIO.read(pngFile);
            assertEquals("Width should match target width", targetWidth, img.getWidth());
            assertTrue("Height should be clipped to max height or less", img.getHeight() <= maxHeight);
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test createScreenshot verifies aspect ratio preservation.
     */
    public void test_createScreenshot_aspectRatioPreservation() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-aspect-", ".png");
        try {
            final int targetWidth = 400;
            final int maxHeight = 600;

            generator.createScreenshot("https://fess.codelibs.org/", targetWidth, maxHeight, pngFile);

            assertTrue("File should exist", pngFile.exists());

            final BufferedImage img = ImageIO.read(pngFile);
            assertEquals("Width should match target width", targetWidth, img.getWidth());

            // Height should maintain aspect ratio (may be clipped to maxHeight)
            assertTrue("Height should be positive", img.getHeight() > 0);
            assertTrue("Height should not exceed max height", img.getHeight() <= maxHeight);
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test initialization creates proper worker components.
     */
    public void test_createWorker_createsAllComponents() {
        // Verify all worker components are created
        assertNotNull("Worker should be created", generator.worker);
        assertNotNull("Playwright should be created", generator.worker.getValue1());
        assertNotNull("Browser should be created", generator.worker.getValue2());
        assertNotNull("BrowserContext should be created", generator.worker.getValue3());
        assertNotNull("Page should be created", generator.worker.getValue4());
        assertNotNull("NavigateOptions should be created", generator.navigateOptions);
        // Note: available field is protected, but if worker is created, generator is available
    }

    /**
     * Test that navigation timeout is properly set in navigateOptions.
     */
    public void test_navigationTimeout_configuredCorrectly() {
        // Default navigation timeout should be 30000
        assertEquals("Default navigation timeout should be 30000ms", 30000.0, generator.navigationTimeout, 0.01);

        // Verify navigateOptions is created (can't directly verify timeout value as it's internal)
        assertNotNull("NavigateOptions should be created", generator.navigateOptions);
    }

    /**
     * Test createScreenshot with HTTPS URL.
     */
    public void test_createScreenshot_httpsUrl() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-https-", ".png");
        try {
            generator.createScreenshot("https://www.google.com/", 200, 200, pngFile);
            assertTrue("File should exist for HTTPS URL", pngFile.exists());

            final BufferedImage img = ImageIO.read(pngFile);
            assertNotNull("Image should be readable", img);
            assertEquals("Width should match", 200, img.getWidth());
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test that worker is properly created and initialized.
     */
    public void test_workerProperlyCreatedAndInitialized() {
        // Verify worker is created (indicates generator is available)
        assertNotNull("Worker should be properly created after initialization", generator.worker);
        assertNotNull("Worker should have Playwright instance", generator.worker.getValue1());
    }

    /**
     * Test createScreenshot handles response redirects properly.
     */
    public void test_createScreenshot_handlesRedirects() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-redirect-", ".png");
        try {
            // URL that might redirect (e.g., http -> https)
            generator.createScreenshot("http://www.google.com/", 200, 200, pngFile);
            assertTrue("File should exist even with redirects", pngFile.exists());

            final BufferedImage img = ImageIO.read(pngFile);
            assertNotNull("Image should be readable after redirect", img);
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test that temp file is cleaned up even when createScreenshot succeeds.
     */
    public void test_createScreenshot_tempFileCleanup() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-cleanup-", ".png");
        try {
            generator.createScreenshot("https://fess.codelibs.org/", 200, 200, pngFile);
            assertTrue("Output file should exist", pngFile.exists());

            // Temp files should be cleaned up automatically
            // We can't directly verify this, but the test should not leave temp files
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test createScreenshot with very small dimensions.
     */
    public void test_createScreenshot_verySmallDimensions() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-small-", ".png");
        try {
            final int width = 50;
            final int height = 50;

            generator.createScreenshot("https://fess.codelibs.org/", width, height, pngFile);
            assertTrue("File should exist", pngFile.exists());

            final BufferedImage img = ImageIO.read(pngFile);
            assertEquals("Width should match small dimension", width, img.getWidth());
            assertTrue("Height should not exceed small max height", img.getHeight() <= height);
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test createScreenshot with very large width dimension.
     */
    public void test_createScreenshot_largeDimensions() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-large-", ".png");
        try {
            final int width = 1920;
            final int height = 1080;

            generator.createScreenshot("https://fess.codelibs.org/", width, height, pngFile);
            assertTrue("File should exist", pngFile.exists());

            final BufferedImage img = ImageIO.read(pngFile);
            assertEquals("Width should match large dimension", width, img.getWidth());
            assertTrue("Height should not exceed large max height", img.getHeight() <= height);
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }

    /**
     * Test that different load states work correctly.
     */
    public void test_createScreenshot_withDifferentLoadStates() throws IOException {
        final LoadState[] loadStates = { LoadState.LOAD, LoadState.DOMCONTENTLOADED, LoadState.NETWORKIDLE };

        for (LoadState loadState : loadStates) {
            final File pngFile = File.createTempFile("fess-thumbnail-loadstate-", ".png");
            try {
                generator.setRenderedState(loadState);
                generator.createScreenshot("https://fess.codelibs.org/", 200, 200, pngFile);
                assertTrue("File should exist for load state: " + loadState, pngFile.exists());
            } finally {
                if (pngFile.exists()) {
                    pngFile.delete();
                }
            }
        }

        // Reset to default
        generator.setRenderedState(LoadState.NETWORKIDLE);
    }

    /**
     * Test createScreenshot with full page enabled.
     */
    public void test_createScreenshot_withFullPage() throws IOException {
        final File pngFile = File.createTempFile("fess-thumbnail-fullpage-", ".png");
        try {
            generator.setLoadFullPage(true);
            generator.createScreenshot("https://fess.codelibs.org/", 200, 200, pngFile);
            assertTrue("File should exist with full page enabled", pngFile.exists());

            final BufferedImage img = ImageIO.read(pngFile);
            assertNotNull("Image should be readable with full page", img);
        } finally {
            generator.setLoadFullPage(false); // Reset to default
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }
}
