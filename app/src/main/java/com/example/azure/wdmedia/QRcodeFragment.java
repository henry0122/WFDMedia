package com.example.azure.wdmedia;


import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.magiclen.magicqrcode.QRCodeEncoder;


/**
 * A simple {@link Fragment} subclass.
 */
public class QRcodeFragment extends Fragment {

    private View mContentView = null;

    private Button scannerButton;
    private Button genButton;
    private QRCodeEncoder qr;
    private boolean[][] qrData;

    private QRListener qrListener;

    public void setOnQRListener(QRListener q) {
        qrListener = q;
    }


    public QRcodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mContentView = inflater.inflate(R.layout.fragment_qrcode,null);

        scannerButton = (Button) mContentView.findViewById(R.id.btn_receive);
        genButton = (Button) mContentView.findViewById(R.id.btn_send);

        //final MyCanvas myCanvas = new MyCanvas(mContentView);

        qr = new QRCodeEncoder("127.0.0.1");
        qr.setErrorCorrect(QRCodeEncoder.ErrorCorrect.MAX);
        qrData = qr.encode();


        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), BarcodeScanner.class);
                startActivity(intent);
            }
        });

        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), ShowQRcode.class);
//                intent.putExtra("QRDATA", qrData);
//                startActivity(intent);

                //setContentView(myCanvas);

                //drawQRCode(canvas, qrData);

            }
        });

        return mContentView;
    }


    // inner class for saving QRcode
    public class MyCanvas extends View
    {
        public  MyCanvas(Context context)
        {
            super(context);
        }

        @Override
        public void draw(Canvas canvas)
        {
            super.draw(canvas);
            drawQRCode(canvas, qrData);
        }

        public void drawQRCode(final Canvas canvas, final boolean[][] qrData) {
            final Paint paint = new Paint();
            final int width = canvas.getWidth();
            final int height = canvas.getHeight();

            //畫背景(全白)
            paint.setColor(Color.WHITE);
            canvas.drawRect(new Rect(0, 0, width, height), paint);

            final int imageSize = Math.min(width, height);
            final int length = qrData.length;
            final int size = imageSize / length;
            final int actualImageSize = size * length;
            final int offsetImageX = (width - actualImageSize) / 2;
            final int offsetImageY = (height - actualImageSize) / 2;

            //畫資料(true為黑色)
            paint.setColor(Color.BLACK);
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    if (qrData[i][j]) {
                        final int x = i * size + offsetImageX;
                        final int y = j * size + offsetImageY;
                        canvas.drawRect(new Rect(x, y, x + size, y + size), paint);
                    }
                }
            }
        }

    }

    public interface QRListener {

        void btnQRsendListener();

        void btnQRreceiveListener();


    }

}
