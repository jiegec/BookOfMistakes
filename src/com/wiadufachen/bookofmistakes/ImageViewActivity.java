package com.wiadufachen.bookofmistakes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by win7 on 2014-03-30.
 */
public class ImageViewActivity extends Activity {
    public static Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyImageView v = new MyImageView(this);
        v.setImageBitmap(bitmap);
        v.setupView();
        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(v);
        setContentView(layout);

    }
}
