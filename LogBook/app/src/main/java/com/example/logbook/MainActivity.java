package com.example.logbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button Next, Back, Add, Reset;
    ArrayList<String> List = new ArrayList<>();
    TextView Mess;
    EditText Input;
    ImageView Image;
    private int currentIndex = 0;
    private static final String FILE_NAME = "URLs.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findAllElements();
        try {
            loadURLs();
        } catch (IOException e) {
            e.printStackTrace();
            displayMessage("Read file error, file = " + FILE_NAME);
        }
        setImage();
        setNext();
        setBack();
        setAdd();
        setRest();
    }

    private void setAnimationLeft() {
        Animation in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Image.setAnimation(in_left);
    }

    private void setAnimationRight() {
        Animation on_Right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        Image.setAnimation(on_Right);
    }

    private void setRest() {
        Reset.setOnClickListener(v -> {
            List.clear();
            Image.setImageResource(0);
            removeFile();
            currentIndex = 0;
            displayMessage("Data reset completed");
        });
    }

    private void removeFile() {
        getApplicationContext().deleteFile(FILE_NAME);
    }

    private void setAdd() {
        Add.setOnClickListener(v -> {
            String URL = Input.getText().toString().trim();
            List.add(URL);
            try {
                saveFile(URL);
                displayMessage("URL added successfully");
            } catch (IOException e) {
                e.printStackTrace();
                displayMessage("Save file error");
            }
        });
    }

    private void saveFile(String url) throws IOException {
        FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(FILE_NAME, Context.MODE_APPEND);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        bufferedWriter.write(url);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        bufferedWriter.close();
        outputStreamWriter.close();
    }

    private void setBack() {
        Back.setOnClickListener(v -> {
            currentIndex--;
            setImage();
            setAnimationRight();
            displayMessage(List.get(currentIndex));
        });
    }

    private void displayMessage(String message) {
        Mess.setText(message);
    }

    private void loadURLs() throws IOException {
        FileInputStream fileInputStream = getApplicationContext().openFileInput(FILE_NAME);
        if (fileInputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String lineData =
                    bufferedReader.readLine();
            while (lineData != null) {
                List.add(lineData);
                lineData = bufferedReader.readLine();
            }
        }
    }

    private void setNext() {
        Next.setOnClickListener(v -> {
            currentIndex++;
            setImage();
            setAnimationLeft();
            displayMessage(List.get(currentIndex));
        });
    }

    private void setImage() {
        int size = List.size();
        if (currentIndex >= size) {
            currentIndex = 0;
        } else if (currentIndex < 0) {
            currentIndex = size - 1;
        }
        if (size > 0) {
            Glide.with(this)
                    .load(List.get(currentIndex))
                    .into(Image);
        }
    }

    private void findAllElements() {
        Add = findViewById(R.id.btnAdd);
        Back = findViewById(R.id.btnBack);
        Reset = findViewById(R.id.btnReset);
        Next = findViewById(R.id.btnNext);
        Input = findViewById(R.id.txtLink);
        Mess = findViewById(R.id.txtMess);
        Image = findViewById(R.id.ImageView);
    }
}