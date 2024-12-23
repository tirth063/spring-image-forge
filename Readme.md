# 🖼️ SpringImageForge | Dynamic Image Generator API

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![REST API](https://img.shields.io/badge/REST%20API-v2-blue.svg)](https://swagger.io/)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

> 🚀 A powerful, lightweight Spring Boot REST API for dynamically generating placeholder images, perfect for mockups, prototypes, and testing environments.

## ✨ Features

- 🎨 Multiple image formats (PNG, SVG, JPEG, GIF, WebP)
- 📱 Retina display support (@2x, @3x)
- 🎯 Custom dimensions with smart defaults
- 🌈 Dynamic color customization (hex codes & color names)
- 📝 Custom text with multi-line support
- ⚡ High-performance image generation
- 🛡️ Built-in error handling and validation
- 📊 RESTful API design

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+ or Gradle 7.0+
- Spring Boot 3.2.0+

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/spring-image-forge.git

# Navigate to project directory
cd spring-image-forge

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

## 🎯 API Usage

### Base URL
```
http://localhost:8080/api/v2
```

### Generate Default Image
```http
GET /generate
```

### Generate Custom Image
```http
GET /generate/{width}x{height}[/{bgColor}][/{textColor}][.{format}]
```

### Examples

1. Basic Image (300x150):
```
/generate/300x150
```

2. Custom Colors:
```
/generate/600x400/FF0000/FFFFFF
```

3. Retina Display:
```
/generate/600x400@2x
```

4. Custom Text:
```
/generate/600x400?text=Hello+World
```

5. SVG Format:
```
/generate/600x400.svg
```

## 🎨 Supported Features

### Image Formats
- PNG (default)
- SVG (vector graphics)
- JPEG/JPG
- GIF
- WebP

### System Fonts
- serif
- sans-serif
- monospace
- dialog
- dialoginput

### Color Formats
- Hex codes (#FF0000)
- Color names (red, blue, etc.)
- Default fallback colors

## 🔧 Configuration

```yaml
# application.properties or application.yml
spring:
  application:
    name: spring-image-forge
server:
  port: 8080
```

## 🛠️ Technical Details

### Architecture
- RESTful API design
- Spring Boot 3.x
- Java AWT for image processing
- SVG generation support
- Error handling middleware
- Input validation
- Logging implementation

### Performance
- Lightweight response payloads
- Optimized image generation
- Caching support
- Memory-efficient processing

## 📚 Use Cases

1. **Development & Testing**
   - Placeholder images for prototypes
   - UI/UX mockups
   - Design system testing

2. **Content Management**
   - Dynamic banner generation
   - Thumbnail creation
   - Social media assets

3. **E-commerce**
   - Product image placeholders
   - Category thumbnails
   - Banner generation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=tirth063/spring-image-forge&type=Date)](https://star-history.com/#tirth063/spring-image-forge&Date)
---
## 🔗 Related Projects

- [Spring Boot Bill-Generation-System](https://github.com/tirth063/Bill-Generation-System)
- [Spring Boot Sale Campaign System](https://github.com/tirth063/Sale-Campaign-System)
- [Java-Servlet-and-MySQL Sign-in and Sign-up](https://github.com/tirth063/-User-Registration-and-Login-System-using-java-Servlet-and-MySQL)

---

Made with ❤️ by [Tirth R. Patel](https://github.com/tirth063)

