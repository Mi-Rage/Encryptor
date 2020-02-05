import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Enygma {
    public static void main(String[] args) {
        try (Crypto crypto = new Crypto(args)) {
            crypto.start();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class Crypto implements Closeable {

    private String mode = "enc";                     // По умолчанию mode в режиме enc. Даже если параметр не передан.
    private PrintWriter output = new PrintWriter(System.out); // По умолчанию вывод производится в консоль
    private int key = 0;                             // По умолчанию ключ сдвига ноль, даже если параметр не передан.
    private String bufferOut = "";                   // По умолчанию строка шифрования пустая, даже если параметр не передан.
    private String alg = "shift";                    // По умолчанию алгоритм - сдвиг.
    private boolean closed = false;

    public Crypto(String[] args) {
        // Если нет аргументов - выведем хелп
        if (args.length == 0) {
            System.out.println("Error : Используй аргументы -mode -key -in -out -alg -data");
            System.out.println("-mode enc (шифровать) или dec (расшифровать)");
            System.out.println("-key числовое значение сдвига шифра");
            System.out.println("-in файл *.txt с входящими данными");
            System.out.println("-out файл *.txt для полученных данных");
            System.out.println("-alg unicode для шифра по всей таблице ASCII или shift для шифра только по алфавиту");
            System.out.println("-data строка символов для обработки, если нет файлов");
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-mode":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -mode");
                    } else if (!args[i + 1].equals("enc") && !args[i + 1].equals("dec")) {
                        System.out.println("Error : Неверный параметр " + args[i + 1] + " для -mode");
                    } else {
                        this.mode = args[i + 1];
                    }
                    break;
                case "-key":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -key");
                    } else if (!args[i + 1].matches("[0-9]+")) {
                        System.out.println("Error : Ключ сдвига должен состоять только из цифр!");
                    } else {
                        this.key = Integer.parseInt(args[i + 1]);
                    }
                    break;
                case "-in": //  Если есть -in сразу читаем файл в строку
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -in");
                    } else {
                        try {
                            this.bufferOut = new String(Files.readAllBytes(Paths.get(args[i + 1])));
                        } catch (IOException e) {
                            System.out.println("Error: Проблема с файлом " + e.getMessage());
                        }
                    }
                    break;
                case "-out":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -out");
                    } else {
                        try {
                            this.output = new PrintWriter(new File(args[i + 1]));
                        } catch (IOException e) {
                            System.out.println("Error: Проблема с файлом " + e.getMessage());
                        }
                    }
                    break;
                case "-alg":
                    if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                        System.out.println("Error : Нет значения для ключа -alg");
                    } else if (!args[i + 1].equals("shift") && !args[i + 1].equals("unicode")) {
                        System.out.println("Error : Неверный параметр " + args[i + 1] + " для -alg");
                    } else {
                        this.alg = args[i + 1];
                    }
                    break;
            }
        }
        // Если есть -data то читаем из неё, игнорируя наличие файлов -in
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-data")) {
                if (i + 1 == args.length || args[i + 1].startsWith("-")) {
                    System.out.println("Error: Проблема с параметром -data");
                } else {
                    this.bufferOut = args[i + 1];
                }
            }
        }

    }

    public String enygma() {

        StringBuilder outString = new StringBuilder(); // Выходящая строка по умолчанию

        char startUnicode = 32;    // Первый символ ASCII
        char finishUnicode = 127;  // Последний символ ASCII
        int sizeUnicode = 96;      // К-во символов в таблице ASCII

        char startSiftBig = 65;    // Первый заглавный символ для shift
        char finishShiftBig = 90;  // Последний заглавный символ для shift
        int sizeShift = 26;      // К-во символов в алфавите для shift

        char startSiftSmall = 97;    // Первый строчный символ для shift
        char finishShiftSmall = 122;  // Последний строчный символ для shift


        // Полученный аргумент для шифрования переводим в массив символов
        char[] chars = this.bufferOut.toCharArray();

        for (char item : chars) {
            if (this.alg.equals("unicode")) {
                if (item >= startUnicode && item <= finishUnicode) {
                    if (this.mode.equals("enc")) {
                        char shiftItem = (char) (((item - startUnicode + this.key) % sizeUnicode) + startUnicode);
                        outString.append(shiftItem);
                    } else if (this.mode.equals("dec")) {
                        char shiftItem = (char) (finishUnicode - (finishUnicode - item + this.key) % sizeUnicode);
                        outString.append(shiftItem);
                    } else {
                        System.out.println("Error : Неверное значение ключа -mode");
                        break;
                    }
                } else {
                    outString.append(item);
                }
            } else if (this.alg.equals("shift")) {
                if (item >= startSiftBig && item <= finishShiftBig) {
                    if (this.mode.equals("enc")) {
                        char shiftItem = (char) (((item - startSiftBig + this.key) % sizeShift) + startSiftBig);
                        outString.append(shiftItem);
                    } else if (this.mode.equals("dec")) {
                        char shiftItem = (char) (finishShiftBig - (finishShiftBig - item + this.key) % sizeShift);
                        outString.append(shiftItem);
                    } else {
                        System.out.println("Error : Неверное значение ключа -mode");
                        break;
                    }
                } else if (item >= startSiftSmall && item <= finishShiftSmall) {
                    if (this.mode.equals("enc")) {
                        char shiftItem = (char) (((item - startSiftSmall + this.key) % sizeShift) + startSiftSmall);
                        outString.append(shiftItem);
                    } else if (this.mode.equals("dec")) {
                        char shiftItem = (char) (finishShiftSmall - (finishShiftSmall - item + this.key) % sizeShift);
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

    private boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() throws IOException {
        this.output.close();
        this.closed = true;
    }

    public void start() {
        if (this.isClosed()) {
            return;
        }
        this.output.println(enygma());

    }
}