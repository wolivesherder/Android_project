package com.example.readphotofromalbum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class rongqiActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv1,tv2,tv3;
    private FragmentTransaction fragmentTransaction;
    private detect f1;
    private matchface f2;
    private upload f3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rongqi);
        tv1=findViewById(R.id.jiancebt);
        tv2=findViewById(R.id.shibiebt);
        tv3=findViewById(R.id.shangchuanbt);

        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
        setTab(1);
    }
    private void hidefragment(FragmentTransaction fragment){
        if(f1!=null)
        {
            fragment.hide(f1);
        }
        if(f2!=null)
        {
            fragment.hide(f2);
        }
        if(f3!=null)
        {
            fragment.hide(f3);
        }
    }

    private void setTab(int i){
        fragmentTransaction=getSupportFragmentManager().beginTransaction();
        hidefragment(fragmentTransaction);
        tv1.setSelected(false);
        tv2.setSelected(false);
        tv3.setSelected(false);
        switch (i){
            case 1:
                tv1.setSelected(true);
                if (f1==null){
                    f1=new detect();
                    fragmentTransaction.add( R.id.qitaneirong,f1);
                }else {
                    fragmentTransaction.show(f1);
                }
                break;
            case 2:
                tv2.setSelected(true);
                if (f2==null){
                    f2=new matchface();
                    fragmentTransaction.add( R.id.qitaneirong,f2);
                }else {
                    fragmentTransaction.show(f2);
                }
                break;
            case 3:
                tv3.setSelected(true);
                if (f3==null){
                    f3=new upload();
                    fragmentTransaction.add( R.id.qitaneirong,f3);
                }else {
                    fragmentTransaction.show(f3);
                }
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.jiancebt:
                setTab(1);
                break;
            case R.id.shibiebt:
                setTab(2);
                break;
            case R.id.shangchuanbt:
                setTab(3);
                break;
        }
    }
}
