package e1;

import java.util.Scanner;

/**
 *   Интерфейс командной строки
 * @author aabyodj
 */
public class CLI {    

    private Scanner in;

    public CLI() {
        in = new Scanner(System.in);
    }
    
    /**
     *   Меню: выбор одного из вариантов
     * @param options список вариантов
     * @return индекс выбранного варианта
     */
    public int getChoice(Option[] options) {
        if (options.length < 1) return -1;
        int result;
        while (true) {
            System.out.println("Выберите:\n---------");
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + " - " + options[i].text);
            }
            result = in.nextInt() - 1;
            if (result < 0 || result >= options.length) {
                System.out.println("Нет такого варианта.");
            } else break;
        }
        System.out.println("Выбрано: " + options[result].text);
        options[result].action.run();
        return result;
    }
    
    /**
     * Ввод значения типа int
     * @param hint текстовая подсказка
     * @return введённое значение
     */
    public int getInt(String hint) {
        if (hint != null) {
            if (!hint.isBlank()) System.out.print(hint + ": ");
        }
        return in.nextInt();
    }
    
    /** Ожидание нажатия ENTER */
    public void waitForEnter() {
        System.out.print("Нажмите ВВОД для продолжения...");
        in.nextLine();
        //if (!in.hasNextLine()) 
        //System.out.println(in.hasNextLine());
        while (!in.hasNextLine());
        //while (!in.hasNextLine()) in.nextLine();;
    }
    
    /**
     * Пункт меню для int getChoice(Option[] list)
     */
    public static class Option {
        final String text;
        final Runnable action;

        /**
         * Создать пункт меню
         * @param text подсказка для пользователя
         * @param action действие
         */
        public Option(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }
}
