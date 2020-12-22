package my.pyrohivskyi;

import com.google.protobuf.CodedInputStream;

import java.io.IOException;
import java.io.RandomAccessFile;

public class WorkWithFile {

    private String path;

    // Экземпляр класса который обеспечит возможность
    // работать с файлом
    private RandomAccessFile file;

    // говорим конструктору проинициализировать путь к файлу
    public WorkWithFile(String path) {
        this.path = path;
    }

    // метод демонстрирует переход на указанный символ
    public long goTo(int num) throws IOException {
        // инициализируем класс RandomAccessFile
        // в параметры передаем путь к файлу
        // и модификатор который говорит, что файл откроется только для чтения
        file = new RandomAccessFile(path, "r");

        // переходим на num символ
        file.seek(num);

        // получаем текущее состояние курсора в файле
        long pointer = file.getFilePointer();
        file.close();

        return pointer;
    }

    // этот метод читает файл и выводит его содержимое
    public String read() throws IOException {
        file = new RandomAccessFile(path, "r");
        String res = "";
        int b = file.read();
        // побитово читаем символы и плюсуем их в строку
        while(b != -1){
            res = res + (char)b;
            b = file.read();
        }
        file.close();

        return res;
    }

    // читаем файл с определенного символа
    public String readFrom(int numberSymbol) throws IOException {
        // открываем файл для чтения
        file = new RandomAccessFile(path, "r");
        String res = "";

        // ставим указатель на нужный вам символ
        file.seek(numberSymbol);
        int b = file.read();

        // побитово читаем и добавляем символы в строку
        while(b != -1){
            res = res + (char)b;

            b = file.read();
        }
        file.close();

        return res;
    }

    // запись в файл
    public void write(String st) throws IOException {
        // открываем файл для записи
        // для этого указываем модификатор rw (read & write)
        // что позволит открыть файл и записать его
        file = new RandomAccessFile(path, "rw");

        // записываем строку переведенную в биты
        byte[] b = st.getBytes();
        file.write(b);

        // закрываем файл, после чего данные записываемые данные попадут в файл
        file.close();
    }
}

