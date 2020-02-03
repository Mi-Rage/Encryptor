/*
 * Утилита шифрования файлов. Работа в комендной строке с через команды в аргументах.
 * -mode : режим работы
 *      -enc шифруем
 *      -dec расшифровывем
 *  -key : ключ сдвига в таблице ASCII
 *  -in и -out : имена файлов с входящими данными и фалйла для записи результата.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Encryptor {
    public static void main(String[] args) {

        String mode = "enc"; // По умолчанию mode в режиме enc. Даже если параметр не передан.
        int shift = 0;       // По умолчанию ключ сдвига ноль, даже если параметр не передан.
        String buf = "";     // По умолчанию строка шифрования пустая, даже если параметр не передан.
        String filePathIn = "";// По умолчанию путь к файлу чтения пустой.
        String filePathOut = "";// По умолчанию путь к файлу записи пустой.

        if (args.length == 0) {
            System.out.println("Error : Используй аргументы -mode -key -in -out");
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-mode":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -mode");
                    } else if (!args[i + 1].equals("enc") && !args[i + 1].equals("dec")) {
                        System.out.println("Error : Неверный параметр " + args[i + 1] + " для -mode");
                    }
                    mode = args[i + 1];
                    break;
                case "-key":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -key");
                    } else if (!args[i + 1].matches("[0-9]+")) {
                        System.out.println("Error : Ключ сдвига должен состоять только из цифр!");
                    }
                    shift = Integer.parseInt(args[i + 1]);
                    break;
                case "-in":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -in");
                    }
                    filePathIn = args[i + 1];
                    break;
                case "-out":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -out");
                    }
                    filePathOut = args[i + 1];
                    break;
                case "-data":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -data");
                    }
                    buf = args[i + 1];
                    break;
                default:
                    System.out.println("Error : Не выявлено правильных аргументов");
                    break;
            }
        }


        // Полученный аргумент -data переводим в массив символов
        char[] chars = buf.toCharArray();

        boolean isDataAndIn = (!buf.equals("") && !filePathIn.equals("")) || (!buf.equals("") && filePathIn.equals(""));
        boolean isNoDataYesIn = (buf.equals("") && !filePathIn.equals(""));
        boolean isNoFileOut = (!filePathOut.equals(""));

        switch (mode) {
            case "enc":
                if (isDataAndIn) {                                    // Если есть и -data и -in
                    System.out.println(encrypt(chars, shift));      // в приоритете -data
                    break;
                } else if (isNoDataYesIn) {                           // Нет -data есть -in
                    try {
                        buf = readInFile(filePathIn);               // Читаем файл
                    } catch (IOException e) {
                        System.out.println("Error : Cannot read file: " + e.getMessage());
                    }
                    char[] charsFileEnc = buf.toCharArray();
                    if (isNoFileOut) {
                        writeOutFile(filePathOut, encrypt(charsFileEnc, shift)); // Если есть -out то пишем в файл
                    } else {
                        System.out.println(encrypt(charsFileEnc, shift)); // Если нет -out то выводим в консоль
                    }
                }
                break;

            case "dec":
                if (isDataAndIn) {                                    // Если есть и -data и -in
                    System.out.println(decrypt(chars, shift));      // в приоритете -data
                    break;
                } else if (isNoDataYesIn) {                           // Нет -data есть -in
                    try {
                        buf = readInFile(filePathIn);               // Читаем файл
                    } catch (IOException e) {
                        System.out.println("Error : Cannot read file: " + e.getMessage());
                    }
                    char[] charsFileDec = buf.toCharArray();
                    if (isNoFileOut) {
                        writeOutFile(filePathOut, decrypt(charsFileDec, shift)); // Если есть -out то пишем в файл
                    } else {
                        System.out.println(decrypt(charsFileDec, shift)); // Если нет -out то выводим в консоль
                    }
                }
                break;
            default:
                System.out.println("Error : Unknown operation");
                break;
        }
    }

    // Шифруем
    private static String encrypt(char[] chars, int shift) {
        StringBuilder encString = new StringBuilder(); // Защифрованная строка по умолчанию
        char start = 32;    // Первый символ ASCII
        char finish = 127;  // Последний символ ASCII
        int size = 96;      // К-во символов в таблице ASCII

        for (char item : chars) {
            if (item >= start && item <= finish) {
                char shiftItem = (char) (((item - start + shift) % size) + start);
                encString.append(shiftItem);
            } else {
                encString.append(item);
            }
        }
        return encString.toString(); // Возвращаем зашифрованную строку
    }

    // Расшифровываем
    private static String decrypt(char[] chars, int shift) {
        StringBuilder decString = new StringBuilder(); // Расшифрованная строка по умолчанию
        char start = 32;        // Первый символ ASCII
        char finish = 127;      // Последний символ ASCII
        int size = 96;          // К-во символов в таблице ASCII

        for (char item : chars) {
            if (item >= start && item <= finish) {
                char shiftItem = (char) (finish - (finish - item + shift) % size);
                decString.append(shiftItem);
            } else {
                decString.append(item);
            }
        }
        return decString.toString(); // Возвращаем расшифрованную строку
    }

    // Читаем файл
    public static String readInFile(String filePathIn) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePathIn)));
    }

    // Пишем в файл
    public static void writeOutFile(String filePathOut, String decString) {
        File file = new File(filePathOut);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(decString); // Пишем в файл полученную зашифрованную строку
        } catch (IOException e) {
            System.out.printf("Error : An exception occurs %s", e.getMessage());
        }
    }


}
