package com.groupa.digitalbackendapplication.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class PageFooterEvent extends PdfPageEventHelper {

    private PdfTemplate totalPagesTemplate;
    private BaseFont baseFont;

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        totalPagesTemplate = writer.getDirectContent().createTemplate(50, 12);
        try {
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create footer font", e);
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        String text = "Page " + writer.getPageNumber() + " of ";
        float textWidth = baseFont.getWidthPoint(text, 8);
        float x = (document.right() + document.left()) / 2;
        float y = document.bottom() - 15;

        cb.beginText();
        cb.setFontAndSize(baseFont, 8);
        cb.setTextMatrix(x - textWidth / 2 - 13, y);
        cb.showText(text);
        cb.endText();

        cb.addTemplate(totalPagesTemplate, x - textWidth / 2 + 13, y);
        cb.restoreState();
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        totalPagesTemplate.beginText();
        totalPagesTemplate.setFontAndSize(baseFont, 8);
        totalPagesTemplate.setTextMatrix(0, 0);
        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
        totalPagesTemplate.endText();
    }
}
