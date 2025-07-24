# Fess Thumbnail Playwright Plugin

[![Java CI with Maven](https://github.com/codelibs/fess-thumbnail-playwright/actions/workflows/maven.yml/badge.svg)](https://github.com/codelibs/fess-thumbnail-playwright/actions/workflows/maven.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.codelibs.fess/fess-thumbnail-playwright/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.codelibs.fess/fess-thumbnail-playwright)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A powerful thumbnail generation plugin for [Fess](https://fess.codelibs.org/) that uses Microsoft Playwright to capture high-quality screenshots of web pages for search result thumbnails.

## Features

- **High-Quality Screenshots**: Generates accurate web page thumbnails using real browser rendering
- **Multi-Browser Support**: Supports Chromium, Firefox, and WebKit engines
- **Intelligent Content Filtering**: Only processes HTML content to optimize performance
- **Configurable Viewport**: Customizable browser viewport dimensions for consistent thumbnail sizing
- **Thread-Safe Operations**: Synchronized screenshot generation for reliable concurrent processing
- **Flexible Image Processing**: Automatic resizing and cropping to fit specified dimensions while maintaining aspect ratio
- **Resource Management**: Efficient browser resource management with timeout protection

## Requirements

- **Java**: 21 or higher
- **Fess**: 15.0 or higher
- **Maven**: 3.6 or higher (for building from source)

## Installation

### Method 1: Using Maven Central (Recommended)

1. Download the latest JAR from [Maven Central](https://repo1.maven.org/maven2/org/codelibs/fess/fess-thumbnail-playwright/)

2. Place the JAR file in your Fess plugins directory:
   ```bash
   cp fess-thumbnail-playwright-*.jar $FESS_HOME/app/WEB-INF/lib/
   ```

3. Restart Fess

### Method 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/codelibs/fess-thumbnail-playwright.git
cd fess-thumbnail-playwright

# Build the project
mvn clean package

# Copy the generated JAR to Fess
cp target/fess-thumbnail-playwright-*.jar $FESS_HOME/app/WEB-INF/lib/
```

## Configuration

The plugin supports the following system properties for customization:

| Property | Description | Default Value |
|----------|-------------|---------------|
| `thumbnail.playwright.viewport.width` | Browser viewport width in pixels | `960` |
| `thumbnail.playwright.viewport.height` | Browser viewport height in pixels | `960` |
| `thumbnail.playwright.navigation.timeout` | Page load timeout in milliseconds | `30000` |

### Example Configuration

Add these properties to your Fess system configuration:

```properties
# Set custom viewport dimensions
thumbnail.playwright.viewport.width=1280
thumbnail.playwright.viewport.height=720

# Set navigation timeout to 45 seconds
thumbnail.playwright.navigation.timeout=45000
```

## Usage

Once installed, the plugin automatically integrates with Fess's thumbnail generation system. No additional configuration is required for basic usage.

### Browser Selection

The plugin supports three browser engines:

- **Chromium** (default): Best compatibility and performance
- **Firefox**: Alternative rendering engine
- **WebKit**: Safari-based rendering

### Thumbnail Generation Process

1. **Content Filtering**: Only HTML content (`text/html` MIME type) is processed
2. **Browser Launch**: Creates a headless browser instance with configured settings
3. **Page Navigation**: Loads the target URL with specified timeout
4. **Screenshot Capture**: Takes a full-page or viewport screenshot
5. **Image Processing**: Resizes and crops the image to fit thumbnail dimensions
6. **Resource Cleanup**: Safely closes browser resources with timeout protection

## Architecture

### Core Components

#### `PlaywrightThumbnailGenerator`
- **Purpose**: Main thumbnail generation engine
- **Features**: Browser management, screenshot capture, image processing
- **Thread Safety**: Synchronized operations for concurrent access

#### `CustomFessXpathTransformer`
- **Purpose**: Content filtering for thumbnail generation
- **Optimization**: Skips non-HTML content to improve performance

### Key Features

- **Singleton Browser Management**: Efficient resource utilization
- **Configurable Load States**: Wait for different page load conditions
- **Background Resource Cleanup**: Non-blocking browser resource disposal
- **Comprehensive Error Handling**: Robust error recovery and logging

## Testing

The project includes comprehensive test coverage:

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report
```

### Test Coverage

- **CustomFessXpathTransformer**: 8 test cases covering MIME type filtering and edge cases
- **PlaywrightThumbnailGenerator**: 18 test cases covering browser management, screenshot generation, and configuration

## Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add some amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/fess-thumbnail-playwright.git
cd fess-thumbnail-playwright

# Install dependencies (required for first-time setup)
git clone https://github.com/codelibs/fess-parent.git
cd fess-parent
mvn install -Dgpg.skip=true
cd ../fess-thumbnail-playwright

# Build and test
mvn clean compile
mvn test
```

### Code Style

The project uses automated code formatting. Format your code before committing:

```bash
mvn formatter:format
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Issues and Support

- **Bug Reports**: Please use the [GitHub Issues](https://github.com/codelibs/fess-thumbnail-playwright/issues) page
- **Feature Requests**: Submit them as GitHub Issues with the "enhancement" label
- **Questions**: Use [Discussions](https://discuss.codelibs.org/c/fessen/) for general questions

## Related Projects

- [Fess](https://github.com/codelibs/fess) - Full-text search server
- [Microsoft Playwright](https://github.com/microsoft/playwright-java) - Browser automation library

