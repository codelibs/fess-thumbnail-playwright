# Fess Thumbnail Example

This is an example project for creating custom thumbnail generators for [Fess](https://fess.codelibs.org/).

## Overview

This project provides a template/base implementation for creating custom thumbnail generators. It demonstrates the basic structure and API for implementing thumbnail generation functionality that can be integrated into Fess.

## Features

- **Template Implementation**: A working example of `BaseThumbnailGenerator` extension
- **Clean Architecture**: Minimal dependencies focusing on core Fess integration
- **Easy Customization**: Well-documented code with clear extension points
- **Production Ready**: Includes proper configuration, testing, and packaging setup

## Requirements

- **Java**: 21 or higher
- **Fess**: 15.0 or higher
- **Maven**: 3.6 or higher (for building from source)

## Project Structure

```
fess-thumbnail-example/
├── pom.xml                          # Maven configuration
├── README.md                        # This file
└── src/
    ├── main/
    │   ├── java/
    │   │   └── org/
    │   │       └── codelibs/
    │   │           └── fess/
    │   │               ├── thumbnail/
    │   │               │   └── example/
    │   │               │       └── ExampleThumbnailGenerator.java
    │   │               └── crawler/
    │   │                   └── transformer/
    │   │                       └── CustomFessXpathTransformer.java
    │   └── resources/
    │       ├── fess_thumbnail+htmlThumbnailGenerator.xml
    │       └── crawler/
    │           └── transformer+fessXpathTransformer.xml
    └── test/
        └── java/
            └── org/
                └── codelibs/
                    └── fess/
                        ├── thumbnail/
                        │   └── example/
                        │       └── ExampleThumbnailGeneratorTest.java
                        └── crawler/
                            └── transformer/
                                └── CustomFessXpathTransformerTest.java
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/codelibs/fess-thumbnail-example.git
cd fess-thumbnail-example

# Build the project
mvn clean package

# The JAR file will be created in target/fess-thumbnail-example-*.jar
```

## Creating Your Own Thumbnail Generator

### 1. Use This Project as a Template

Fork or copy this project as a starting point for your custom thumbnail generator.

### 2. Implement Thumbnail Generation Logic

Edit `src/main/java/org/codelibs/fess/thumbnail/example/ExampleThumbnailGenerator.java`:

```java
protected boolean createThumbnail(final String url, final int width,
                                  final int height, final File outputFile) {
    // Add your thumbnail generation logic here
    // Examples:
    // - Use Playwright/Selenium for browser screenshots
    // - Use ImageMagick for image processing
    // - Use wkhtmltoimage for HTML to image conversion
    // - Implement custom rendering

    return true; // Return true if successful
}
```

### 3. Add Required Dependencies

Update `pom.xml` to include any libraries you need:

```xml
<dependencies>
    <!-- Example: Add Playwright for browser automation -->
    <dependency>
        <groupId>com.microsoft.playwright</groupId>
        <artifactId>playwright</artifactId>
        <version>1.42.0</version>
    </dependency>

    <!-- Add other dependencies as needed -->
</dependencies>
```

### 4. Configure the Generator

Update `src/main/resources/fess_thumbnail+htmlThumbnailGenerator.xml` to add custom properties:

```xml
<component name="htmlThumbnailGenerator"
    class="org.codelibs.fess.thumbnail.example.ExampleThumbnailGenerator">
    <property name="name">"htmlThumbnailGenerator"</property>

    <!-- Add your custom properties -->
    <property name="viewportWidth">1280</property>
    <property name="viewportHeight">720</property>

    <postConstruct name="addCondition">
        <arg>"mimetype"</arg>
        <arg>"text/html"</arg>
    </postConstruct>
    <postConstruct name="register"></postConstruct>
</component>
```

### 5. Build and Deploy

```bash
# Build your custom thumbnail generator
mvn clean package

# Copy to Fess plugins directory
cp target/fess-thumbnail-example-*.jar $FESS_HOME/app/WEB-INF/plugin/

# Restart Fess
```

## Key Components

### ExampleThumbnailGenerator

The main thumbnail generator class that extends `BaseThumbnailGenerator`.

**Key Methods:**
- `init()`: Initialization logic, called after dependency injection
- `generate()`: Main entry point for thumbnail generation
- `createThumbnail()`: Actual thumbnail creation logic (implement this)
- `destroy()`: Cleanup resources

### CustomFessXpathTransformer

Custom transformer for content filtering before thumbnail generation.

**Features:**
- Filters HTML content by MIME type
- Optimizes performance by skipping non-HTML content
- Customizable for specific content types

## Configuration

The thumbnail generator can be configured through Fess system properties:

```properties
# Example configuration
thumbnail.example.property=value
```

Access configuration in your code:

```java
final FessConfig fessConfig = ComponentUtil.getFessConfig();
final String value = fessConfig.getSystemProperty("thumbnail.example.property");
```

## Testing

Run tests with Maven:

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ExampleThumbnailGeneratorTest
```

## Examples of Thumbnail Generators

This template can be used to create various types of thumbnail generators:

1. **Browser-based Screenshot Generator**
   - Use Playwright or Selenium
   - Capture real browser rendering
   - Handle JavaScript-heavy pages

2. **Image Processing Generator**
   - Use ImageMagick or similar libraries
   - Process and resize existing images
   - Apply filters and effects

3. **HTML to Image Generator**
   - Use wkhtmltoimage or similar tools
   - Convert HTML documents to images
   - Lightweight alternative to browser automation

4. **PDF Thumbnail Generator**
   - Use Apache PDFBox or similar
   - Generate thumbnails from PDF documents
   - Extract first page or specific pages

## Related Projects

- [Fess](https://github.com/codelibs/fess) - Full-text search server
- [Fess Thumbnail Playwright](https://github.com/codelibs/fess-thumbnail-playwright) - Playwright-based thumbnail generator

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## Support

- **Issues**: Report bugs or request features at [GitHub Issues](https://github.com/codelibs/fess-thumbnail-example/issues)
- **Discussions**: Ask questions at [Fess Discussion Forum](https://discuss.codelibs.org/c/fessen/)
- **Documentation**: Visit [Fess Documentation](https://fess.codelibs.org/documentation.html)
