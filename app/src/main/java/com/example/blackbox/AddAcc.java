package com.example.blackbox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class AddAcc extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // user input
    private boolean validate;
    EditText edt_txt_pass, edt_txt_acc, edt_txt_title;
    TextInputLayout input_layout_title, input_layout_acc, input_layout_pass;
    String title;
    // img index for icons
    int img_index;

    // button
    Button btn_save;

    // dropdown spinner
    Spinner spin_category;
    ArrayList<SpinModel> spinList;

    //
    private final String mail = "E-Mail";
    private final String other = "Other";
    private final String debit_card = "Debit Card";
    private final String phone = "Phone";
    private final String computer = "Computer";
    private final String social_media = "Social Media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_acc);

        edt_txt_acc = findViewById(R.id.edt_txt_acc);
        edt_txt_pass = findViewById(R.id.edt_txt_pass);
        edt_txt_title = findViewById(R.id.edt_txt_title);
        input_layout_acc = findViewById(R.id.input_layout_acc);
        input_layout_pass = findViewById(R.id.input_layout_pass);
        input_layout_title = findViewById(R.id.input_layout_title);


        // save into database
        btn_save = findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    DataBaseHelper db = new DataBaseHelper(AddAcc.this);

                    CardModel cardModel = new CardModel();
                    cardModel.setAcc(edt_txt_acc.getText().toString());
                    cardModel.setPass(edt_txt_pass.getText().toString());
                    cardModel.setImg(img_index);
                    if(img_index == 4)
                        title = edt_txt_title.getText().toString();
                    cardModel.setTitle(title);
                    db.addAcc(cardModel);
                }
            }
        });

        // spinner1
        spin_category = findViewById(R.id.spin_category);
        spinList = getSpinList();
        SpinAdapter spinAdapter = new SpinAdapter(this, spinList);
        if (spin_category != null) {
            spin_category.setAdapter(spinAdapter);
            spin_category.setOnItemSelectedListener(this);
        }

        edt_txt_acc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(edt_txt_acc.getText().length() >= 1)
                    input_layout_acc.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(edt_txt_acc.getText().length() >= 1)
                    input_layout_acc.setError(null);
                else
                    input_layout_acc.setError("Enter Your Account Name");
            }
        });

        edt_txt_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(edt_txt_pass.getText().length() >= 1)
                    input_layout_pass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(edt_txt_pass.getText().length() >= 1)
                    input_layout_pass.setError(null);
                else
                    input_layout_pass.setError("Enter Your Password");
            }
        });
    }

    // if empty error
    private boolean validateFields() {
        int yourDesiredLength = 1;
        if(edt_txt_pass.getText().length() < yourDesiredLength || edt_txt_acc.getText().length() < yourDesiredLength)
            validate = false;
        else
            validate = true;
        if (edt_txt_acc.getText().length() < yourDesiredLength) {
            input_layout_acc.setError("Enter Your Account Name");
        }
        if (edt_txt_pass.getText().length() < yourDesiredLength) {
            input_layout_pass.setError("Enter Your Password");
        }
        return validate;
    }

    private ArrayList<SpinModel> getSpinList() {
        spinList = new ArrayList<>();
        spinList.add(new SpinModel(mail, R.drawable.e_mail));
        spinList.add(new SpinModel(social_media, R.drawable.social_media));
        spinList.add(new SpinModel(computer, R.drawable.computer));
        spinList.add(new SpinModel(phone, R.drawable.phone));
        spinList.add(new SpinModel(other, R.drawable.title));
        return spinList;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SpinModel item = (SpinModel) adapterView.getSelectedItem();
        getIndex(item.getCategory());
        Toast.makeText(this, item.getCategory(), Toast.LENGTH_SHORT).show();
        if(img_index == 4) {
            edt_txt_title.setEnabled(true);
            //text_input_layout_title.setFocusable(true);
        }
        else{
            edt_txt_title.setEnabled(false);
            //text_input_layout_title.setFocusable(false);
        }
    }

    // passing the arbitrary index instead of storing the img as blob
    private void getIndex(String category) {
        switch (category) {
            case mail:
                img_index = 0;
                title = "E-Mail";
                break;
            case social_media:
                img_index = 1;
                title = "Social Media";
                break;
            case computer:
                img_index = 2;
                title = "Computer";
                break;
            case phone:
                img_index = 3;
                title = "Phone";
                break;
            case other:
                img_index = 4;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}