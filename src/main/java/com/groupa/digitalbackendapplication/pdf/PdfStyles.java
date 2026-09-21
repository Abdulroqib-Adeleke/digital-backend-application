package com.groupa.digitalbackendapplication.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

final class PdfStyles {
    static final BaseColor HEADER_BG = new BaseColor(31, 73, 125);
    static final BaseColor ROW_ALT   = new BaseColor(220, 230, 241);
    static final BaseColor GREEN     = new BaseColor(0, 128, 0);
    static final BaseColor RED       = new BaseColor(192, 0, 0);
    static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    static final BaseColor BLUE_700 = new BaseColor(29, 78, 216),  BLUE_600 = new BaseColor(37, 99, 235);
    static final BaseColor BLUE_200 = new BaseColor(191, 219, 254), BLUE_50 = new BaseColor(239, 246, 255);
    static final BaseColor SLATE_50 = new BaseColor(248, 250, 252), SLATE_100 = new BaseColor(241, 245, 249);
    static final BaseColor SLATE_200 = new BaseColor(226, 232, 240), SLATE_400 = new BaseColor(148, 163, 184);
    static final BaseColor SLATE_500 = new BaseColor(100, 116, 139), SLATE_800 = new BaseColor(30, 41, 59);
    static final BaseColor GREEN_50 = new BaseColor(240, 253, 244), GREEN_100 = new BaseColor(220, 252, 231);
    static final BaseColor GREEN_600 = new BaseColor(22, 163, 74),  GREEN_700 = new BaseColor(21, 128, 61);
    static final BaseColor RED_50 = new BaseColor(254, 242, 242),   RED_100 = new BaseColor(254, 226, 226);
    static final BaseColor RED_600 = new BaseColor(220, 38, 38),    RED_700 = new BaseColor(185, 28, 28);
    static final BaseColor ORANGE_50 = new BaseColor(255, 251, 235), ORANGE_100 = new BaseColor(217, 119, 6);

    private static final String[] FONT_PATHS = {
            "/fonts/NotoSans-Regular.ttf",                          // file you add to src/main/resources/fonts
            "/net/sf/jasperreports/fonts/dejavu/DejaVuSans.ttf"     // from the jasperreports-fonts dependency
    };

    private static final BaseFont BASE;
    static final String CURRENCY;

    static {
        BaseFont bf = null;
        for (String path : FONT_PATHS) {
            try (InputStream in = PdfStyles.class.getResourceAsStream(path)) {
                if (in != null) {
                    bf = BaseFont.createFont(path, BaseFont.IDENTITY_H,
                            BaseFont.EMBEDDED, true, in.readAllBytes(), null);
                    break;
                }
            } catch (Exception ignored) {
                // try the next path
            }
        }
        if (bf == null) {
            try {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot load any PDF font", e);
            }
            CURRENCY = "NGN";   // Helvetica has no naira glyph
        } else {
            CURRENCY = "₦";
        }
        BASE = bf;
    }

    static Font font(float size, int style, BaseColor color) { return new Font(BASE, size, style, color); }
    static Font font(float size) { return font(size, Font.NORMAL, BaseColor.BLACK); }

    static String money(BigDecimal v) { return v == null ? "-" : String.format("%s %,.2f", CURRENCY, v); }    static String nz(String s) { return s == null || s.isBlank() ? "-" : s; }

    static String mask(String acct) {
        return (acct == null || acct.length() < 4) ? nz(acct) : "******" + acct.substring(acct.length() - 4);
    }

    static PdfPCell cell(String text, Font font, int align, BaseColor bg, float padding) {
        PdfPCell c = new PdfPCell(new Phrase(nz(text), font));
        c.setHorizontalAlignment(align);
        c.setPadding(padding);
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }

    static PdfPCell plain(Phrase p, int align) {
        PdfPCell c = new PdfPCell(p);
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(0);
        return c;
    }

    /** Cell with only a bottom rule, used for table rows. */
    static PdfPCell ruled(Phrase p, int align, BaseColor line, float width) {
        PdfPCell c = plain(p, align);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(line);
        c.setBorderWidth(width);
        c.setPaddingTop(7);
        c.setPaddingBottom(7);
        return c;
    }

    /** Two-line phrase: a main line and a smaller line under it. */
    static Phrase two(String top, Font f1, String bottom, Font f2) {
        Phrase p = new Phrase();
        p.add(new Chunk(nz(top), f1));
        p.add(Chunk.NEWLINE);
        p.add(new Chunk(nz(bottom), f2));
        return p;
    }
}
