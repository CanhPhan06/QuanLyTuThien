package com.mycompany.charitymanagement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

public final class ExportUtils {

    private ExportUtils() {
    }

    public static <T> void exportTableToCsv(TableView<T> table, String title, String defaultFileName) {
        if (table == null || table.getItems() == null || table.getItems().isEmpty()) {
            DialogUtils.warning("Không có dữ liệu để xuất file.");
            return;
        }

        ExportTarget target = chooseExportTarget(title, defaultFileName);
        if (target == null) return;

        try {
            if (target.csv) {
                writeTableCsv(table, target.file);
            } else {
                writeTablePdf(table, target.file, title);
            }
            DialogUtils.info("Đã xuất file: " + target.file.getAbsolutePath());
        } catch (IOException ex) {
            DialogUtils.warning("Không thể xuất file. Vui lòng kiểm tra quyền ghi file hoặc thử thư mục khác.");
        }
    }

    public static void exportTextToCsv(String title, String defaultFileName, String content) {
        if (content == null || content.trim().isEmpty()) {
            DialogUtils.warning("Không có nội dung báo cáo để xuất file.");
            return;
        }

        ExportTarget target = chooseExportTarget(title, defaultFileName);
        if (target == null) return;

        try {
            if (target.csv) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(target.file, StandardCharsets.UTF_8))) {
                    writer.println("Nội dung báo cáo");
                    for (String line : content.split("\\R")) {
                        writer.println(csv(line));
                    }
                }
            } else {
                writeTextPdf(title, target.file, content);
            }
            DialogUtils.info("Đã xuất file: " + target.file.getAbsolutePath());
        } catch (IOException ex) {
            DialogUtils.warning("Không thể xuất file. Vui lòng kiểm tra quyền ghi file hoặc thử thư mục khác.");
        }
    }

    private static ExportTarget chooseExportTarget(String title, String defaultFileName) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(defaultPdfName(defaultFileName));
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV UTF-8", "*.csv");
        chooser.getExtensionFilters().addAll(pdfFilter, csvFilter);
        chooser.setSelectedExtensionFilter(pdfFilter);

        File file = chooser.showSaveDialog(App.getScene() == null ? null : App.getScene().getWindow());
        if (file == null) return null;

        FileChooser.ExtensionFilter selectedFilter = chooser.getSelectedExtensionFilter();
        boolean csvSelected = selectedFilter == csvFilter || file.getName().toLowerCase().endsWith(".csv");
        return new ExportTarget(ensureExtension(file, csvSelected ? ".csv" : ".pdf"), csvSelected);
    }

    private static final class ExportTarget {
        final File file;
        final boolean csv;
        ExportTarget(File file, boolean csv) {
            this.file = file;
            this.csv = csv;
        }
    }

    private static <T> void writeTableCsv(TableView<T> table, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            List<TableColumn<T, ?>> columns = visibleColumns(table);
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) writer.print(',');
                writer.print(csv(columns.get(i).getText()));
            }
            writer.println();

            for (T item : table.getItems()) {
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) writer.print(',');
                    Object value = columns.get(i).getCellObservableValue(item) == null
                            ? ""
                            : columns.get(i).getCellObservableValue(item).getValue();
                    writer.print(csv(value == null ? "" : String.valueOf(value)));
                }
                writer.println();
            }
        }
    }

    private static <T> void writeTablePdf(TableView<T> table, File file, String title) throws IOException {
        List<TableColumn<T, ?>> columns = visibleColumns(table);
        writePdf(title, file, writer -> {
            writer.wrappedLine(columns.stream().map(TableColumn::getText).collect(Collectors.joining(" | ")));
            writer.line("--------------------------------------------------------------------------------");
            for (T item : table.getItems()) {
                String row = columns.stream()
                        .map(column -> {
                            Object value = column.getCellObservableValue(item) == null
                                    ? ""
                                    : column.getCellObservableValue(item).getValue();
                            return value == null ? "" : String.valueOf(value);
                        })
                        .collect(Collectors.joining(" | "));
                writer.wrappedLine(row);
                writer.line("");
            }
        });
    }

    private static void writeTextPdf(String title, File file, String content) throws IOException {
        writePdf(title, file, writer -> {
            for (String line : content.split("\\R")) {
                writer.wrappedLine(line);
            }
        });
    }

    private static void writePdf(String title, File file, PdfContentWriter contentWriter) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfTextWriter writer = new PdfTextWriter(document);
            writer.line(title);
            writer.line("");
            contentWriter.write(writer);
            writer.close();
            document.save(file);
        }
    }

    @FunctionalInterface
    private interface PdfContentWriter {
        void write(PdfTextWriter writer) throws IOException;
    }

    private static <T> List<TableColumn<T, ?>> visibleColumns(TableView<T> table) {
        return table.getColumns().stream()
                .filter(TableColumn::isVisible)
                .collect(Collectors.toList());
    }

    private static String defaultPdfName(String defaultFileName) {
        if (defaultFileName == null || defaultFileName.trim().isEmpty()) {
            return "xuat-du-lieu.pdf";
        }
        return defaultFileName.replaceAll("(?i)\\.csv$", ".pdf");
    }

    private static File ensureExtension(File file, String extension) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase().endsWith(extension)) {
            return file;
        }
        return new File(path + extension);
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private static final class PdfTextWriter {

        private static final float MARGIN = 44;
        private static final float FONT_SIZE = 10;
        private static final float TITLE_SIZE = 16;
        private static final float LEADING = 15;

        private final PDDocument document;
        private final PDFont font;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float y;
        private boolean firstLine = true;

        PdfTextWriter(PDDocument document) throws IOException {
            this.document = document;
            this.font = loadFont(document);
            newPage();
        }

        void line(String text) throws IOException {
            if (y < MARGIN + LEADING) {
                newPage();
            }
            contentStream.setFont(font, firstLine ? TITLE_SIZE : FONT_SIZE);
            contentStream.showText(safePdfText(text));
            contentStream.newLineAtOffset(0, -LEADING);
            y -= LEADING;
            firstLine = false;
        }

        void wrappedLine(String text) throws IOException {
            for (String line : wrap(text, page.getMediaBox().getWidth() - MARGIN * 2)) {
                line(line);
            }
        }

        void close() throws IOException {
            if (contentStream != null) {
                contentStream.endText();
                contentStream.close();
                contentStream = null;
            }
        }

        private void newPage() throws IOException {
            if (contentStream != null) {
                contentStream.endText();
                contentStream.close();
            }
            page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);
            y = page.getMediaBox().getHeight() - MARGIN;
            contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, y);
        }

        private List<String> wrap(String text, float maxWidth) throws IOException {
            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
            String remaining = safePdfText(text);
            while (!remaining.isEmpty()) {
                int end = remaining.length();
                while (end > 0 && textWidth(remaining.substring(0, end)) > maxWidth) {
                    end--;
                }
                if (end <= 0) {
                    end = Math.min(1, remaining.length());
                }
                int breakAt = remaining.lastIndexOf(' ', end);
                if (breakAt > 20) {
                    end = breakAt;
                }
                lines.add(remaining.substring(0, end).trim());
                remaining = remaining.substring(Math.min(end, remaining.length())).trim();
            }
            if (lines.isEmpty()) {
                lines.add("");
            }
            return lines;
        }

        private float textWidth(String text) throws IOException {
            return font.getStringWidth(text) / 1000 * FONT_SIZE;
        }

        private static PDFont loadFont(PDDocument document) throws IOException {
            String[] candidates = {
                "C:\\Windows\\Fonts\\arial.ttf",
                "C:\\Windows\\Fonts\\segoeui.ttf",
                "C:\\Windows\\Fonts\\tahoma.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf",
                "/usr/share/fonts/TTF/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/msttcorefonts/Arial.ttf"
            };
            for (String candidate : candidates) {
                File fontFile = new File(candidate);
                if (fontFile.exists()) {
                    return PDType0Font.load(document, fontFile);
                }
            }
            IOException first = null;
            try {
                java.nio.file.Path fontDir = java.nio.file.Paths.get("/usr/share/fonts");
                if (java.nio.file.Files.isDirectory(fontDir)) {
                    java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(fontDir);
                    try {
                        java.util.Optional<java.nio.file.Path> ttf = walk
                            .filter(p -> p.toString().endsWith(".ttf"))
                            .filter(p -> {
                                String name = p.getFileName().toString().toLowerCase();
                                return name.contains("sans") || name.contains("arial") || name.contains("dejavu");
                            })
                            .findFirst();
                        if (ttf.isPresent()) {
                            return PDType0Font.load(document, ttf.get().toFile());
                        }
                    } finally {
                        walk.close();
                    }
                }
            } catch (IOException e) {
                first = e;
            }
            if (first != null) {
                throw first;
            }
            throw new IOException("Không tìm thấy font Unicode để xuất PDF.");
        }

        private static String safePdfText(String value) {
            return value == null ? "" : value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
        }
    }
}
