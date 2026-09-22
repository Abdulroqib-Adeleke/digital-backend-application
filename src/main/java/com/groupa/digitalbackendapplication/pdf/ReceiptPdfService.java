package com.groupa.digitalbackendapplication.pdf;

import com.groupa.digitalbackendapplication.domain.dto.response.TransactionHistoryResponseDto;
import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.service.TransactionService;
import com.groupa.digitalbackendapplication.service.impl.TransactionServiceImpl;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service

public class ReceiptPdfService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a");

    public byte[] generatePDF(TransactionHistoryResponseDto dto) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A5, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(header());

            doc.add(statusPill(dto.transactionStatus()));

            Paragraph amount = new Paragraph(PdfStyles.money(dto.amount()), PdfStyles.font(26, Font.BOLD, PdfStyles.SLATE_800));
            amount.setAlignment(Element.ALIGN_CENTER);
            amount.setSpacingBefore(12);
            doc.add(amount);

            Paragraph when = new Paragraph(dto.createdAt().format(DT), PdfStyles.font(9, Font.NORMAL, PdfStyles.SLATE_500));
            when.setAlignment(Element.ALIGN_CENTER);
            when.setSpacingBefore(4);
            doc.add(when);

            Paragraph cut = new Paragraph();
            cut.setSpacingBefore(14);
            cut.setSpacingAfter(10);
            cut.add(new Chunk(new DottedLineSeparator()));
            doc.add(cut);

            Font lab = PdfStyles.font(9, Font.NORMAL, PdfStyles.SLATE_500);
            Font val = PdfStyles.font(9, Font.BOLD, PdfStyles.SLATE_800);
            Font sub = PdfStyles.font(7.5f, Font.NORMAL, PdfStyles.SLATE_400);

            PdfPTable t = new PdfPTable(new float[]{2, 3});
            t.setWidthPercentage(100);
            row(t, "Transaction Type", new Phrase(PdfStyles.nz(dto.transactionType().name()), val), lab);
            row(t, "Source Account", PdfStyles.two(dto.sourceAccountName(), val, PdfStyles.mask(dto.sourceAccount()), sub), lab);
            row(t, "Destination Account", PdfStyles.two(dto.destinationAccountName(), val, PdfStyles.mask(dto.destinationAccount()), sub), lab);
            row(t, "Description", new Phrase(PdfStyles.nz(dto.description()), val), lab);
            row(t, "Transaction ID", new Phrase(PdfStyles.nz(dto.transactionId().toString()), PdfStyles.font(9, Font.NORMAL, PdfStyles.SLATE_800)), lab);
            doc.add(t);
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate receipt PDF", e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
        return out.toByteArray();
    }

    private PdfPTable header() {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = PdfStyles.plain(PdfStyles.two("POI BANK", PdfStyles.font(20, Font.BOLD, BaseColor.WHITE),
                "TRANSACTION RECEIPT", PdfStyles.font(8, Font.NORMAL, PdfStyles.BLUE_200)), Element.ALIGN_CENTER);
        c.setBackgroundColor(PdfStyles.BLUE_600);
        c.setPadding(18);
        t.addCell(c);
        return t;
    }

    private PdfPTable statusPill(TransactionStatus status) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(32);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.setSpacingBefore(16);


        if(status.equals(TransactionStatus.PENDING)){
            BaseColor fg = PdfStyles.ORANGE_100;
            PdfPCell c = PdfStyles.plain(new Phrase(TransactionStatus.PENDING.name(),
                    PdfStyles.font(8, Font.BOLD, fg)), Element.ALIGN_CENTER);
            c.setBackgroundColor(PdfStyles.ORANGE_50);
            c.setBorder(Rectangle.BOX);
            c.setBorderColor(BaseColor.ORANGE);
            c.setPadding(5);
            t.addCell(c);
        }
        else if (status.equals(TransactionStatus.DECLINED)){
            BaseColor fg = PdfStyles.RED_600;
            PdfPCell c = PdfStyles.plain(new Phrase(TransactionStatus.DECLINED.name(),
                    PdfStyles.font(8, Font.BOLD, fg)), Element.ALIGN_CENTER);
            c.setBackgroundColor(PdfStyles.RED_50);
            c.setBorder(Rectangle.BOX);
            c.setBorderColor(PdfStyles.RED_100);
            c.setPadding(5);
            t.addCell(c);
        }
        else if (status.equals(TransactionStatus.SUCCESSFUL)){
            BaseColor fg = PdfStyles.GREEN_600;
            PdfPCell c = PdfStyles.plain(new Phrase(TransactionStatus.SUCCESSFUL.name(),
                    PdfStyles.font(8, Font.BOLD, fg)), Element.ALIGN_CENTER);
            c.setBackgroundColor(PdfStyles.GREEN_50);
            c.setBorder(Rectangle.BOX);
            c.setBorderColor(PdfStyles.GREEN_100);
            c.setPadding(5);
            t.addCell(c);
        }
        else{
            BaseColor fg = PdfStyles.GREEN;
            PdfPCell c = PdfStyles.plain(new Phrase("Undefined",
                    PdfStyles.font(8, Font.BOLD, fg)), Element.ALIGN_CENTER);
            c.setBackgroundColor(PdfStyles.GREEN_50);
            c.setBorder(Rectangle.BOX);
            c.setBorderColor(BaseColor.GREEN);
            c.setPadding(5);
            t.addCell(c);
        }

        return t;
    }

    private void row(PdfPTable t, String label, Phrase value, Font lab) {
        t.addCell(PdfStyles.ruled(new Phrase(label, lab), Element.ALIGN_LEFT, PdfStyles.SLATE_100, 0.6f));
        t.addCell(PdfStyles.ruled(value, Element.ALIGN_RIGHT, PdfStyles.SLATE_100, 0.6f));
    }
}
