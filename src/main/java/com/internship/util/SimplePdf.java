package com.internship.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Minimal PDF writer (Helvetica text and rectangles) so exports need no third-party library. */
public class SimplePdf {
    private final int width;
    private final int height;
    private final List<StringBuilder> pages = new ArrayList<>();

    public SimplePdf(int width, int height) {
        this.width = width;
        this.height = height;
        newPage();
    }

    public void newPage() { pages.add(new StringBuilder()); }

    private StringBuilder current() { return pages.get(pages.size() - 1); }

    public void text(double x, double y, int size, boolean bold, String s) {
        current().append("BT /").append(bold ? "F2" : "F1").append(' ').append(size).append(" Tf ")
                .append(num(x)).append(' ').append(num(y)).append(" Td (").append(escape(s)).append(") Tj ET\n");
    }

    /** Centres text using an average Helvetica glyph width, which is close enough for headings. */
    public void centered(double y, int size, boolean bold, String s) {
        double w = (s == null ? 0 : s.length()) * size * (bold ? 0.56 : 0.5);
        text(Math.max(20, (width - w) / 2), y, size, bold, s);
    }

    public void rect(double x, double y, double w, double h, double lineWidth) {
        current().append(num(lineWidth)).append(" w ").append(num(x)).append(' ').append(num(y)).append(' ')
                .append(num(w)).append(' ').append(num(h)).append(" re S\n");
    }

    public byte[] toBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        int n = pages.size();
        write(out, "%PDF-1.4\n");
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < n; i++) kids.append(5 + 2 * i).append(" 0 R ");
        object(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        object(out, offsets, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>");
        object(out, offsets, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        object(out, offsets, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>");
        for (int i = 0; i < n; i++) {
            object(out, offsets, 5 + 2 * i, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + width + " " + height
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + (6 + 2 * i) + " 0 R >>");
            String content = pages.get(i).toString();
            object(out, offsets, 6 + 2 * i, "<< /Length " + content.length() + " >>\nstream\n" + content + "endstream");
        }
        int xref = out.size();
        write(out, "xref\n0 " + (offsets.size() + 1) + "\n0000000000 65535 f \n");
        for (int off : offsets) write(out, String.format(Locale.ROOT, "%010d 00000 n \n", off));
        write(out, "trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private static void object(ByteArrayOutputStream out, List<Integer> offsets, int id, String body) {
        offsets.add(out.size());
        write(out, id + " 0 obj\n" + body + "\nendobj\n");
    }

    private static void write(ByteArrayOutputStream out, String s) {
        out.writeBytes(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String num(double d) { return String.format(Locale.ROOT, "%.2f", d); }

    private static String escape(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : (s == null ? "" : s.replaceAll("\\s+", " ")).toCharArray()) {
            if (c == '\\' || c == '(' || c == ')') b.append('\\').append(c);
            else if (c < 32 || c > 255) b.append('?');
            else b.append(c);
        }
        return b.toString();
    }
}
