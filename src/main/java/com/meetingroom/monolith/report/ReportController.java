package com.meetingroom.monolith.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.meetingroom.monolith.booking.Booking;
import com.meetingroom.monolith.booking.BookingRepository;
import com.meetingroom.monolith.room.Room;
import com.meetingroom.monolith.room.RoomRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public ReportController(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        List<Booking> allBookings = bookingRepository.findAll();
        List<Room> allRooms = roomRepository.findAll();

        long upcoming = allBookings.stream().filter(b -> "Upcoming".equalsIgnoreCase(b.getStatus())).count();
        long ongoing = allBookings.stream().filter(b -> "Ongoing".equalsIgnoreCase(b.getStatus())).count();
        long completed = allBookings.stream().filter(b -> "Completed".equalsIgnoreCase(b.getStatus())).count();
        long cancelled = allBookings.stream().filter(b -> "Cancelled".equalsIgnoreCase(b.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", allBookings.size());
        stats.put("upcoming", upcoming);
        stats.put("ongoing", ongoing);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("totalRooms", allRooms.size());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/bookings/excel")
    public ResponseEntity<byte[]> exportBookingsExcel() throws Exception {
        List<Booking> bookings = bookingRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bookings Report");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Title", "Room Name", "User Name", "Email", "Date", "Start Time", "End Time", "Status"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Booking b : bookings) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(b.getId());
                row.createCell(1).setCellValue(b.getTitle());
                row.createCell(2).setCellValue(b.getRoomName() != null ? b.getRoomName() : "");
                row.createCell(3).setCellValue(b.getUserName() != null ? b.getUserName() : "");
                row.createCell(4).setCellValue(b.getUserEmail() != null ? b.getUserEmail() : "");
                row.createCell(5).setCellValue(b.getBookingDate() != null ? b.getBookingDate().toString() : "");
                row.createCell(6).setCellValue(b.getStartTime() != null ? b.getStartTime().toString() : "");
                row.createCell(7).setCellValue(b.getEndTime() != null ? b.getEndTime().toString() : "");
                row.createCell(8).setCellValue(b.getStatus() != null ? b.getStatus() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            byte[] bytes = out.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings_report.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        }
    }

    @GetMapping("/bookings/pdf")
    public ResponseEntity<byte[]> exportBookingsPdf() throws Exception {
        List<Booking> bookings = bookingRepository.findAll();

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Meeting Room Booking System - Bookings Summary Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2, 2, 2});

        String[] headers = {"ID", "Title", "Room", "User", "Date", "Time", "Status"};
        com.itextpdf.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setBackgroundColor(new BaseColor(31, 60, 146)); // Corporate Primary Blue
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        com.itextpdf.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);

        for (Booking b : bookings) {
            table.addCell(new Phrase(String.valueOf(b.getId()), dataFont));
            table.addCell(new Phrase(b.getTitle(), dataFont));
            table.addCell(new Phrase(b.getRoomName() != null ? b.getRoomName() : "", dataFont));
            table.addCell(new Phrase(b.getUserName() != null ? b.getUserName() : "", dataFont));
            table.addCell(new Phrase(b.getBookingDate() != null ? b.getBookingDate().toString() : "", dataFont));
            table.addCell(new Phrase((b.getStartTime() != null ? b.getStartTime().toString() : "") + "-" + (b.getEndTime() != null ? b.getEndTime().toString() : ""), dataFont));
            table.addCell(new Phrase(b.getStatus() != null ? b.getStatus() : "", dataFont));
        }

        document.add(table);
        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

    @GetMapping("/rooms/excel")
    public ResponseEntity<byte[]> exportRoomsExcel() throws Exception {
        List<Room> rooms = roomRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Rooms Inventory");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Room Name", "Floor", "Capacity", "Facilities", "Status"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Room r : rooms) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getName());
                row.createCell(2).setCellValue(r.getFloor() != null ? r.getFloor() : 0);
                row.createCell(3).setCellValue(r.getCapacity() != null ? r.getCapacity() : 0);
                row.createCell(4).setCellValue(r.getFacilities() != null ? r.getFacilities() : "");
                row.createCell(5).setCellValue(r.getStatus() != null ? r.getStatus() : "");
            }

            workbook.write(out);
            byte[] bytes = out.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rooms_report.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        }
    }
}
