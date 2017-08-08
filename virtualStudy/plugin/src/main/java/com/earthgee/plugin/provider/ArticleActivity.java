package com.earthgee.plugin.provider;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.earthgee.plugin.R;
import com.earthgee.plugin.contentProvider.Articles;

/**
 * Created by zhaoruixuan on 2017/8/8.
 */
public class ArticleActivity extends Activity implements View.OnClickListener{

    public static final String EDIT_ARTICLE_ACTION="EDIT_ARTICLE_ACTION";
    public static final int MODIFY_ARTICLE=1;
    public static final int DELETE_ARTICLE=2;

    private int articleId=-1;

    private LinearLayout addLayout;
    private LinearLayout editLayout;
    private EditText addTitle;
    private EditText addAbstract;
    private EditText addUrl;
    private EditText editTitle;
    private EditText editAbstract;
    private EditText editUrl;
    private Button addBtn;
    private Button modifyBtn;
    private Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);

        addLayout= (LinearLayout) findViewById(R.id.layout_add);
        addTitle= (EditText) findViewById(R.id.add_title);
        addAbstract= (EditText) findViewById(R.id.add_abstract);
        addUrl= (EditText) findViewById(R.id.add_url);
        addBtn= (Button) findViewById(R.id.btn_add);

        editLayout= (LinearLayout) findViewById(R.id.layout_edit);
        editTitle= (EditText) findViewById(R.id.edit_title);
        editAbstract= (EditText) findViewById(R.id.edit_abstract);
        editUrl= (EditText) findViewById(R.id.edit_url);
        modifyBtn= (Button) findViewById(R.id.btn_modify);
        deleteBtn= (Button) findViewById(R.id.btn_delete);

        Intent intent=getIntent();
        articleId=intent.getIntExtra(Articles.ID,-1);

        if(articleId!=-1){
            addLayout.setVisibility(View.GONE);
            editLayout.setVisibility(View.VISIBLE);

            String title=intent.getStringExtra(Articles.TITLE);
            editTitle.setText(title);

            String abs=intent.getStringExtra(Articles.ABSTRACT);
            editAbstract.setText(abs);

            String url=intent.getStringExtra(Articles.URL);
            editUrl.setText(url);

            modifyBtn.setOnClickListener(this);
            deleteBtn.setOnClickListener(this);
        }else{
            addLayout.setVisibility(View.VISIBLE);
            editLayout.setVisibility(View.GONE);

            addBtn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(addBtn)){
            String title=addTitle.getText().toString();
            String abs=addAbstract.getText().toString();
            String url=addUrl.getText().toString();

            Intent result=new Intent();
            result.putExtra(Articles.TITLE,title);
            result.putExtra(Articles.ABSTRACT,abs);
            result.putExtra(Articles.URL,url);

            setResult(RESULT_OK,result);
            finish();
        }else if(v.equals(modifyBtn)){
            String title=editTitle.getText().toString();
            String abs=editAbstract.getText().toString();
            String url=editUrl.getText().toString();

            Intent result=new Intent();
            result.putExtra(Articles.ID,articleId);
            result.putExtra(Articles.TITLE,title);
            result.putExtra(Articles.ABSTRACT,abs);
            result.putExtra(Articles.URL,url);
            result.putExtra(EDIT_ARTICLE_ACTION,MODIFY_ARTICLE);

            setResult(RESULT_OK,result);
            finish();
        }else if(v.equals(deleteBtn)){
            Intent result=new Intent();
            result.putExtra(Articles.ID,articleId);
            result.putExtra(EDIT_ARTICLE_ACTION,DELETE_ARTICLE);

            setResult(RESULT_OK,result);
            finish();
        }
    }

}
