package ru.spliterash.warehouse.other;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import ru.spliterash.warehouse.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;

@UtilityClass
public class Utils {
    public void showInternalError(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(throwable.getMessage());
        alert.setTitle("Internal error");
        StringWriter writerStr = new StringWriter();
        PrintWriter writer = new PrintWriter(writerStr);
        throwable.printStackTrace(writer);
        alert.setContentText(writerStr.toString());
        alert.show();
    }

    public BufferedImage getBuffered(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public void showAndWait(Parent node, String title, Window caller) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (caller != null) {
            stage.initOwner(caller);
        }
        Scene scene = new Scene(node);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.showAndWait();
    }

    public javafx.scene.image.Image getImage(InputStream image, int x, int y) throws IOException {
        return getImage(ImageIO.read(image), x, y);
    }

    public javafx.scene.image.Image getImage(BufferedImage image, int x, int y) {
        image = Utils.getBuffered(image.getScaledInstance(x, y, Image.SCALE_SMOOTH));
        return SwingFXUtils.toFXImage(image, null);

    }

    public void showWaitAlert(Alert.AlertType type, String title, String head, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(head);
        alert.setTitle(title);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.setContentText(content);
        alert.initOwner(Main.getMain().getMainStage());
        alert.showAndWait();
    }

    public BooleanBinding boxSelected(ComboBox<?> box) {
        return new BooleanBinding() {
            final ReadOnlyIntegerProperty bind = box.getSelectionModel().selectedIndexProperty();

            {
                super.bind(bind);
            }

            @Override
            public void dispose() {
                super.unbind(bind);
            }

            @Override
            protected boolean computeValue() {
                return box.getSelectionModel().getSelectedIndex() != -1;
            }
        };
    }


    /**
     * Создаёт {@link BooleanBinding} из множества {@link ObservableValue<Boolean>}
     *
     * @return Биндинг который возращает true только если все Property true
     */
    @SafeVarargs
    public BooleanBinding andBinding(ObservableValue<Boolean>... array) {
        return new BooleanBinding() {
            {
                for (ObservableValue<Boolean> expression : array) {
                    super.bind(expression);
                }
            }

            @Override
            public void dispose() {
                for (ObservableValue<Boolean> value : array) {
                    super.unbind(value);
                }
            }

            @Override
            protected boolean computeValue() {
                return Arrays.stream(array).allMatch(ObservableValue::getValue);
            }
        };
    }


    public <T extends Parent> void loadFXML(T component) {
        String fileName = component.getClass().getSimpleName() + ".fxml";
        loadFXML(component, fileName);
    }

    public String getMonthName(int month) {
        Month m = Month.of(month + 1);
        return m.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
    }

    public <T extends Parent> void loadFXML(T component, URL resource) {
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setRoot(component);
        loader.setControllerFactory(theClass -> component);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void wrapTitle(Chart chart) {
        for (Node node : chart.lookupAll(".chart-title")) {
            Label label = (Label) node;
            label.setWrapText(true);
            label.setStyle("-fx-font-size: 17px");
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
        }
    }

    public void tooltipTime(Tooltip tooltip) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public <T extends Parent> void loadFXML(T component, String filename) {
        String fileName = component.getClass().getSimpleName() + ".fxml";
        loadFXML(component, component.getClass().getResource(fileName));
    }


    public <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public void setAnchorProperty(Node... nodes) {
        for (Node node : nodes) {
            AnchorPane.setRightAnchor(node, 0D);
            AnchorPane.setLeftAnchor(node, 0D);
            AnchorPane.setTopAnchor(node, 0D);
            AnchorPane.setBottomAnchor(node, 0D);
        }
    }

    public void onlyNumber(TextInputControl... fields) {
        for (TextInputControl field : fields) {
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    field.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }
    }

    public CellStyle getStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    public void fillSheet(Workbook workbook, String name, String[]... array) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet == null)
            sheet = workbook.createSheet(name);
        CellStyle style = getStyle(workbook);
        for (int i = 0; i < array.length; i++) {
            Row row = sheet.createRow(i);
            for (int k = 0; k < array[i].length; k++) {
                Cell cell = row.getCell(k, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String value = array[i][k];
                try {
                    cell.setCellValue(Double.parseDouble(value));
                } catch (Exception ex) {
                    cell.setCellValue(value);
                }
                cell.setCellStyle(style);
                System.out.println(cell.getAddress().toString());
            }
            row.setHeight((short) -1);
            sheet.autoSizeColumn(i);
        }
    }

    public void saveWorkbook(Workbook workbook, File destination) {
        try (FileOutputStream stream = new FileOutputStream(destination, false)) {
            workbook.write(stream);
        } catch (IOException e) {
            Utils.showWaitAlert(Alert.AlertType.ERROR, "Невозможно сохранить файл", "Ошибка при сохранении файла", e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeWindow(Node node) {
        Scene scene = node.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.close();
        } else
            throw new RuntimeException("Oops");
    }
}
