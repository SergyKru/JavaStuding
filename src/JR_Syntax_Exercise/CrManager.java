package JR_Syntax_Exercise;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;



public class CrManager {
    private static CrManager crManager;
    private CR_STATE state = CR_STATE.NONE;
    private Scanner sc = new Scanner(System.in);
    private String string = null;

    private final boolean READFILE = true;
    private final boolean WRITEFILE = false;
    private Path readFilePath = null;
    private Path writeFilePath = null;
    private Path statFilePath = null;

    private FileReader fileToRead = null;
    private FileReader fileForStat = null;
    private FileWriter fileToWrite = null;
    private boolean stringLikeText = false;


    public static CrManager getInstance(){
        if(crManager==null) crManager = new CrManager();
        return crManager;
    }

    public void configure(){
        clearPaths();
        System.out.println("Выберите режим работы: \nС - кодирование, D - декодирование, B - brute force декодирование, S - статистический анализ, X - выход.");
        string = sc.nextLine();
        setUpState(string);

        if(state != CR_STATE.EXIT && state != CR_STATE.NONE) {
            System.out.println("Введите полный путь к файлам:");
            if (state == CR_STATE.ENCRYPT) System.out.println("Файл для шифрования:");
            else System.out.println("Файл для расшифрования:");

            readFilePath = getFilePath(READFILE);

            if (state != CR_STATE.EXIT)
            {
                if (state == CR_STATE.ENCRYPT) System.out.println("Файл для зашифрованных данных:");
                else System.out.println("Файл для расшифрованных данных:");
                writeFilePath = getFilePath(WRITEFILE);
            }

            if (state == CR_STATE.STAT_A) {
                System.out.println("Файл для статистического анализа:");
                statFilePath = getFilePath(READFILE);
            }
        }

        if (state == CR_STATE.EXIT) sc.close();
    }

    private Path getFilePath(boolean fileReadingMode)  {
        String answer = "";
        boolean checkFileName = false;
        boolean fileExist = false;

        do{
            string = sc.nextLine();
            if(string.isBlank()) continue;
            if ("N".equalsIgnoreCase(string)) state = CR_STATE.EXIT;
            else fileExist = Files.exists(Path.of(string));

            if (fileReadingMode == READFILE && state != CR_STATE.EXIT){
                if (!fileExist)
                    System.out.println("Файл не существует. Введите другой путь. Для выхода наберите \"N\".");
                else checkFileName = true;
            }

            if (fileReadingMode == WRITEFILE && state != CR_STATE.EXIT) {
                if(fileExist){
                    System.out.print("Файл существует. Перезаписать? Да(Y) / Нет(N): ");
                    answer = sc.nextLine();
                    if ("N".equalsIgnoreCase(answer) || answer.isBlank()) System.out.println("Введите другой путь. Для выхода наберите \"N\".");
                    else if ("Y".equalsIgnoreCase(answer)) checkFileName = true;
                }
                if (!fileExist){
                    if (!Files.exists(Path.of(string).getParent()))
                        System.out.println("Путь не существует. Введите другой путь. Для выхода наберите \"N\".");
                    else checkFileName = true;
                }
            }
        } while( !checkFileName && state != CR_STATE.EXIT);

        return Path.of(string);
    }

    private void setUpState(String str) {
        if ("C".equalsIgnoreCase(str)) state = CR_STATE.ENCRYPT;
        else if ("D".equalsIgnoreCase(str)) state = CR_STATE.DECRYPT;
        else if ("B".equalsIgnoreCase(str)) state = CR_STATE.BRUTE_FORCE;
        else if ("S".equalsIgnoreCase(str)) state = CR_STATE.STAT_A;
        else if ("X".equalsIgnoreCase(str)) state = CR_STATE.EXIT;
        else state = CR_STATE.NONE;
    }

    private void clearPaths() {
        readFilePath = null;
        writeFilePath = null;
        statFilePath = null;
    }

    public void work(){

        System.out.print("\r\nРежим работы:  ");
        switch (state) {
            case ENCRYPT :
            {
                openFiles();
                System.out.println("Кодирование..");
                crypt(getKey());
                closeFiles();
            }
            break;
            case DECRYPT:
            {
                openFiles();
                System.out.println("Декодирование..");
                crypt(-getKey());
                closeFiles();
            }
            break;
            case BRUTE_FORCE:
            {
                System.out.println("Brute force..");
                for (int i = -10; i < 11; i++) {
                    if (i==0) continue;
                    System.out.println("Значение ключа: " + i);
                    openFiles();
                    crypt(-i);
                    if(stringLikeText) i=11; else System.out.println("Не подходит\r\n");
                    closeFiles();
                }
                System.out.println("Готово\r\n");
            }
            break;
            case STAT_A:
                System.out.println("Статистический анализ.."); break;
            case EXIT:
                System.out.println("Выход."); break;
        }
    }

    private int getKey() {
        int cryptoKey = 0;
        do{
            System.out.println("Введите ключ: ");
            string = sc.nextLine();
            try{
                cryptoKey = Integer.parseInt(string);
            }
            catch (Exception e)
            {
                System.out.println("Введите число: ");
            }
        }   while (cryptoKey == 0);
        return cryptoKey;
    }

    private void closeFiles() {
        try {
            if(fileToRead != null) fileToRead.close();
            if(fileToWrite != null)fileToWrite.close();
            if(state == CR_STATE.STAT_A && fileForStat != null) fileForStat.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFiles() {
        fileToRead = null;
        try {
            fileToRead = new FileReader(readFilePath.toFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        fileToWrite = null;
        try {
            fileToWrite = new FileWriter(writeFilePath.toFile());
        }
        catch (FileNotFoundException e){
            try {
                Files.createFile(writeFilePath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        fileForStat = null;
        if (state == CR_STATE.STAT_A) try {
            fileForStat = new FileReader(statFilePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void crypt(int crKey) {
        stringLikeText = false;
        char ch=0;
        char tempChar = 0;
        StringBuilder stringBuilder = new StringBuilder();

        try{
            while(fileToRead.ready())
            {
                ch = (char) fileToRead.read();
                stringBuilder.append(ch);
            }

            for (int i = 0; i < stringBuilder.length(); i++) {
                ch = stringBuilder.charAt(i);
                if(state == CR_STATE.DECRYPT || state == CR_STATE.BRUTE_FORCE) tempChar = (char) (ch + crKey);
                else tempChar = ch;

                if(
                        (tempChar == ' ') ||
                                (tempChar == '!') ||
                                (tempChar == '"') ||
                                (tempChar == '\'') ||
                                (tempChar == ',') ||
                                (tempChar == '.') ||
                                (tempChar == ':') ||
                                (tempChar == '?') ||
                                ((tempChar >= 'А')&&(tempChar <= 'Я')) ||
                                ((tempChar >= 'а')&&(tempChar <= 'я'))
                )
                    stringBuilder.setCharAt(i, (char) (ch + crKey));
            }
            int stringLengthToShow = stringBuilder.length();
            if (stringLengthToShow >200 ) stringLengthToShow = 200;
            System.out.println(stringBuilder.toString().substring(0, stringLengthToShow));
            if (stringBuilder.toString().contains(". ") ||
                    stringBuilder.toString().contains(", ") ||
                    stringBuilder.toString().contains("? ") ||
                    stringBuilder.toString().contains("! ")) stringLikeText = true;
            fileToWrite.write(stringBuilder.toString(), 0, stringBuilder.length());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isExit(){
        return state == CR_STATE.EXIT;
    }

}



