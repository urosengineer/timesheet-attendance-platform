package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.dto.export.AttendanceRecordExportDto;
import com.uros.timesheet.attendance.dto.export.ExportRequest;
import com.uros.timesheet.attendance.dto.export.LeaveRequestExportDto;
import com.uros.timesheet.attendance.exception.ExportException;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.AttendanceRecordExportMapper;
import com.uros.timesheet.attendance.mapper.LeaveRequestExportMapper;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.repository.LeaveRequestRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service("pdfExportService")
@RequiredArgsConstructor
@Slf4j
public class PdfExportServiceImpl implements ExportService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;
    private final AttendanceRecordExportMapper attendanceMapper;
    private final LeaveRequestExportMapper leaveMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 15;

    @Override
    public Resource exportToPdf(ExportRequest request) throws ExportException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(messageUtil.get("export.title",
                    userRepository.findById(request.getUserId())
                            .orElseThrow(() -> new ExportException(messageUtil.get("error.user.not.found")))
                            .getFullName()));
            contentStream.endText();
            yPosition -= LINE_HEIGHT * 2;

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(messageUtil.get("export.period",
                    request.getStartDate().format(DATE_FORMATTER),
                    request.getEndDate().format(DATE_FORMATTER)));
            contentStream.endText();
            yPosition -= LINE_HEIGHT * 2;

            switch (request.getExportType()) {
                case ATTENDANCE:
                    yPosition = exportAttendanceRecords(document, contentStream, request, yPosition);
                    break;
                case LEAVE_REQUESTS:
                    exportLeaveRequests(document, contentStream, request, yPosition);
                    break;
                case ALL:
                    yPosition = exportAttendanceRecords(document, contentStream, request, yPosition);
                    yPosition -= LINE_HEIGHT;
                    exportLeaveRequests(document, contentStream, request, yPosition);
                    break;
            }

            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, MARGIN);
            contentStream.showText(messageUtil.get("export.generated.by",
                    request.getRequestedBy().getFullName(),
                    java.time.LocalDate.now().format(DATE_FORMATTER)));
            contentStream.endText();

            contentStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new ByteArrayResource(out.toByteArray());

        } catch (IOException e) {
            log.error("PDF export failed", e);
            throw new ExportException(messageUtil.get("export.error.pdf.generation"));
        }
    }

    private float exportAttendanceRecords(PDDocument document, PDPageContentStream contentStream, ExportRequest request, float yPosition) throws IOException {
        List<AttendanceRecord> records = attendanceRecordRepository.findByUserIdAndDateBetween(
                request.getUserId(), request.getStartDate(), request.getEndDate());
        List<AttendanceRecordExportDto> dtoList = attendanceMapper.toDtoList(records);

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(messageUtil.get("export.section.attendance"));
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        contentStream.setFont(PDType1Font.HELVETICA, 9);

        for (AttendanceRecordExportDto dto : dtoList) {
            if (yPosition < MARGIN) {
                contentStream.close();
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = newPage.getMediaBox().getHeight() - MARGIN;
                contentStream.setFont(PDType1Font.HELVETICA, 9);
            }
            String recordText = String.format("%s: %s - %s (%s) - %s",
                    dto.getDate(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getType(),
                    dto.getStatus());

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(recordText);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }
        return yPosition;
    }

    private void exportLeaveRequests(PDDocument document, PDPageContentStream contentStream, ExportRequest request, float yPosition) throws IOException {
        List<LeaveRequest> requests = leaveRequestRepository.findByUserIdAndStartDateBetween(
                request.getUserId(), request.getStartDate(), request.getEndDate());
        List<LeaveRequestExportDto> dtoList = leaveMapper.toDtoList(requests);

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(messageUtil.get("export.section.leave"));
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        contentStream.setFont(PDType1Font.HELVETICA, 9);

        for (LeaveRequestExportDto dto : dtoList) {
            if (yPosition < MARGIN) {
                contentStream.close();
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = newPage.getMediaBox().getHeight() - MARGIN;
                contentStream.setFont(PDType1Font.HELVETICA, 9);
            }
            String leaveText = String.format("%s - %s: %s (%s) - %s",
                    dto.getStartDate(),
                    dto.getEndDate(),
                    dto.getType(),
                    dto.getNotes(),
                    dto.getStatus());

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(leaveText);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }
    }

    @Override
    public Resource exportToExcel(ExportRequest request) throws ExportException {
        throw new UnsupportedOperationException(messageUtil.get("export.error.excel.not.implemented"));
    }
}