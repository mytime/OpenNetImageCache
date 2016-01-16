package cn.uhei.opennetimagecache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private ImageView iv;


    //Handler消息处理器
    Handler handler = new Handler() {

        //此方法在主线程中调用，可以用来刷新UI
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    //把位图对象显示到ImageView
                    iv.setImageBitmap((Bitmap) msg.obj);
                    break;
                case 0:
                    Toast.makeText(MainActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = (ImageView) findViewById(R.id.iv);



        findViewById(R.id.btnOpen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //下载图片
                //1 确定网址
                final String path = "http://d02.res.meilishuo.net/pic/l/1d/42/94ed817aeb9f8ef007c7ab762812_600_800.jpg";
                //把图片流写入缓存目录
                //arg:保存的路径(缓存目录)，文件名
                final  File file = new File(getCacheDir(),getFileName(path));

                if(file.exists()){
                    //如果缓存存在，从还从中读取图片
                    System.out.println("从缓存读取的");
                    Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                    iv.setImageBitmap(bm);

                }else{
                    System.out.println("从网络下载的");
                    //如果图片不存在，从网络下载
                    new Thread() {
                        @Override
                        public void run() {


                            try {
                                // 2 把网址封装成URL
                                URL url = new URL(path);

                                // 3获取连接对象
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                                //4 对连接对象进行初始化
                                //设置请求方法，大写
                                conn.setRequestMethod("GET");

                                //设置连接超时
                                conn.setConnectTimeout(5000);

                                //设置读取超时
                                conn.setReadTimeout(5000);

                                //5 发送请求，与服务器建立连接
                                conn.connect();

                                //如果响应码为200，说明请求成功
                                if (conn.getResponseCode() == 200) {

                                    //获取服务器响应头中的流，流里的数据就是客户端请求的数据
                                    InputStream is = conn.getInputStream();


                                    //文件输出流
                                    FileOutputStream fos = new FileOutputStream(file);
                                    //每次读取一个字节
                                    byte[] b = new byte[1024];
                                    int len = 0;
                                    //读取is 流，每次读1个字节，然后赋值给len, 流读完是-1，
                                    while ((len = is.read(b)) != -1){
                                        //用字字节方式写写到缓存目录，从0开始，到len结束
                                        fos.write(b,0,len);
                                    }
                                    //关闭流
                                    fos.close();

                                    //读取出流里的数据，并构造成位图对象
//                                Bitmap bm = BitmapFactory.decodeStream(is);
                                    Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());

                                    //更新Ui 必须在主线程的Handler.handleMessage方法里执行
//                                ImageView iv = (ImageView) findViewById(R.id.iv);
//                                //把位图对象显示到ImageView
//                                iv.setImageBitmap(bm);

                                    //把消息发送至主线程的消息队列
                                    Message msg = new Message();
                                    //消息可以携带任何数据被发送出去,
                                    //消息携带位图对象被发送到主线的消息队列，
                                    //然后由消息looper(轮询器)转发发消息传给Handler,
                                    //最后由handleMessage来处理消息
                                    msg.obj = bm;

                                    //1 表示消息发送成功
                                    msg.what = 1;
                                    handler.sendMessage(msg);


                                } else {

                                    Message msg = handler.obtainMessage();

                                    //0 表示消息发送失败
                                    msg.what = 0;
                                    handler.sendMessage(msg);


                                }

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }.start();
                }


//


            }
        });



    }

    //截取url中d文件名称
    public String getFileName(String path){
        int index = path.lastIndexOf("/");
        return path.substring(index+1);
    }

}
