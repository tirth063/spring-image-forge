package com.tirth.sb;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v2")
public class ImageGeneratorV2Controller {
    private static final Logger logger = LoggerFactory.getLogger(ImageGeneratorV2Controller.class);
    private static final int MIN_SIZE = 10;
    private static final int MAX_SIZE = 4000;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 150;

    private static final Map<String, String> SUPPORTED_FORMATS = new HashMap<>() {{
        put("svg", "image/svg+xml");
        put("png", "image/png");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("gif", "image/gif");
        put("webp", "image/webp");
    }};

    // Using only system fonts to avoid licensing issues
    private static final List<String> SUPPORTED_FONTS = Arrays.asList(
            "serif", "sans-serif", "monospace", "dialog", "dialoginput"
    );

    private static final Map<String, String> CSS_COLORS = new HashMap<>() {{
        put("black", "000000");
        put("white", "FFFFFF");
        put("red", "FF0000");
        put("green", "008000");
        put("blue", "0000FF");
        put("yellow", "FFFF00");
        put("purple", "800080");
        put("gray", "808080");
        put("orange", "FFA500");
        put("pink", "FFC0CB");
    }};

    @GetMapping("/")
    public ResponseEntity<String> getDocumentation() {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(generateDocumentationHtml(baseUrl));
    }

    private String generateDocumentationHtml(String baseUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Image Generator API Documentation</title>
                <style>
                    body { font-family: sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; }
                    code { background: #f4f4f4; padding: 2px 5px; border-radius: 3px; }
                    .example { margin: 10px 0; padding: 10px; background: #f8f9fa; border-radius: 5px; }
                </style>
            </head>
            <body>
                <h1>Image Generator API Documentation</h1>
                <h2>Base URL: %s/api/v2</h2>
                <h2>Basic Usage</h2>
                <div class="example">
                    <code>/generate/600x400</code> - Creates a 600x400 image
                </div>
                <h2>Supported Features</h2>
                <ul>
                    <li>Formats: PNG (default), SVG, JPEG, GIF, WebP</li>
                    <li>System Fonts: serif, sans-serif, monospace, dialog</li>
                    <li>Retina Support: @2x, @3x</li>
                    <li>Custom Colors: Hex codes or color names</li>
                </ul>
                <h2>Examples</h2>
                <div class="example">
                    <img src="%s/api/v2/generate/300x150" alt="Example Image">
                    <p><code>/generate/300x150</code></p>
                </div>
            </body>
            </html>
            """, baseUrl, baseUrl);
    }

    @GetMapping("/generate")
    public void generateDefaultImage(HttpServletResponse response) {
        try {
            generateImage(DEFAULT_WIDTH + "x" + DEFAULT_HEIGHT, null, null, "png",
                    "Default Image", "sans-serif", response);
        } catch (Exception e) {
            handleError(response, "Error generating default image", e);
        }
    }

    @GetMapping(value = {
            "/generate/{dimensions}",
            "/generate/{dimensions}/{format}",
            "/generate/{dimensions}/{bgColor}/{textColor}",
            "/generate/{dimensions}/{bgColor}/{textColor}/{format}",
            "/generate/{dimensions}/{bgColor}/{textColor}.{format}"
    })
    public void generateImage(
            @PathVariable String dimensions,
            @PathVariable(required = false) String bgColor,
            @PathVariable(required = false) String textColor,
            @PathVariable(required = false) String format,
            @RequestParam(required = false) String text,
            @RequestParam(required = false, defaultValue = "sans-serif") String font,
            HttpServletResponse response) {

        try {
            ImageSpec imageSpec = parseDimensions(dimensions);
            String finalFormat = validateFormat(format != null ? format : "png");
            String finalBgColor = validateColor(bgColor, "dddddd");
            String finalTextColor = validateColor(textColor, "999999");
            String finalFont = validateFont(font);
            String finalText = processText(text, imageSpec.width + "x" + imageSpec.height);

            response.setContentType(SUPPORTED_FORMATS.get(finalFormat));

            if (finalFormat.equals("svg")) {
                generateSvgImage(imageSpec, finalBgColor, finalTextColor, finalText, finalFont, response);
            } else {
                generateRasterImage(imageSpec, finalBgColor, finalTextColor, finalText,
                        finalFont, finalFormat, response);
            }
        } catch (Exception e) {
            handleError(response, "Failed to generate image", e);
        }
    }

    private void generateRasterImage(ImageSpec spec, String bgColor, String textColor,
                                     String text, String font, String format,
                                     HttpServletResponse response) throws IOException {
        BufferedImage img = createRasterImage(spec, bgColor, textColor, text, font);
        ImageIO.write(img, format.equals("jpg") ? "jpeg" : format, response.getOutputStream());
    }

    private void generateSvgImage(ImageSpec spec, String bgColor, String textColor,
                                  String text, String font, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(createSvgContent(spec, bgColor, textColor, text, font));
    }

    private String createSvgContent(ImageSpec spec, String bgColor, String textColor,
                                    String text, String font) {
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" " +
                        "width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                spec.width, spec.height, spec.width, spec.height));

        // Background
        svg.append(String.format("  <rect width=\"100%%\" height=\"100%%\" fill=\"#%s\"/>\n",
                bgColor));

        // Text
        String[] lines = text.split("\n");
        float fontSize = Math.min(spec.height / 5f, 100f);
        float lineHeight = fontSize * 1.2f;
        float startY = (spec.height - (lineHeight * (lines.length - 1))) / 2f;

        for (int i = 0; i < lines.length; i++) {
            float yPos = startY + (i * lineHeight);
            svg.append(String.format("  <text x=\"50%%\" y=\"%.1f\" " +
                            "font-family=\"%s\" font-size=\"%.1fpx\" fill=\"#%s\" " +
                            "text-anchor=\"middle\" dominant-baseline=\"middle\">%s</text>\n",
                    yPos, font, fontSize, textColor, escapeXml(lines[i])));
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private BufferedImage createRasterImage(ImageSpec spec, String bgColor, String textColor,
                                            String text, String font) {
        int width = spec.width * spec.retinaScale;
        int height = spec.height * spec.retinaScale;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        try {
            // Set background
            g2d.setColor(Color.decode("#" + bgColor));
            g2d.fillRect(0, 0, width, height);

            // Configure text rendering
            g2d.setColor(Color.decode("#" + textColor));
            int fontSize = Math.min(height / 5, 100) * spec.retinaScale;
            g2d.setFont(new Font(font, Font.PLAIN, fontSize));

            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw text
            drawMultilineText(g2d, text, width, height);

            return img;
        } finally {
            g2d.dispose();
        }
    }

    private void drawMultilineText(Graphics2D g2d, String text, int width, int height) {
        String[] lines = text.split("\n");
        FontMetrics fm = g2d.getFontMetrics();

        int lineHeight = fm.getHeight();
        int totalHeight = lineHeight * lines.length;
        int y = (height - totalHeight) / 2 + fm.getAscent();

        for (String line : lines) {
            int x = (width - fm.stringWidth(line)) / 2;
            g2d.drawString(line, x, y);
            y += lineHeight;
        }
    }

    private void handleError(HttpServletResponse response, String message, Exception e) {
        logger.error(message, e);
        try {
            BufferedImage errorImg = createErrorImage(message);
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            ImageIO.write(errorImg, "png", response.getOutputStream());
        } catch (IOException ioe) {
            logger.error("Failed to generate error image", ioe);
        }
    }

    private BufferedImage createErrorImage(String message) {
        return createRasterImage(
                new ImageSpec(400, 200, 1),
                "FFFFFF",
                "FF0000",
                "Error Occurred\nPlease check the documentation",
                "sans-serif"
        );
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static class ImageSpec {
        final int width;
        final int height;
        final int retinaScale;

        ImageSpec(int width, int height, int retinaScale) {
            this.width = width;
            this.height = height;
            this.retinaScale = retinaScale;
        }
    }

    private ImageSpec parseDimensions(String dimensions) throws ImageGenerationException {
        Pattern pattern = Pattern.compile("(\\d+)(?:x(\\d+))?(?:@(\\d)x)?");
        Matcher matcher = pattern.matcher(dimensions);

        if (!matcher.matches()) {
            throw new ImageGenerationException("Invalid dimension format. Use width[x height][@2x|@3x]");
        }

        int width = Integer.parseInt(matcher.group(1));
        int height = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : width;
        int retinaScale = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 1;

        if (width < MIN_SIZE || height < MIN_SIZE) {
            throw new ImageGenerationException("Image dimensions must be at least " + MIN_SIZE + "x" + MIN_SIZE);
        }

        if (width > MAX_SIZE || height > MAX_SIZE) {
            throw new ImageGenerationException("Image dimensions must not exceed " + MAX_SIZE + "x" + MAX_SIZE);
        }

        return new ImageSpec(width, height, retinaScale);
    }

    private String validateFormat(String format) throws ImageGenerationException {
        String normalizedFormat = format.toLowerCase();
        if (!SUPPORTED_FORMATS.containsKey(normalizedFormat)) {
            throw new ImageGenerationException("Unsupported format: " + format);
        }
        return normalizedFormat;
    }

    private String validateColor(String color, String defaultColor) {
        if (color == null) return defaultColor;

        // Check if it's a CSS color name
        String hexColor = CSS_COLORS.get(color.toLowerCase());
        if (hexColor != null) return hexColor;

        // Validate hex color
        String cleaned = color.replaceAll("[^0-9A-Fa-f]", "");
        return cleaned.isEmpty() ? defaultColor : cleaned;
    }

    private String validateFont(String font) {
        String normalizedFont = font.toLowerCase();
        return SUPPORTED_FONTS.contains(normalizedFont) ? normalizedFont : "sans-serif";
    }

    private String processText(String text, String defaultText) {
        if (text == null) return defaultText;
        return text.replace("+", " ")
                .replace("\\n", "\n")
                .replace("%20", " ");
    }

    private static class ImageGenerationException extends Exception {
        ImageGenerationException(String message) {
            super(message);
        }
    }
}