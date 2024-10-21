import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите URL Музыки:");
        String URLMusic = scanner.nextLine();
        System.out.println("Введите URL Картинки:");
        String URLPicture = scanner.nextLine();

        // Создаем потоки для загрузки музыки и картинки
        Thread musicThread = new Thread(() -> downloadFileWithProgress(URLMusic, "file.mp3"));
        Thread pictureThread = new Thread(() -> downloadFileWithProgress(URLPicture, "picture.jpg"));

        musicThread.start();
        pictureThread.start();
    }

    public static void downloadFileWithProgress(String fileURL, String filePath) {
        try {
            if (!isURLAvailable(fileURL)) {
                System.out.println("URL недоступен: " + fileURL);
                return;
            }

            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int fileSize = connection.getContentLength();

            try (InputStream is = connection.getInputStream();
                 OutputStream os = new FileOutputStream(filePath)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                int downloadedBytes = 0;
                int lastPercentage = 0;

                // Чтение данных с отображением прогресса
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;

                    int percentage = (int) ((double) downloadedBytes / fileSize * 100);
                    if (percentage != lastPercentage) {
                        System.out.println("Загрузка " + filePath + ": " + percentage + "%");
                        lastPercentage = percentage;
                    }
                }

                System.out.println(filePath + " успешно загружен.");
            }

            // Асинхронное открытие файла
            openFileAsync(filePath);

            // Проверка типа картинки после загрузки
            if (filePath.endsWith("jpg") || filePath.endsWith("jpeg")) {
                String fileType = getFileType(filePath);
                if (Objects.equals(fileType, "JPEG")) {
                    System.out.println("Тип файла: JPEG");
                } else {
                    System.out.println("Тип файла не совпадает");
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    public static void openFileAsync(String filePath) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(new File(filePath));
                System.out.println("Открытие файла: " + filePath);
            } catch (IOException e) {
                System.err.println("Не удалось открыть файл: " + filePath);
            }
        }).start();
    }

    public static boolean isURLAvailable(String fileURL) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(fileURL).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getFileType(String filename) {
        try (FileInputStream fis = new FileInputStream(filename)) {
            byte[] magicBytes = new byte[4];
            if (fis.read(magicBytes) != -1) {
                return determineFileType(magicBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String determineFileType(byte[] magicBytes) {
        if (magicBytes.length < 4) {
            return "Неопределенный тип";
        }

        // Определение различных типов файлов
        if (magicBytes[0] == (byte) 0x49 && magicBytes[1] == (byte) 0x44 &&
                magicBytes[2] == (byte) 0x33) {
            return "MP3";
        } else if (magicBytes[0] == (byte) 0x89 && magicBytes[1] == (byte) 0x50 &&
                magicBytes[2] == (byte) 0x4E && magicBytes[3] == (byte) 0x47) {
            return "PNG";
        } else if (magicBytes[0] == (byte) 0xFF && magicBytes[1] == (byte) 0xD8) {
            return "JPEG";
        } else if (magicBytes[0] == (byte) 0x25 && magicBytes[1] == (byte) 0x50 &&
                magicBytes[2] == (byte) 0x44 && magicBytes[3] == (byte) 0x46) {
            return "PDF";
        } else if (magicBytes[0] == (byte) 0x42 && magicBytes[1] == (byte) 0x4D) {
            return "BMP";
        }

        return "Неопределенный тип";
    }
}
