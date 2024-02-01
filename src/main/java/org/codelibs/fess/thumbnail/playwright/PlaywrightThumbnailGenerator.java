/*
 * Copyright 2012-2023 CodeLibs Project and the Others.
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codelibs.core.exception.IORuntimeException;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.core.misc.Tuple4;
import org.codelibs.fess.Constants;
import org.codelibs.fess.crawler.exception.CrawlerSystemException;
import org.codelibs.fess.mylasta.direction.FessConfig;
import org.codelibs.fess.thumbnail.impl.BaseThumbnailGenerator;
import org.codelibs.fess.util.ComponentUtil;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.ScreenshotOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;

import jakarta.annotation.PostConstruct;

public class PlaywrightThumbnailGenerator extends BaseThumbnailGenerator {

    private static final Logger logger = LogManager.getLogger(PlaywrightThumbnailGenerator.class);

    protected NewContextOptions newContextOptions = new NewContextOptions();

    protected LoadState renderedState = LoadState.NETWORKIDLE;

    protected Map<String, String> options = new HashMap<>();

    protected LaunchOptions launchOptions;

    protected String browserName = "chromium";

    protected int closeTimeout = 15; // 15s

    protected int viewportWidth = 960;

    protected int viewportHeight = 960;

    protected boolean loadFullPage = false;

    protected Tuple4<Playwright, Browser, BrowserContext, Page> worker;

    protected Page.NavigateOptions navigateOptions;

    protected double navigationTimeout = 30000;

    @PostConstruct
    public void init() {
        final String lastaEnv = System.getProperty("lasta.env");
        if (!Constants.EXECUTE_TYPE_THUMBNAIL.equals(lastaEnv)) {
            if (logger.isDebugEnabled()) {
                logger.debug("lasta.env is {}. PlaywrightThumbnailGenerator is disabled.", lastaEnv);
            }
            return;
        }

        createWorker();
    }

    protected void createWorker() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initiaizing Playwright...");
        }

        updateProperties();

        Playwright playwright = null;
        Browser browser = null;
        BrowserContext browserContext = null;
        Page page = null;
        try {
            playwright = Playwright.create(new Playwright.CreateOptions().setEnv(options));
            browser = getBrowserType(playwright).launch(launchOptions);
            browserContext = browser.newContext(newContextOptions);
            page = browserContext.newPage();
            page.setViewportSize(viewportWidth, viewportHeight);
            worker = new Tuple4<>(playwright, browser, browserContext, page);
            navigateOptions = new Page.NavigateOptions().setTimeout(navigationTimeout);
            available = true;
        } catch (final Exception e) {
            available = false;
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to create Playwright instance.", e);
            }
            close(playwright, browser, browserContext, page);
            throw new CrawlerSystemException("Failed to create PlaywrightThumbnailGenerator.", e);
        }
    }

    protected void updateProperties() {
        final FessConfig fessConfig = ComponentUtil.getFessConfig();
        final String viewportWidthStr = fessConfig.getSystemProperty("thumbnail.playwright.viewport.width");
        if (viewportWidthStr != null) {
            viewportWidth = Integer.valueOf(viewportWidthStr);
        }
        final String viewportHeightStr = fessConfig.getSystemProperty("thumbnail.playwright.viewport.height");
        if (viewportHeightStr != null) {
            viewportHeight = Integer.valueOf(viewportHeightStr);
        }
        final String navigationTimeoutStr = fessConfig.getSystemProperty("thumbnail.playwright.navigation.timeout");
        if (navigationTimeoutStr != null) {
            navigationTimeout = Double.valueOf(navigationTimeoutStr);
        }
    }

    protected BrowserType getBrowserType(final Playwright playwright) {
        if (logger.isDebugEnabled()) {
            logger.debug("Create {}...", browserName);
        }
        return switch (browserName) {
        case "firefox":
            yield playwright.firefox();
        case "webkit":
            yield playwright.webkit();
        case "chromium":
            yield playwright.chromium();
        default:
            throw new CrawlerSystemException("Unknown browser name: " + browserName);
        };
    }

    @Override
    public boolean generate(final String thumbnailId, final File outputFile) {
        if (worker == null) {
            logger.warn("[{}] Playwright is not availavle.", thumbnailId);
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Generate Thumbnail: {}", thumbnailId);
        }

        if (outputFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("The thumbnail file exists: {}", outputFile.getAbsolutePath());
            }
            return true;
        }

        final File parentFile = outputFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!parentFile.isDirectory()) {
            logger.warn("Not found: {}", parentFile.getAbsolutePath());
            return false;
        }

        return process(thumbnailId, (configId, url) -> {
            boolean created = false;
            final FessConfig fessConfig = ComponentUtil.getFessConfig();
            final int targetWidth = fessConfig.getThumbnailHtmlImageThumbnailWidthAsInteger();
            final int maxHeight = fessConfig.getThumbnailHtmlImageThumbnailHeightAsInteger();
            try {
                createScreenshot(url, targetWidth, maxHeight, outputFile);
                created = true;
            } catch (final Throwable t) {
                logger.warn("Failed to create thumbnail: {} -> {} ({}:{})", thumbnailId, url, t.getClass().getCanonicalName(),
                        t.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug("Details for failed thumbnail creation.", t);
                }
            } finally {
                if (!created) {
                    updateThumbnailField(thumbnailId, StringUtil.EMPTY);
                    if (outputFile.exists() && !outputFile.delete()) {
                        logger.warn("Failed to delete {}", outputFile.getAbsolutePath());
                    }
                }
            }
            return outputFile.exists();
        });
    }

    protected synchronized void createScreenshot(final String url, final int width, final int height, final File outputFile) {
        final Page page = worker.getValue4();
        File tempPngFile = null;
        try {
            final Response response = page.navigate(url, navigateOptions);
            page.waitForLoadState(renderedState);
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded {} -> {}", url, response.url());
            }

            tempPngFile = ComponentUtil.getSystemHelper().createTempFile("fess-thumbnail-", ".png");
            page.screenshot(createScreenshotOptions(tempPngFile));

            if (logger.isDebugEnabled()) {
                logger.debug("Saved screenshot: {}", tempPngFile.getAbsolutePath());
            }

            // Load the original screenshot
            final BufferedImage img = ImageIO.read(tempPngFile);

            final int imageWidth = img.getWidth();
            final int imageHeight = img.getHeight();
            if (logger.isDebugEnabled()) {
                logger.debug("Screenshot is {}x{}", imageWidth, imageHeight);
            }

            // Calculate the target height to maintain the aspect ratio
            final int targetHeight = (int) ((double) imageHeight / imageWidth * width);

            // Create a new image with the target dimensions
            BufferedImage resizedImg = new BufferedImage(width, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = null;
            try {
                g2d = resizedImg.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(img, 0, 0, width, targetHeight, null);
            } finally {
                if (g2d != null) {
                    g2d.dispose();
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Resized screenshot is {}x{}", resizedImg.getWidth(), resizedImg.getHeight());
            }

            // If the resized image is taller than the maximum height, clip it
            if (resizedImg.getHeight() > height) {
                resizedImg = resizedImg.getSubimage(0, 0, width, height);
            }

            // Save the resized/clipped image
            ImageIO.write(resizedImg, "png", outputFile);
        } catch (final IOException e) {
            throw new IORuntimeException(e);
        } finally {
            if (tempPngFile != null) {
                tempPngFile.delete();
            }
        }
    }

    protected ScreenshotOptions createScreenshotOptions(final File tempPngFile) {
        return new Page.ScreenshotOptions().setFullPage(loadFullPage).setPath(tempPngFile.toPath());
    }

    @Override
    public void destroy() {
        if (worker != null) {
            close(worker.getValue1(), worker.getValue2(), worker.getValue3(), worker.getValue4());
        }
    }

    protected void close(final Playwright playwright, final Browser browser, final BrowserContext context, final Page page) {
        closeInBackground(() -> {
            if (page != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Closing Page...");
                }
                page.close();
            }
        });
        closeInBackground(() -> {
            if (context != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Closing BrowserContext...");
                }
                context.close();
            }
        });
        closeInBackground(() -> {
            if (browser != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Closing Browser...");
                }
                browser.close();
            }
        });
        closeInBackground(() -> {
            if (playwright != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Closing Playwright...");
                }
                playwright.close();
            }
        });
    }

    protected void closeInBackground(final Runnable closer) {
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            final Thread thread = new Thread(() -> {
                try {
                    closer.run();
                } catch (final Exception e) {
                    logger.warn("Failed to close the playwright instance.", e);
                }
                latch.countDown();
            }, "Playwright-Closer");
            thread.setDaemon(true);
            thread.start();
            if (!latch.await(closeTimeout, TimeUnit.SECONDS)) {
                logger.warn("The close process is timed out.");
            }
        } catch (final InterruptedException e) {
            logger.warn("Interrupted to wait a process.", e);
        } catch (final Exception e) {
            logger.warn("Failed to close the playwright instance.", e);
        }
    }

    public void setLaunchOptions(final LaunchOptions launchOptions) {
        this.launchOptions = launchOptions;
    }

    public void setBrowserName(final String browserName) {
        this.browserName = browserName;
    }

    public void setRenderedState(final LoadState loadState) {
        this.renderedState = loadState;
    }

    public void setCloseTimeout(final int closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    public void setNewContextOptions(final NewContextOptions newContextOptions) {
        this.newContextOptions = newContextOptions;
    }

    public void setViewportWidth(int viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public void setViewportHeight(int viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public void setLoadFullPage(boolean loadFullPage) {
        this.loadFullPage = loadFullPage;
    }
}
