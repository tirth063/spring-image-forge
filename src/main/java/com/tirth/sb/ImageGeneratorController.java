package com.tirth.sb;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController("/v1")
public class ImageGeneratorController {

    @GetMapping(value = "/generate-image", produces = MediaType.IMAGE_PNG_VALUE)
    public void generateImage(
            @RequestParam String size,
            @RequestParam(required = false,defaultValue = "dddddd") String bgColor,
            @RequestParam(required = false,defaultValue = "999999") String textColor,
            @RequestParam(required = false) String text,
            @RequestParam(required = false, defaultValue = "Lato") String font,
            @RequestParam(required = false, defaultValue = "png") String format,
            HttpServletResponse response) throws IOException {

        // Parse size parameter
        String[] dimensions = size.split("x");
        int width = Integer.parseInt(dimensions[0]);
        int height = (dimensions.length > 1) ? Integer.parseInt(dimensions[1]) : width;

        // Validate dimensions
        if (width < 10 || height < 10 || width > 4000 || height > 4000) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid dimensions. Width and height must be between 10 and 4000.");
            return;
        }

        // Set default values if not provided
        bgColor = (bgColor != null) ? bgColor : "FFFFFF";
        textColor = (textColor != null) ? textColor : "000000";
        text = (text != null && !text.isEmpty()) ? text : width + "x" + height;

        // Create a buffered image
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        try {
            // Set background color
            g2d.setColor(Color.decode("#" + bgColor));
            g2d.fillRect(0, 0, width, height);

            // Set text color and font
            g2d.setColor(Color.decode("#" + textColor));
            g2d.setFont(new Font(font, Font.PLAIN, Math.min(height / 5, 100)));

            // Enable anti-aliasing for better text quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw the text in the center
            FontMetrics fm = g2d.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(text, x, y);
        } finally {
            g2d.dispose();
        }

        // Set the content type based on the format
        response.setContentType("image/" + format.toLowerCase());

        // Write the image directly to the response output stream
        ImageIO.write(img, format.toLowerCase(), response.getOutputStream());
    }
}