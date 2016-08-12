package es.maps.scoreboard.app;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Stack;
//import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class ScoreBoardActivity extends Activity {

    private int[] totalScore = { 0, 0 };
    private int[][] partialScore = { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 },
            { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } };
    private int[][] shotsIn = { { 0, 0, 0 }, { 0, 0, 0 } };
    private TextView totalTextView[] = new TextView[2];
    private TextView partialTextView[] = new TextView[2];
    private TextView shotsInTextView[][] = new TextView[2][3];
    private TextView currentPeriodTextView;
    private Stack<Basket> stackBasket = new Stack<Basket>();
    private Stack<Basket> stackRedo = new Stack<Basket>();
    private int currentPeriod = 0;
    private int numPeriods = 9;
    static public enum MessageIds {MSG_RESTORE}
    static public MessageIds[] mi = MessageIds.values();

    Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (mi[msg.what])
            {
                case MSG_RESTORE:
                    printCounters();
                    break;
                default:
                    break;
            }
//		        Toast.makeText(ScoreBoardActivity.this, "Saved", Toast.LENGTH_SHORT).show();
        }
    };

    //AtomicBoolean isRunning=new AtomicBoolean(false);

    public void onClickPoints(View v) {
        int t = Integer.parseInt(v.getTag().toString());
        int team = t / 10;
        int points = t % 10;
        totalScore[team] += points;
        totalTextView[team].setText(Integer.toString(totalScore[team]));
        partialScore[currentPeriod][team] += points;
        partialTextView[team].setText(Integer
                .toString(partialScore[currentPeriod][team]));
        shotsIn[team][points - 1]++;
        shotsInTextView[team][points - 1].setText(Integer
                .toString(shotsIn[team][points - 1]));
        stackBasket.push(new Basket((team == 0) ? Basket.teams.HOME
                : Basket.teams.VISITOR, currentPeriod, 0, points,
                totalScore[0], totalScore[1]));
        saveStatus();
    }

    public void onClickUndo(View v) {
        if (stackBasket.empty())
            return;
        Basket basket = stackBasket.pop();
        stackRedo.push(basket);
        int team = basket.getTeam().ordinal();
        int points = basket.getPoints();
        int period = basket.getPeriod();
        totalScore[team] -= points;
        totalTextView[team].setText(Integer.toString(totalScore[team]));
        partialScore[period][team] -= points;
        partialTextView[team].setText(Integer
                .toString(partialScore[currentPeriod][team]));
        shotsIn[team][points - 1]--;
        shotsInTextView[team][points - 1].setText(Integer
                .toString(shotsIn[team][points - 1]));
        saveStatus();
    }

    public void onClickRedo(View v) {
        if (stackRedo.empty())
            return;
        Basket basket = stackRedo.pop();
        stackBasket.push(basket);
        int team = basket.getTeam().ordinal();
        int points = basket.getPoints();
        int period = basket.getPeriod();
        totalScore[team] += points;
        totalTextView[team].setText(Integer.toString(totalScore[team]));
        partialScore[period][team] += points;
        partialTextView[team].setText(Integer
                .toString(partialScore[currentPeriod][team]));
        shotsIn[team][points - 1]++;
        shotsInTextView[team][points - 1].setText(Integer
                .toString(shotsIn[team][points - 1]));
        saveStatus();
    }

   public void onClickReset(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(ScoreBoardActivity.this).create();
        alertDialog.setTitle("@string/reset");
        alertDialog.setMessage("@string/sure");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "@string/yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stackBasket.clear();
                resetCounters();
                printCounters();
                new File(getExternalFilesDir(null), "match.bin").delete();
                saveStatus();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"@string/no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alertDialog.show();
    }

    public void onClickExit(View v) {
        finish();
    }

    public void onClickPeriodDown(View v) {
        if (currentPeriod > 0)
        {
            currentPeriod--;
            partialTextView[0].setText(Integer
                    .toString(partialScore[currentPeriod][0]));
            partialTextView[1].setText(Integer
                    .toString(partialScore[currentPeriod][1]));
            currentPeriodTextView.setText(Integer.toString(currentPeriod + 1));
        }
    }

    public void onClickPeriodUp(View v) {
        if (currentPeriod < numPeriods - 1)
        {
            currentPeriod++;
            partialTextView[0].setText(Integer
                    .toString(partialScore[currentPeriod][0]));
            partialTextView[1].setText(Integer
                    .toString(partialScore[currentPeriod][1]));
            currentPeriodTextView.setText(Integer.toString(currentPeriod + 1));
        }
    }


    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        totalTextView[0] = (TextView) findViewById(R.id.homeTotalTextView);
        totalTextView[1] = (TextView) findViewById(R.id.visitorTotalTextView);

        partialTextView[0] = (TextView) findViewById(R.id.homePartialTextView);
        partialTextView[1] = (TextView) findViewById(R.id.visitorPartialTextView);

        shotsInTextView[0][0] = (TextView) findViewById(R.id.homeShotsIn1TextView);
        shotsInTextView[0][1] = (TextView) findViewById(R.id.homeShotsIn2TextView);
        shotsInTextView[0][2] = (TextView) findViewById(R.id.homeShotsIn3TextView);
        shotsInTextView[1][0] = (TextView) findViewById(R.id.visitorShotsIn1TextView);
        shotsInTextView[1][1] = (TextView) findViewById(R.id.visitorShotsIn2TextView);
        shotsInTextView[1][2] = (TextView) findViewById(R.id.visitorShotsIn3TextView);

        currentPeriodTextView = (TextView) findViewById(R.id.currentPeriodTextView);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN	| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            Toast.makeText(this, "@string/noMediaString", Toast.LENGTH_SHORT).show();
        }
    }

    public void onResume() {
        super.onResume();

        resetCounters();
        readStatus();
    }

    private void resetCounters() {
        for (int t = 0; t < 2; t++) {
            totalScore[t] = 0;
            for (int p = 0; p < 3; p++) {
                shotsIn[t][p] = 0;
            }

            for (int p = 0; p < numPeriods; p++) {
                partialScore[p][t] = 0;
            }
        }

        currentPeriod = 0;
    }

    private void printCounters() {
        for (int t = 0; t < 2; t++) {
            totalTextView[t].setText(Integer.toString(totalScore[t]));
            partialTextView[t].setText(Integer.toString(partialScore[currentPeriod][t]));

            for (int p = 0; p < 3; p++) {
                shotsInTextView[t][p].setText(Integer.toString(shotsIn[t][p]));
            }
        }
        currentPeriodTextView.setText(Integer.toString(currentPeriod + 1));
    }

    private void saveStatus() {
        Thread background=new Thread(new Runnable() {
            public void run() {
                try {
                    ObjectOutputStream  outputStream = new ObjectOutputStream(new FileOutputStream(new File(getExternalFilesDir(null), "match.bin")));
                    outputStream.writeInt(currentPeriod);
                    outputStream.writeObject(totalScore);
                    outputStream.writeObject(partialScore);
                    outputStream.writeObject(shotsIn);
//                    Basket b = new Basket();
                    for (Basket b : stackBasket)
                    {
                        // b = stackBasket.get(i);
                        outputStream.writeObject(b);
                    }
                    outputStream.close();
                }
                catch (Throwable t) {
                }
            }

        });
        background.start();
    }

    private void readStatus() {
        Thread background=new Thread(new Runnable() {
            public void run() {
                try {
                    ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(new File(getExternalFilesDir(null), "match.bin")));
                    try {

                        currentPeriod = inputStream.readInt();
                        totalScore = (int [])inputStream.readObject();
                        partialScore = (int[][]) inputStream.readObject();
                        shotsIn = (int[][]) inputStream.readObject();
                        Basket b;
                        while ((b = (Basket)inputStream.readObject()) != null)
                        {
                            stackBasket.push(b);
                        }
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
//						e.printStackTrace();
                    }
                    catch (EOFException e) {
                        inputStream.close();
                        handler.sendMessage(handler.obtainMessage(MessageIds.MSG_RESTORE.ordinal()));
                        // TODO Auto-generated catch block
//						e.printStackTrace();
                    }
                }
                catch (FileNotFoundException e) {
                    handler.sendMessage(handler.obtainMessage(MessageIds.MSG_RESTORE.ordinal()));
                    // TODO Auto-generated catch block
//					e.printStackTrace();
                }
                catch (Throwable t) {
//                    return;
                }
            }

        });
        background.start();
    }


}