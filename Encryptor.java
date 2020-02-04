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
        int key = 0;       // По умолчанию ключ сдвига ноль, даже если параметр не передан.
        String buf = "";     // По умолчанию строка шифрования пустая, даже если параметр не передан.
        String filePathIn = "";// По умолчанию путь к файлу чтения пустой.
        String filePathOut = "";// По умолчанию путь к файлу записи пустой.
        String alg = "shift";   // По умлляанию алгоритм - сдвиг.

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
                    } else {
                        mode = args[i + 1];
                    }
                    break;
                case "-key":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -key");
                    } else if (!args[i + 1].matches("[0-9]+")) {
                        System.out.println("Error : Ключ сдвига должен состоять только из цифр!");
                    } else {
                        key = Integer.parseInt(args[i + 1]);
                    }
                    break;
                case "-in":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -in");
                    } else {
                        filePathIn = args[i + 1];
                    }
                    break;
                case "-out":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -out");
                    } else {
                        filePathOut = args[i + 1];
                    }
                    break;
                case "-data":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -data");
                    } else {
                        buf = args[i + 1];
                    }
                    break;
                case "-alg":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -alg");
                    } else if (!args[i + 1].equals("shift") && !args[i + 1].equals("unicode")) {
                        System.out.println("Error : Неверный параметр " + args[i + 1] + " для -alg");
                    } else {
                        alg = args[i + 1];
                    }
                    break;
            }
        }

        boolean isDataAndIn = (!buf.equals("") && !filePathIn.equals("")) || (!buf.equals("") && filePathIn.equals(""));
        boolean isNoDataYesIn = (buf.equals("") && !filePathIn.equals(""));
        boolean isNoFileOut = (!filePathOut.equals(""));

        if (isDataAndIn) {                                    // Если есть и -data и -in
            System.out.println(crypto(buf, key, mode, alg));     // в приоритете -data
        } else if (isNoDataYesIn) {                           // Нет -data есть -in
            try {
                buf = readInFile(filePathIn);               // Читаем файл
            } catch (IOException e) {
                System.out.println("Error : Cannot read file: " + e.getMessage());
            }
            if (isNoFileOut) {
                writeOutFile(filePathOut, crypto(buf, key, mode, alg)); // Если есть -out то пишем в файл
            } else {
                System.out.println(crypto(buf, key, mode, alg)); // Если нет -out то выводим в консоль
            }
        }
    }

    /**
     * Метод шифрования/дешифровки
     * @param buf - исходная строка для работы
     * @param shift - ключ сдвига
     * @param mode - режим работы, шифровать или расшифровать
     * @return зашифрованная или расшифрованная в зависимости от mode строка
     */
        private static String crypto (String buf, int shift, String mode, String alg){
            StringBuilder outString = new StringBuilder(); // Выходящая строка по умолчанию
            char start = 32;    // Первый символ ASCII
            char finish = 127;  // Последний символ ASCII
            int size = 96;      // К-во символов в таблице ASCII

            // Полученный аргумент для шифрования переводим в массив символов
            char[] chars = buf.toCharArray();

            for (char item : chars) {
                if(alg.equals("unicode")){
                    if (item >= start && item <= finish) {
                        if (mode.equals("enc")) {
                            char shiftItem = (char) (((item - start + shift) % size) + start);
                            outString.append(shiftItem);
                        } else if (mode.equals("dec")) {
                            char shiftItem = (char) (finish - (finish - item + shift) % size);
                            outString.append(shiftItem);
                        } else {
                            System.out.println("Error : Неверное значение ключа -mode");
                            break;
                        }
                    } else {
                        outString.append(item);
                    }
                } else if (alg.equals("shift")) {
                    if (item >= 65 && item <= 90) {
                        if (mode.equals("enc")) {
                            char shiftItem = (char) (((item - 65 + shift) % 26) + 65);
                            outString.append(shiftItem);
                        } else if (mode.equals("dec")) {
                            char shiftItem = (char) (finish - (90 - item + shift) % 26);
                            outString.append(shiftItem);
                        } else {
                            System.out.println("Error : Неверное значение ключа -mode");
                            break;
                        }
                    } else if (item >= 97 && item <= 122) {
                        if (mode.equals("enc")) {
                            char shiftItem = (char) (((item - 97 + shift) % 26) + 97);
                            outString.append(shiftItem);
                        } else if (mode.equals("dec")) {
                            char shiftItem = (char) (finish - (122 - item + shift) % 26);
                            outString.append(shiftItem);
                        } else {
                            System.out.println("Error : Неверное значение ключа -mode");
                            break;
                        }
                    } else {
                        outString.append(item);
                    }
                }

            }
            return outString.toString(); // Возвращаем зашифрованную строку
        }

//    // Шифруем
//    private static String encrypt(char[] chars, int shift) {
//        StringBuilder encString = new StringBuilder(); // Защифрованная строка по умолчанию
//        char start = 32;    // Первый символ ASCII
//        char finish = 127;  // Последний символ ASCII
//        int size = 96;      // К-во символов в таблице ASCII
//
//        for (char item : chars) {
//            if (item >= start && item <= finish) {
//                char shiftItem = (char) (((item - start + shift) % size) + start);
//                encString.append(shiftItem);
//            } else {
//                encString.append(item);
//            }
//        }
//        return encString.toString(); // Возвращаем зашифрованную строку
//    }
//
//    // Расшифровываем
//    private static String decrypt(char[] chars, int shift) {
//        StringBuilder decString = new StringBuilder(); // Расшифрованная строка по умолчанию
//        char start = 32;        // Первый символ ASCII
//        char finish = 127;      // Последний символ ASCII
//        int size = 96;          // К-во символов в таблице ASCII
//
//        for (char item : chars) {
//            if (item >= start && item <= finish) {
//                char shiftItem = (char) (finish - (finish - item + shift) % size);
//                decString.append(shiftItem);
//            } else {
//                decString.append(item);
//            }
//        }
//        return decString.toString(); // Возвращаем расшифрованную строку
//    }

        // Читаем файл
        public static String readInFile (String filePathIn) throws IOException {
            return new String(Files.readAllBytes(Paths.get(filePathIn)));
        }

        // Пишем в файл
        public static void writeOutFile (String filePathOut, String decString){
            File file = new File(filePathOut);
            try (PrintWriter printWriter = new PrintWriter(file)) {
                printWriter.println(decString); // Пишем в файл полученную зашифрованную строку
            } catch (IOException e) {
                System.out.printf("Error : An exception occurs %s", e.getMessage());
            }
        }


    }

