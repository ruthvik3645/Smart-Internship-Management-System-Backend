package com.internship;

import com.internship.util.SimplePdf;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class SimplePdfTest {
    @Test
    void producesPdfWithEscapedText() {
        SimplePdf pdf = new SimplePdf(595, 842);
        pdf.text(40, 800, 12, true, "Hello (world) \\ test");
        pdf.newPage();
        pdf.centered(400, 20, false, "Page two");
        String out = new String(pdf.toBytes(), StandardCharsets.ISO_8859_1);
        assertTrue(out.startsWith("%PDF-1.4"));
        assertTrue(out.contains("/Count 2"));
        assertTrue(out.contains("Hello \\(world\\) \\\\ test"));
        assertTrue(out.trim().endsWith("%%EOF"));
    }
}
