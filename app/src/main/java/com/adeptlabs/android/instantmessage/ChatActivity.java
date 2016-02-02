package com.adeptlabs.android.instantmessage;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatActivity extends AppCompatActivity {
    //UI Components
    Button sendButton;
    EditText usrText;
    TextView txtWindow;

    NotificationCompat.Builder nBuilder;

    ServerChooseFragment newChooser = new ServerChooseFragment();

    private final String versionID = "0.1";

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String serverIP = newChooser.getServerIP();
    private String message = "";
    private Socket connection;
    private final int port = 8888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendButton = (Button) findViewById(R.id.send_button);
        usrText = (EditText) findViewById(R.id.message_box);
        txtWindow = (TextView) findViewById(R.id.txt_window);
        startClient();
    }

    void notify(String title, String text) {
        nBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.chat_bubble_white)
                .setContentTitle(title)
                .setContentText(text);

        Intent resultIntent = new Intent(this, ChatActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent pendingIntent =
                stackBuilder.
                        getPendingIntent(0,
                                PendingIntent.FLAG_UPDATE_CURRENT);

        nBuilder.setContentIntent(pendingIntent);
        NotificationManager nManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(23, nBuilder.build());
    }

    void startClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        connectServer();
                        startStream();
                        whileChatting();
                    } catch (UnknownHostException u) {
                        u.printStackTrace();
                    } catch (IOException i) {
                        i.printStackTrace();
                    } finally {
                       closeConnection();
                    }
                }
            }
        }).start();
    }

    void makeSnackbar(String message, int length) {
        Snackbar.make(findViewById(R.id.send_button), message, length).show();
    }

    public void sendMessage(View v) {
        String message = "CLIENT: " + usrText.getText().toString();
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            makeSnackbar("Message Sent!", Snackbar.LENGTH_SHORT);
            showMessage("\n" + message);
            usrText.setText("");
        } catch (IOException i) {
            makeSnackbar("CAN'T SEND MESSAGE", Snackbar.LENGTH_SHORT);
        }
    }

    void showDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ERROR")
                .setMessage(errorMessage)
                .setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtWindow.append(message);
            }
        });
    }

    private void connectServer() throws IOException {
        makeSnackbar("Now Connectiing", Snackbar.LENGTH_SHORT);
        connection = new Socket(InetAddress.getByName("139.216.250.16"), 8888);
        makeSnackbar("Connection Established", Snackbar.LENGTH_SHORT);
    }

    private void startStream() throws IOException {
        outputStream = new ObjectOutputStream(connection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(connection.getInputStream());
        makeSnackbar("Streams Set up", Snackbar.LENGTH_SHORT);
    }

    private void whileChatting() throws IOException {
        String message = "You're now connected";

        makeSnackbar(message, Snackbar.LENGTH_SHORT);

        do {
            try {
                message = (String) inputStream.readObject();
                makeSnackbar("Message Received!", Snackbar.LENGTH_SHORT);
                showMessage("\n" + message);
                notify("New Message",message);
            } catch (ClassNotFoundException classNotFound) {
                showMessage("\n Irretrievable Data");
            }
        } while (!message.equals("CLIENT - END"));
    }

    private void closeConnection() {
        makeSnackbar("Connection Closed", Snackbar.LENGTH_SHORT);
        try {
            outputStream.close();
            inputStream.close();
            connection.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.open_dialog) {
            showMessage("\nServer Dialog Opened");
           DialogFragment newFrag = new ServerChooseFragment();
            newFrag.show(getFragmentManager(), "server");
        }

        return super.onOptionsItemSelected(item);
    }
}
