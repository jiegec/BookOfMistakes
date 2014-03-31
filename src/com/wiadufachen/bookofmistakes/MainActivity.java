package com.wiadufachen.bookofmistakes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.jsoup.Jsoup;
import sun.applet.Main;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by win7 on 14-2-15.
 */
public class MainActivity extends Activity {
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private Button buttonAddCategory;

    private class Holder {
        private Category category;
        private List<Question> questions;

        Holder(Category category, List<Question> questions) {
            this.category = category;
            this.questions = questions;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }
    }

    private ArrayList<Holder> arrayQuestions = new ArrayList<Holder>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private SharedPreferences preferences;
    private final int UPDATE_ADAPTER = 1;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADAPTER:
                    expandableListView.setAdapter(expandableListAdapter);
                    for(int i = 0;i < expandableListAdapter.getGroupCount();i++) {
                        expandableListView.expandGroup(i);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static MainActivity INSTANCE;

    public void update() {
        GetQuestionAsyncTask task = new GetQuestionAsyncTask();
        task.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        INSTANCE = this;
        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        buttonAddCategory = (Button) findViewById(R.id.buttonAddCategory);
        expandableListAdapter = new QuestionExpandableListAdapter();
        expandableListView.setAdapter(expandableListAdapter);
        GetQuestionAsyncTask task = new GetQuestionAsyncTask();
        task.execute();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, QuestionActivity.class);
                readLock.lock();
                Integer questionId = 0;
                try {
                    questionId = arrayQuestions.get(groupPosition).getQuestions().get(childPosition).getId();
                } finally {
                    readLock.unlock();
                }
                intent.putExtra("question", questionId);
                startActivity(intent);
                return false;
            }
        });
        buttonAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText text = new EditText(MainActivity.this);
                final AlertDialog d =
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.text_add_category))
                                .setView(text)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        final CategoryDao d = new CategoryDao(MainActivity.this);
                                        d.save(new Category(0,text.getText().toString()));
                                        GetQuestionAsyncTask task = new GetQuestionAsyncTask();
                                        task.execute();
                                    }
                                })
                                .setNegativeButton("NO",null).create();
                d.show();
            }
        });
    }

    private class QuestionExpandableListAdapter extends BaseExpandableListAdapter {
        QuestionExpandableListAdapter() {

        }

        @Override
        public int getGroupCount() {
            readLock.lock();
            try {
                return arrayQuestions.size();
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            readLock.lock();
            try {
                return arrayQuestions.get(groupPosition).getQuestions().size();
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            readLock.lock();
            try {
                return arrayQuestions.get(groupPosition).getCategory().getName();
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            readLock.lock();
            try {
                return arrayQuestions.get(groupPosition).getQuestions().get(childPosition).getTitle();
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            LinearLayout l =  new LinearLayout(MainActivity.this);
            l.setOrientation(LinearLayout.HORIZONTAL);
            TextView t = new TextView(MainActivity.this);
            String name = "";
            int category = 0;
            readLock.lock();
            try {
                name = arrayQuestions.get(groupPosition).getCategory().getName();
                category = arrayQuestions.get(groupPosition).getCategory().getId();
            } finally {
                readLock.unlock();
            }
            t.setText(name);
            t.setGravity(Gravity.CENTER_VERTICAL);
            t.setPadding(90, 0, 0, 0);
            ImageView b = new ImageView(MainActivity.this);
            b.setImageResource(android.R.drawable.ic_menu_add);
            b.setOnClickListener(new MyOnClickListener(category));
            ImageView delete = new ImageView(MainActivity.this);
            delete.setImageResource(android.R.drawable.ic_menu_delete);
            delete.setOnClickListener(new DeleteCategoryListener(category));
            l.addView(t);
            l.addView(b);
            l.addView(delete);
            return l;
        }
        private class MyOnClickListener implements View.OnClickListener {
            private int category;

            MyOnClickListener(int category) {
                this.category = category;
            }

            @Override
            public void onClick(View v) {
                Question q = new Question(0,"","","",category);
                final QuestionDao d = new QuestionDao(MainActivity.this);
                int questionId = d.save(q);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, QuestionActivity.class);
                intent.putExtra("question", questionId);
                startActivity(intent);
            }
        }

        private class DeleteCategoryListener implements View.OnClickListener {
            private int category;
            DeleteCategoryListener(int category) {
                this.category = category;
            }
            @Override
            public void onClick(View v) {
                final CategoryDao d = new CategoryDao(MainActivity.this);
                d.delete(category);
                final QuestionDao dao = new QuestionDao(MainActivity.this);
                dao.deleteByCategory(category);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View item;
            if (convertView == null) {
                item = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.twolinelistitem, parent, false);
            } else {
                item = convertView;
            }
            String topic = "";
            int questionId = 0;
            String detail = "";
            readLock.lock();
            try {
                topic = Jsoup.parse(arrayQuestions.get(groupPosition).getQuestions().get(childPosition).getTitle()).text();
                detail = Jsoup.parse(arrayQuestions.get(groupPosition).getQuestions().get(childPosition).getSolution()).text();
                questionId = arrayQuestions.get(groupPosition).getQuestions().get(childPosition).getId();
            } finally {
                readLock.unlock();
            }

            ((TextView) item.findViewById(android.R.id.text1)).setText(topic);
            ((TextView) item.findViewById(android.R.id.text2)).setText(detail);
            (item.findViewById(android.R.id.icon)).setOnClickListener(new DeleteQuestionOnClickListener(questionId));
            return item;
        }

        private class DeleteQuestionOnClickListener implements View.OnClickListener {
            private int questionId;

            DeleteQuestionOnClickListener(int questionId) {
                this.questionId = questionId;
            }

            @Override
            public void onClick(View v) {
                final QuestionDao d = new QuestionDao(MainActivity.this);
                d.delete(questionId);
                MainActivity.INSTANCE.update();
            }
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public class GetQuestionAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            CategoryDao c = new CategoryDao(MainActivity.this);
            QuestionDao q = new QuestionDao(MainActivity.this);
            ArrayList<Holder> temp = new ArrayList<Holder>();
            for (Category category : c.getAll()) {
                List<Question> questions = q.findByCategoryId(category.getId());
                Holder h = new Holder(category, questions);
                temp.add(h);
            }
            writeLock.lock();
            try {
                arrayQuestions = temp;
            } finally {
                writeLock.unlock();
            }
            handler.obtainMessage(UPDATE_ADAPTER).sendToTarget();
            return null;
        }
    }

}
