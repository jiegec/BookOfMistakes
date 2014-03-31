package com.wiadufachen.bookofmistakes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.XML;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by win7 on 14-3-26.
 */
public class QuestionActivity extends Activity {
    private Button buttonTitleInsert;
    private Button buttonAnswerInsert;
    private Button buttonSolutionInsert;
    private EditText editTitle;
    private EditText editAnswer;
    private EditText editSolution;
    private Question question;
    private static final int RESULT_TITLE = 1;
    private static final int RESULT_ANSWER = 2;
    private static final int RESULT_SOLUTION = 4;
    private static final int RESULT_CAMERA = 8;
    private static final int RESULT_PICK = 16;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                MainActivity.INSTANCE.update();
        }
        return super.onKeyDown(keyCode, event);
    }


    private final Html.ImageGetter imgGetter = new Html.ImageGetter() {
        @SuppressWarnings("deprecation")
        public Drawable getDrawable(String source) {
            DisplayMetrics  dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            BitmapDrawable d = new BitmapDrawable(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(new File(getFilesDir(), source).getPath()),dm.widthPixels/2,dm.heightPixels/2));
            d.setBounds(0,0,dm.widthPixels/2,dm.heightPixels/2);
            return d;
            /*
            BitmapFactory.Options option = new BitmapFactory.Options();
            // set inJustDecodeBounds to true, allowing the caller to query the bitmap info without having to allocate the
            // memory for its pixels.
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(getFilesDir(), source).getPath(), option);
            DisplayMetrics  dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int ratio = (int)Math.ceil( option.outWidth / (float)dm.widthPixels);
            if(ratio > 1) {
                option.inSampleSize = ratio;
            }
            Bitmap bm = BitmapFactory.decodeFile(new File(getFilesDir(), source).getPath(), option);
            BitmapDrawable d = new BitmapDrawable(bm);
            d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
            return d;*/
            /*
            Bitmap bitmap = BitmapFactory.decodeFile(new File(getFilesDir(), source).getPath());
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            DisplayMetrics  dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int newWidth = dm.widthPixels;
            Matrix matrix = new Matrix();
            matrix.postScale(((float) (width) / (float) (newWidth)), (float) (width) / (float) (newWidth));
            bitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
            @SuppressWarnings("deprecation")
            BitmapDrawable d = new BitmapDrawable(bitmap);*/
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question);
        Integer questionId = getIntent().getIntExtra("question",0);
        QuestionDao d = new QuestionDao(this);
        question = d.find(questionId);
        buttonTitleInsert = (Button) findViewById(R.id.buttonTitleInsert);
        buttonAnswerInsert = (Button) findViewById(R.id.buttonAnswerInsert);
        buttonSolutionInsert = (Button) findViewById(R.id.buttonSolutionInsert);
        editTitle = (EditText) findViewById(R.id.editTitle);
        editAnswer = (EditText) findViewById(R.id.editAnswer);
        editSolution = (EditText) findViewById(R.id.editSolution);
        buttonTitleInsert.setOnClickListener(new MyOnClickListener(RESULT_TITLE,0));
        buttonAnswerInsert.setOnClickListener(new MyOnClickListener(RESULT_ANSWER,0));
        buttonSolutionInsert.setOnClickListener(new MyOnClickListener(RESULT_SOLUTION,0));
        final TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final QuestionDao d = new QuestionDao(QuestionActivity.this);
                question.setTitle(Html.toHtml(editTitle.getText()));
                question.setAnswer(Html.toHtml(editAnswer.getText()));
                question.setSolution(Html.toHtml(editSolution.getText()));
                d.update(question);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        editTitle.setClickable(true);
        editTitle.setMovementMethod(LinkMovementMethod.getInstance());
        editTitle.setText(setImagesClickable(new SpannableStringBuilder(Html.fromHtml(question.getTitle(), imgGetter, null))));
        editAnswer.setClickable(true);
        editAnswer.setMovementMethod(LinkMovementMethod.getInstance());
        editAnswer.setText(setImagesClickable(new SpannableStringBuilder(Html.fromHtml(question.getAnswer(), imgGetter, null))));
        editSolution.setClickable(true);
        editSolution.setMovementMethod(LinkMovementMethod.getInstance());
        editSolution.setText(setImagesClickable(new SpannableStringBuilder(Html.fromHtml(question.getSolution(), imgGetter, null))));
        editTitle.addTextChangedListener(watcher);
        editAnswer.addTextChangedListener(watcher);
        editSolution.addTextChangedListener(watcher);
    }

    private Spanned setImagesClickable(SpannableStringBuilder s) {
        ImageSpan[] image_spans = s.getSpans(0, s.length(), ImageSpan.class);
        for (ImageSpan span : image_spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            ClickableSpan click_span = new MyClickableSpan(span.getSource());
            ClickableSpan[] click_spans = s.getSpans(start, end, ClickableSpan.class);
            if(click_spans.length != 0) {
                for(ClickableSpan c_span : click_spans) {
                    s.removeSpan(c_span);
                }
            }
            s.setSpan(click_span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return  s;
    }

    private class MyClickableSpan extends ClickableSpan {
        private String src;
        MyClickableSpan(String src) {
            this.src = src;
        }
        @Override
        public void onClick(View widget) {
            // TODO Auto-generated method stub
            Bitmap bitmap = BitmapFactory.decodeFile(new File(getFilesDir(), src).getPath());
            ImageViewActivity.bitmap = bitmap;
            Intent intent = new Intent();
            intent.setClass(QuestionActivity.this,ImageViewActivity.class);
            startActivity(intent);
        }
    }

    private class MyOnClickListener implements View.OnClickListener, DialogInterface.OnClickListener {
        private int id;
        private int type;

        MyOnClickListener(int id,int type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            final AlertDialog d = new AlertDialog.Builder(QuestionActivity.this).setTitle(getResources().getString(R.string.text_insert))
                    .setPositiveButton(getResources().getString(R.string.text_take_a_photo), new MyOnClickListener(id,RESULT_CAMERA))
                    .setNeutralButton(getResources().getString(R.string.text_pick_from_gallery), new MyOnClickListener(id, RESULT_PICK)).create();

            d.show();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(type == RESULT_CAMERA) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, id | RESULT_CAMERA);
                }else {
                    Toast.makeText(QuestionActivity.this,getResources().getString(R.string.text_no_camera),Toast.LENGTH_LONG).show();
                }
            }else if (type == RESULT_PICK) {
                Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, id | RESULT_PICK);
            }
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == RESULT_OK) {
                String fileName;
                if((requestCode & RESULT_PICK) != 0) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String path = cursor.getString(columnIndex);
                    cursor.close();
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    FileInputStream i = null;
                    byte buffer[] = new byte[4096];
                    int len;
                    try {
                        i = new FileInputStream(path);
                        while((len = i.read(buffer)) != -1) {
                            md.update(buffer,0,len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            if(i != null) {
                                i.close();
                            }
                        }catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                    fileName = bytesToHex(md.digest()) + ".jpg";
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(new File(getFilesDir(), fileName));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            if(os != null) {
                                os.close();
                            }
                        }catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream o = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, o);
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    ByteArrayInputStream i = new ByteArrayInputStream(o.toByteArray());
                    byte buffer[] = new byte[4096];
                    int len;
                    while((len = i.read(buffer)) != -1) {
                        md.update(buffer,0,len);
                    }
                    fileName = bytesToHex(md.digest()) + ".jpg";
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(new File(getFilesDir(), fileName));
                        o.writeTo(os);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            if(os != null) {
                                os.close();
                            }
                        }catch(IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                EditText editText = null;
                if((requestCode & RESULT_TITLE) != 0) {
                    editText = editTitle;
                }else if((requestCode & RESULT_ANSWER) != 0) {
                    editText = editAnswer;
                }else if((requestCode & RESULT_SOLUTION) != 0){
                    editText = editSolution;
                }
                editText.append(setImagesClickable(new SpannableStringBuilder(Html.fromHtml("\n<img src=\"" + fileName + "\">\n", imgGetter, null))));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
