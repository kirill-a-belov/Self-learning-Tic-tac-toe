package com.example.kirill.tictactoe;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //int sim = 0; //Счётчик симуляций
    int i = 0; // Число ходов за кон
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, btn;  //  Ассоциация с кнопками
    Random random = new Random();
    Boolean queue = false; // Кто начинает первым
    StringBuffer neuron = new StringBuffer(); //  Генерируемый нейрон (лог текущей игры
    private NeuronNetwork neuronNetwork; // Класс для работы с нейронной сетью (БД SQLite)
    private SQLiteDatabase sdb;

    public Thread simulate =  new Thread(new Runnable() {//Симулируем нажатия
        @Override
        public void run() {

                try {
                    Thread.sleep(50,0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            for (int i = 0; i < 500; i++) {


                btn = (Button) findViewById(getResources().getIdentifier("button" + (random.nextInt(9) + 1), "id", "com.example.kirill.tictactoe"));

                if ((btn.getText() != "O") && (btn.getText() != "X")) { //  Если да - нажимаем

                    btn.performClick();

                    i = 501;
                }
            }




        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout);
        b1 = (Button) findViewById(R.id.button1);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);
        b4 = (Button) findViewById(R.id.button4);
        b5 = (Button) findViewById(R.id.button5);
        b6 = (Button) findViewById(R.id.button6);
        b7 = (Button) findViewById(R.id.button7);
        b8 = (Button) findViewById(R.id.button8);
        b9 = (Button) findViewById(R.id.button9);
        neuronNetwork = new NeuronNetwork(this, NeuronNetwork.DATABASE_NAME, null, 1);
        sdb = neuronNetwork.getWritableDatabase();

    }

    /*
    Метод - ход компьютера.
     */
    public void move() {
        int resID;

        String query = "SELECT * FROM " + NeuronNetwork.DATABASE_TABLE + " WHERE " + NeuronNetwork.NEURONE_NAME + " LIKE '" + neuron + "%' ORDER BY weight DESC";
        Cursor cursor2 = sdb.rawQuery(query, null);

        //  Вначале проверка есть ли текущий нейрон (стратегия с его началом) в базе, если есть - идём по стратегии,
        if (cursor2.getCount() > 0) {//Если стратегия есть в базе - работаем по ней

            cursor2.moveToFirst();
            String neuronTemp = cursor2.getString(cursor2
                    .getColumnIndex(NeuronNetwork.NEURONE_NAME));
            neuronTemp = neuronTemp.substring(neuron.length());
            neuronTemp = neuronTemp.substring(0, neuronTemp.indexOf('|'));
            neuronTemp = neuronTemp.substring(6, 7);
            resID = getResources().getIdentifier("button" + Integer.parseInt(neuronTemp), "id", "com.example.kirill.tictactoe");

        } else {//Если стратегии в базе нет - работаем по рандому
            resID = getResources().getIdentifier("button" + (random.nextInt(9) + 1), "id", "com.example.kirill.tictactoe");
        }
        cursor2.close();
        btn = (Button) findViewById(resID);
        CharSequence btntxt = btn.getText();

        // Смотрим, можно ли нажать текущую кнопку
        if ((btntxt != "O") && (btntxt != "X")) { //  Если да - нажимаем
            ++i;
            btn.setText("O");
            neuron.append(btn.getResources().getResourceEntryName(btn.getId()) + 'O' + '|');
            winner("");
        } else if (i < 8) { //  Если нет, проверяем есть ли ещё свободные кнопки

            move();
        } else { // Если нет - конец игре, ничья

            gameFinal("Ничья!");

        }
       //if (sim < 50000) {simulate.run();}// Запуск симуляции для наполнения базы
    }

    /*
    Метод отработки нажатия на кнопку пользователем, после себя запускает ход компьютера
     */
    public void btnClk(View view) {
                btn = (Button) view;
        CharSequence btntxt = btn.getText();
        if ((btntxt != "O") && (btntxt != "X")) {

            ++i;

            btn.setText("X");
            neuron.append(btn.getResources().getResourceEntryName(btn.getId()) + 'X' + '|');
            if (!winner("")) {
                move();
            }

        }


    }

    /*
    Конец игры, обнуление всех переменных и вывод сообщения
     */
    public void gameFinal(String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Игра окончена")
                .setMessage(string)
                .setCancelable(false)
                .setNegativeButton("Ещё раз!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                i = 0;
                                b1.setText("");
                                b2.setText(" ");
                                b3.setText("");
                                b4.setText(" ");
                                b5.setText("");
                                b6.setText(" ");
                                b7.setText("");
                                b8.setText("");
                                b9.setText(" ");

                                if (!queue) {
                                    move();
                                    queue = true;
                                } else {
                                    queue = false;
                                }
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
       // alert.getButton(alert.BUTTON_NEGATIVE).performClick(); //Автоматическое закрытие окна окончания раунда

    }

    /*
    Метод вычисляет победителя анализируя возможные выигрышные комбинации
     */
    public Boolean winner(String c) {

        CharSequence win = "";

        if (((b1.getText() == b4.getText()) && (b4.getText() == b5.getText()))) {
            win = b1.getText();
        }
        if (((b2.getText() == b6.getText()) && (b6.getText() == b7.getText()))) {
            win = b6.getText();
        }
        if (((b3.getText() == b8.getText()) && (b8.getText() == b9.getText()))) {
            win = b8.getText();
        }
        if (((b1.getText() == b2.getText()) && (b2.getText() == b3.getText()))) {
            win = b2.getText();
        }
        if (((b4.getText() == b6.getText()) && (b6.getText() == b8.getText()))) {
            win = b6.getText();
        }
        if (((b5.getText() == b7.getText()) && (b7.getText() == b9.getText()))) {
            win = b7.getText();
        }
        if (((b5.getText() == b7.getText()) && (b7.getText() == b9.getText()))) {
            win = b7.getText();
        }
        if (((b1.getText() == b6.getText()) && (b6.getText() == b9.getText()))) {
            win = b6.getText();
        }
        if (((b5.getText() == b6.getText()) && (b6.getText() == b3.getText()))) {
            win = b6.getText();
        }
        //  Выбираем стратегии из базы на основе текущего выигрышного нейрона

        if (win != "") {

            String query = "SELECT * FROM " + NeuronNetwork.DATABASE_TABLE + " WHERE " + NeuronNetwork.NEURONE_NAME + " = '" + neuron + "'";
            Cursor cursor2 = sdb.rawQuery(query, null);
            if (win == "O") {
                int weight = 0;

                if (cursor2.getCount() > 0) {

                    cursor2.moveToFirst();
                    weight = cursor2.getInt(cursor2
                            .getColumnIndex(NeuronNetwork.NEURONE_WEIGHT));
                }
                cursor2.close();

                if (weight == 0) {// В случае если такой стратегии ещё нет в базе, дописываем
                    ContentValues values = new ContentValues();
                    values.put(NeuronNetwork.NEURONE_NAME, neuron.toString());
                    values.put(NeuronNetwork.NEURONE_WEIGHT, "1");
                    sdb.insert(NeuronNetwork.DATABASE_TABLE, null, values);


                } else {// Если такая стратегия уже есть - увеличиваем её вес
                    ContentValues values = new ContentValues();
                    values.put(NeuronNetwork.NEURONE_WEIGHT, weight + 1);
                    sdb.update(NeuronNetwork.DATABASE_TABLE, values,
                            NeuronNetwork.NEURONE_NAME + "= ?", new String[]{neuron.toString()});


                }
            } else {

                //  Инвертируем стратегию и тоже пишем в базу - обучение на действиях "врага"
                int weight = 0;
                String c1, c2, c3, c4;
                c1 = neuron.toString().replace('X', '*');
                c2 = c1.replace('O', '$');
                c3 = c2.replace('*', 'O');
                c4 = c3.replace('$', 'X');

                query = "SELECT * FROM " + NeuronNetwork.DATABASE_TABLE + " WHERE " + NeuronNetwork.NEURONE_NAME + " = '" + c4 + "'";
                cursor2 = sdb.rawQuery(query, null);
                if (cursor2.getCount() > 0) {

                    cursor2.moveToFirst();
                    weight = cursor2.getInt(cursor2
                            .getColumnIndex(NeuronNetwork.NEURONE_WEIGHT));
                }
                cursor2.close();
                if (weight == 0) {// В случае если такой стратегии ещё нет в базе, дописываем
                    ContentValues values = new ContentValues();
                    values.put(NeuronNetwork.NEURONE_NAME, c4.toString());
                    values.put(NeuronNetwork.NEURONE_WEIGHT, "1");
                    sdb.insert(NeuronNetwork.DATABASE_TABLE, null, values);


                } else {// Если такая стратегия уже есть - увеличиваем её вес
                    ContentValues values = new ContentValues();
                    values.put(NeuronNetwork.NEURONE_WEIGHT, weight + 1);
                    sdb.update(NeuronNetwork.DATABASE_TABLE, values,
                            NeuronNetwork.NEURONE_NAME + "= ?", new String[]{c4.toString()});


                }
            }
            System.out.println(neuron);
           // sim++;
            neuron.setLength(0);
            gameFinal(("Победил " + win + " !"));
            return true;
        } else {
            return false;
        }


    }


}