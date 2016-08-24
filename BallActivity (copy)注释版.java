package com.thundersoft.b.reboundball;

/**
 * Created by ts on 16-8-2.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Random;

public class BallActivity extends Activity {
    private boolean isExit=false;
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit=false;
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(new BallSurfaceView(this));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击的是返回键
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //如果isExit标记为false，提示用户再次按键
            if(!isExit){
                isExit=true;
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //如果用户没有在2秒内再次按返回键的话，就发送消息标记用户为不退出状态
                mHandler.sendEmptyMessageDelayed(0, 3000);
            }
            //如果isExit标记为true，退出程序
            else{
                //退出程序
                this.finish();
                System.exit(0);
            }
        }
        return false;


    }

	//定义类继承surfaceview ，在surfaceview实例上面绘图
    class BallSurfaceView extends SurfaceView implements Callback, Runnable {


        //游戏是否结束
        boolean gamerunning=true;
        boolean restart=false; //是否是再次启动
        int xdire = 1; //x轴方向
        int ydire = 1; //y轴方向
        float xspeed;  //速度绝对值
        float yspeed;  //speed

        boolean chufa = false;
        boolean touch=true; //判断触摸
        float xvelocity; //速度
        float yvelocity; //速度
        private VelocityTracker vt = null;
        private int screenW;        //屏幕宽度
        private int screenH;        //屏幕高度
        private Paint paint;        //定义画笔
        private float cx = 50;      //圆点默认X坐标，小球X的坐标
        private float cy = 50;      //圆点默认Y坐标，小球Y的坐标
        private int radius = 40;   //小球的半径
        //定义颜色数组
        private int colorArray[] = {Color.BLACK, Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED};
        private int paintColor = colorArray[0]; //定义画笔默认颜色
        private Canvas canvas = null; //定义画布
        private Thread th = null;     //定义线程
        private SurfaceHolder sfh = null;
        RectF rect ;//绘制挡板的长方体
        float leftx; //挡板的左上
        float lefty;
        float rightx;//挡板的右下
        float righty;
	//处理重新开始运行
        Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==1){

                    th = new Thread(BallSurfaceView.this);
                    cx = screenW / 2;
                    cy = screenH - radius;
                    gamerunning=true;
                    touch=true;
                    xspeed=0;
                    yspeed=0;
                    th.start();

                }
            }
        };


	
        public BallSurfaceView(Context context) {
            super(context);
                
                /*screenW = getWidth();
                screenH = getHeight();*/

            //初始化画笔
          /*  Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            */

            //rect = new RectF(50, 50, 200, 200);
            initPaint();
            sfh = getHolder();
            sfh.addCallback(this);
            th = new Thread(this);//初始化线程
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //获取屏幕宽度
            screenW = getWidth();
            //获取屏幕高度
            screenH = getHeight();
            cx = screenW / 2;
            cy = screenH - radius;//初始化时的位置
            leftx=screenW/4;//挡板的初始化
            lefty=4*radius;
            rightx=screenW*3/4+1;
            righty=5*radius+1;
            rect=new RectF(leftx,lefty,rightx,righty);
            /**
	     *坐标的说明
             * 左上坐标 screenW/4,2*radius
             * 左下     screenW/4,3*radius
             * 右上坐标 screenW*3/4+1,2*radius
             * 右下坐标 screenW*3/4+1,3*radius
             *
             *
             */
            //启动绘图线程
            th.start();
        }

        private void initPaint() {
            paint = new Paint();
            //设置消除锯齿
            paint.setAntiAlias(true);
		//更加平滑（好像没什么用）
            paint.setDither(true);
            //设置画笔颜色
            paint.setColor(paintColor);
        }

	//绘图线程
        @Override
        public void run() {

            while (gamerunning) {
                try {
                    if (chufa) {
                        changeXY();
                        revise();
                    }
                    myDraw();
                    Thread.sleep(100);
                    nextXY();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

       
        //实现绘图操作
        protected void myDraw() {
            //获取canvas实例
            canvas = sfh.lockCanvas();
            //将屏幕设置为白色
            canvas.drawColor(Color.WHITE);
            //修正圆点坐标
            revise();
            //随机设置画笔颜色
            setPaintRandomColor();

            //绘制小圆作为小球

            canvas.drawCircle(cx, cy, radius, paint);




            paint.setColor(Color.GRAY);
            canvas.drawRect(rect, paint);
            canvas.drawLine(0, 10, 0, -65, paint);
            //将画好的画布提交
            sfh.unlockCanvasAndPost(canvas);
        }


        //为画笔设置随机颜色
        private void setPaintRandomColor() {
            Random rand = new Random();
            int randomIndex = rand.nextInt(colorArray.length);
            paint.setColor(colorArray[randomIndex]);
           // paint.setColor(colorArray[1]);//模拟利用蓝色绘图
        }

        //修正圆点坐标
        private void revise() {
            if (cx <= radius) {
                cx = radius;
            } else if (cx >= (screenW - radius)) {
                cx = screenW - radius;
            }
            if (cy <= radius) {
                cy = radius;
            } else if (cy >= (screenH - radius)) {
                cy = screenH - radius;
            }
		//挡板处的修正还不完善
            if (((cx>=leftx&&xdire==1)||(cx<=rightx&&xdire==-1))&&(cy>righty&&cy<righty+radius&&ydire==-1)) {

                cy=righty+radius;
              ydire=1;
            }
        }
	
	//生成对话框
        protected void dialog(String info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BallActivity.this);
            builder.setMessage(info);

            builder.setTitle("提示");

            builder.setPositiveButton("重新开始", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                   myDraw();
                    /*if (th==null){
                        th = new Thread(BallSurfaceView.this);
                    }
                    th.start();*/
                    Message mes=handler.obtainMessage();
                    mes.what=1;
                    handler.sendMessage(mes);




                }
            });

            builder.setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();


                    BallActivity.this.finish();
                }
            });

            builder.create().show();
        }

	//对OnTouchEvent进行重写，按下，拖动，抬起，进行相应的处理
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float mcx=screenW / 2;
            float mcy=screenH - radius;

            if (touch){
            if (vt == null) {
                vt = VelocityTracker.obtain();//速度测量的工具类
            }
            vt.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 按下
                   // gamerunning=true;
                    cx = (int) event.getX();
                    cy = (int) event.getY();



                    mcx = cx;
                    mcy = cy;

                    break;
                case MotionEvent.ACTION_MOVE:
                    // 移动

                    cx =  event.getX();
                    cy =  event.getY();
                    if (cy<screenH/2){
                        cy=screenH/2;
                    }


                    vt.computeCurrentVelocity(100,100);


                    mcx = cx;
                    mcy = cy;
                    /**
                     * mcx-cx>0 right  true
                     *
                     * mcy-cy>0 bottom ture
                     */
                   /* xvelocity = vt.getXVelocity();
                    yvelocity = vt.getYVelocity();*/

                    break;
                case MotionEvent.ACTION_UP:
                    // 抬起
                    touch=false;//设置标记
                    xvelocity = vt.getXVelocity();//速度获取
                    yvelocity = vt.getYVelocity();

                  /*  cx =  event.getX();
                    cy =  event.getY();*/

                    xspeed= Math.abs(xvelocity);//可用速度
                    yspeed = Math.abs(yvelocity);
                    if (xspeed>25){
                        xspeed=25;
                    }else if (xspeed<10){
                        xspeed=10;
                    }
                    if (yspeed>25){
                        yspeed=25;
                    }else if (yspeed<10){
                        yspeed=10;
                    }

                    Log.i("TAG", "onTouchEvent: "+xspeed);
                    Log.i("TAG", "onTouchEvent: "+yspeed);


                    chufa = true;
                    Log.i("TAG", "---xx--:::" + xvelocity);
                    Log.i("TAG", "---xx--:::" + yvelocity);
		//方向的判定
                    if (xvelocity>=0f){
                        xdire=1;
                    }else{
                        xdire=-1;
                    }
                    if (yvelocity>=0f){
                        ydire=1;

                    }else{
                        ydire=-1;
                    }
                    break;


            }}

               
            //return super.onTouchEvent(event);
            return true;
        }

	//坐标的如何改变
        public void nextXY(){
            if (xspeed>=0.05){
                xspeed-=0.05;
            }else if(xspeed<0.05){
                xspeed-=0.01;
            }

            if (yspeed>=0.05){
                yspeed-=0.05;
            }else if(yspeed<0.05){
                yspeed-=0.01;
            }


        }
        private void changeXY() {


          /*  cx=cx+10*xdire;
            cy=cx+10*ydire;*/

            if (cx <= radius&&xdire==-1) {
                cx = radius;
                xdire=-xdire;
                //setPaintRandomColor();
            } else if (cx >= (screenW - radius)&&xdire==1) {
                cx = screenW - radius;
                xdire=-xdire;
               // setPaintRandomColor();
            }
            if (cy <= radius&&ydire==-1) {
                cy = radius;
                ydire=-ydire;
               // setPaintRandomColor();
            } else if (cy >= (screenH - radius)&&ydire==1) {
                cy = screenH - radius;
                ydire=-ydire;
                //setPaintRandomColor();
            }
//////////////////////////////////////////////
            if (((cx>=leftx-xspeed&&xdire==1)||(cx<=rightx+xspeed&&xdire==-1))&&(cy>righty&&cy<righty+radius&&ydire==-1)) {

                cy=righty+radius;
                ydire=-ydire;
                setPaintRandomColor();
            }

            if (cy<lefty){
                gamerunning=false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog("恭喜您完成了游戏，重新开始吗？");
                    }
                });
            }

            if (xspeed<0.07&&yspeed<0.07&&restart==false){
                gamerunning=false;
                restart=true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       dialog("很遗憾您没能成功，重新开始吗？");
                    }
                });
            }

        /*    cx += 10*xdire;
            cy += 10*ydire;*/
            cx+=xspeed*xdire;
            cy+=yspeed*ydire;
        }



        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {


        }

    }
}
