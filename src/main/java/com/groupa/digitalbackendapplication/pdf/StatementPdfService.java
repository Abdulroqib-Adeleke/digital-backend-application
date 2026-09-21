package com.groupa.digitalbackendapplication.pdf;

import com.groupa.digitalbackendapplication.domain.dto.request.StatementData;
import com.groupa.digitalbackendapplication.domain.dto.request.StatementLine;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StatementPdfService {

    private static final String FOOTER_NOTE = "POI Bank is a registered financial institution."; // your regulatory wording
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final Font CAP = PdfStyles.font(7, Font.BOLD, PdfStyles.SLATE_400);
    private static final Font VAL = PdfStyles.font(9, Font.BOLD, PdfStyles.SLATE_800);

    public byte[] generate(StatementData d) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageFooterEvent());
            doc.open();

            doc.add(header());
            doc.add(holderAndAccount(d));
            doc.add(summary(d));

            Paragraph h = new Paragraph("TRANSACTION HISTORY", PdfStyles.font(9, Font.BOLD, PdfStyles.SLATE_800));
            h.setSpacingBefore(14);
            h.setSpacingAfter(8);
            doc.add(h);
            doc.add(transactions(d));

            Paragraph end = new Paragraph("End of statement. If you notice any discrepancies, please contact customer support within 30 days.\n"
                    + FOOTER_NOTE, PdfStyles.font(7, Font.NORMAL, PdfStyles.SLATE_400));
            end.setAlignment(Element.ALIGN_CENTER);
            end.setSpacingBefore(16);
            doc.add(end);
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate statement PDF", e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
        return out.toByteArray();
    }

    private PdfPTable header() {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        PdfPCell left = PdfStyles.plain(PdfStyles.two("POI BANK", PdfStyles.font(18, Font.BOLD, BaseColor.WHITE),
                "Digital Banking Solutions", PdfStyles.font(8, Font.NORMAL, PdfStyles.BLUE_200)), Element.ALIGN_LEFT);
        PdfPCell right = PdfStyles.plain(PdfStyles.two("ACCOUNT STATEMENT", PdfStyles.font(12, Font.BOLD, BaseColor.WHITE),
                "Generated: " + LocalDate.now().format(D), PdfStyles.font(8, Font.NORMAL, PdfStyles.BLUE_200)), Element.ALIGN_RIGHT);
        for (PdfPCell c : new PdfPCell[]{left, right}) {
            c.setBackgroundColor(PdfStyles.BLUE_700);
            c.setPadding(16);
            t.addCell(c);
        }
        return t;
    }

    private PdfPTable holderAndAccount(StatementData d) {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);

        Paragraph holder = new Paragraph();
        holder.add(new Chunk("ACCOUNT HOLDER", CAP));
        holder.add(Chunk.NEWLINE);
        holder.add(new Chunk(PdfStyles.nz(d.accountName()), PdfStyles.font(13, Font.BOLD, PdfStyles.SLATE_800)));

        Paragraph acct = new Paragraph();
        acct.setAlignment(Element.ALIGN_RIGHT);
        String[][] rows = {{"ACCOUNT NUMBER", PdfStyles.mask(d.accountNumber())},
                {"ACCOUNT TYPE", PdfStyles.nz(d.accountType())},
                {"STATEMENT PERIOD", d.from().format(D) + " - " + d.to().format(D)}};
        for (String[] r : rows) {
            acct.add(new Chunk(r[0], CAP));
            acct.add(Chunk.NEWLINE);
            acct.add(new Chunk(r[1], VAL));
            acct.add(Chunk.NEWLINE);
        }

        for (Paragraph p : new Paragraph[]{holder, acct}) {
            PdfPCell c = new PdfPCell();
            c.addElement(p);
            c.setBackgroundColor(PdfStyles.SLATE_50);
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(PdfStyles.SLATE_200);
            c.setPadding(14);
            t.addCell(c);
        }
        return t;
    }

    private PdfPTable summary(StatementData d) {
        BigDecimal in = d.lines().stream().filter(StatementLine::credit)
                .map(StatementLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outAmt = d.lines().stream().filter(l -> !l.credit())
                .map(StatementLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        PdfPTable t = new PdfPTable(new float[]{1, 0.08f, 1, 0.08f, 1, 0.08f, 1});
        t.setWidthPercentage(100);
        t.setSpacingBefore(14);
        card(t, "Opening Balance", PdfStyles.money(d.openingBalance()), PdfStyles.SLATE_50, PdfStyles.SLATE_100, PdfStyles.SLATE_500, PdfStyles.SLATE_800, 12);
        gap(t);
        card(t, "Total In", PdfStyles.money(in), PdfStyles.GREEN_50, PdfStyles.GREEN_100, PdfStyles.GREEN_600, PdfStyles.GREEN_700, 12);
        gap(t);
        card(t, "Total Out", PdfStyles.money(outAmt), PdfStyles.RED_50, PdfStyles.RED_100, PdfStyles.RED_600, PdfStyles.RED_700, 12);
        gap(t);
        card(t, "Closing Balance", PdfStyles.money(d.closingBalance()), PdfStyles.BLUE_50, PdfStyles.BLUE_200, PdfStyles.BLUE_600, PdfStyles.BLUE_700, 14);
        return t;
    }

    private void card(PdfPTable t, String label, String value, BaseColor bg, BaseColor border,
                      BaseColor labelColor, BaseColor valueColor, float valueSize) {
        PdfPCell c = PdfStyles.plain(PdfStyles.two(label, PdfStyles.font(7, Font.NORMAL, labelColor),
                value, PdfStyles.font(valueSize, Font.BOLD, valueColor)), Element.ALIGN_LEFT);
        c.setBackgroundColor(bg);
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(border);
        c.setBorderWidth(0.8f);
        c.setPadding(9);
        t.addCell(c);
    }

    private void gap(PdfPTable t) { t.addCell(PdfStyles.plain(new Phrase(""), Element.ALIGN_LEFT)); }

    private PdfPTable transactions(StatementData d) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{2f, 4.4f, 1.6f, 2.2f, 2.2f});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);

        Font hf = PdfStyles.font(7, Font.BOLD, PdfStyles.SLATE_500);
        String[] heads = {"DATE", "DESCRIPTION", "TYPE", "AMOUNT (" + PdfStyles.CURRENCY + ")", "BALANCE (" + PdfStyles.CURRENCY + ")"};
        int[] aligns = {Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};
        for (int i = 0; i < heads.length; i++) {
            t.addCell(PdfStyles.ruled(new Phrase(heads[i], hf), aligns[i], PdfStyles.SLATE_200, 1.5f));
        }

        Font base = PdfStyles.font(8, Font.NORMAL, PdfStyles.SLATE_500);
        Font main = PdfStyles.font(9, Font.BOLD, PdfStyles.SLATE_800);
        Font sub = PdfStyles.font(7, Font.NORMAL, PdfStyles.SLATE_500);

        for (StatementLine l : d.lines()) {
            String amt = (l.credit() ? "+" : "-") + String.format("%,.2f", l.amount());
            Font af = PdfStyles.font(9, Font.BOLD, l.credit() ? PdfStyles.GREEN_600 : PdfStyles.RED_600);

            t.addCell(PdfStyles.ruled(new Phrase(l.date().format(D), base), Element.ALIGN_LEFT, PdfStyles.SLATE_100, 0.6f));
            t.addCell(PdfStyles.ruled(PdfStyles.two(l.description(), main, l.detail(), sub), Element.ALIGN_LEFT, PdfStyles.SLATE_100, 0.6f));
            t.addCell(PdfStyles.ruled(new Phrase(PdfStyles.nz(l.type()), base), Element.ALIGN_LEFT, PdfStyles.SLATE_100, 0.6f));
            t.addCell(PdfStyles.ruled(new Phrase(amt, af), Element.ALIGN_RIGHT, PdfStyles.SLATE_100, 0.6f));
            t.addCell(PdfStyles.ruled(new Phrase(l.balanceAfter() == null ? "-" : String.format("%,.2f", l.balanceAfter()), main),
                    Element.ALIGN_RIGHT, PdfStyles.SLATE_100, 0.6f));
        }
        if (d.lines().isEmpty()) {
            PdfPCell empty = PdfStyles.ruled(new Phrase("No transactions in this period", base), Element.ALIGN_CENTER, PdfStyles.SLATE_100, 0.6f);
            empty.setColspan(5);
            t.addCell(empty);
        }
        return t;
    }
}
