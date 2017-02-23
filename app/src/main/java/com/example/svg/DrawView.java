package com.example.svg;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class DrawView extends View {
    private Paint paint = new Paint();

     public static String svgpath;
    public DrawView(Context context){
        super(context);
        svgpath= MainActivity.svg;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        paint.setColor(color);
        paint.setStrokeWidth(2);
       paint.setStyle(Paint.Style.STROKE);
        canvas.drawColor(Color.BLACK);
        canvas.drawPaint(paint);
        Path curvePath= SvgToCanvas(getSvgArray(svgpath));
        scalePath(curvePath);
        canvas.drawPath(curvePath, paint);
}
    public void scalePath(Path path){
        float screenHeight= Resources.getSystem().getDisplayMetrics().heightPixels;
        float screenWidth= Resources.getSystem().getDisplayMetrics().widthPixels;
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, screenWidth, screenHeight);
        matrix.setRectToRect(rectF, viewRect, Matrix.ScaleToFit.CENTER);
       path.transform(matrix);
    }
    public List<List<String>> getSvgArray(String svg) {
        List<List<String>> list = new ArrayList<List<String>>();
        svg=svg.replaceAll("," , " ");
        svg=svg.toUpperCase();
        svg=svg.replaceAll("([A-Z])", " $1 ");
        svg=svg.trim().replaceAll("\\s{2,}", " ");
        String[] svgCommands = svg.split("(?=[MHVCZQL])");
        for (int i = 0; i < svgCommands.length; i++) {
            list.add(new ArrayList(Arrays.asList(svgCommands[i].split(" "))));
                }
        return list;
    }
    public Path SvgToCanvas(List<List<String>> svgCommands) {
        Path path = new Path();

        Float[] currentPoint = new Float[2];
        for (int i = 0; i < svgCommands.size(); i++) {
            List<String> line=svgCommands.get(i);
            switch (line.get(0)) {
                case "M":
                  currentPoint[0]=Float.parseFloat((line.get(1)));
                    currentPoint[1]=Float.parseFloat(line.get(2));
                    path.moveTo(currentPoint[0], currentPoint[1]);
                    break;
                case "Z":
                    path.close();break;
                case "L":
                    currentPoint[0]=Float.parseFloat((line.get(1)));
                    currentPoint[1]=Float.parseFloat(line.get(2));
                    path.lineTo(currentPoint[0], currentPoint[1]);
                    break;
                case "V":
                    currentPoint[1]=Float.parseFloat(line.get(1));
                    path.lineTo(currentPoint[0], currentPoint[1]);
                    break;
                case "H":
                    currentPoint[0]=Float.parseFloat(line.get(1));
                    path.lineTo(currentPoint[0], currentPoint[1]);

                    break;
                case "Q":
                    svgQuadTo(path,line, currentPoint);
                    break;
                case "C":
                    svgCubicTo(path,line, currentPoint);
                    break;
                    //case "A":
            }
        }
        return path;
    }

    public Path svgCubicTo(Path path, List<String> list, Float[] point) {
        list.remove(0);
        while (!list.isEmpty()) {
            if (list.get(0).equals("S")) {
                path.cubicTo(point[0], point[1],
                        Float.parseFloat(list.get(1)), Float.parseFloat(list.get(2)),
                        Float.parseFloat(list.get(3)), Float.parseFloat(list.get(4)));
                list.subList(0,5).clear();
                point[0]= Float.parseFloat(list.get(3));
                point[1]=Float.parseFloat(list.get(4));
               continue;
            }
            {

               path.cubicTo(Float.parseFloat(list.get(0)), Float.parseFloat(list.get(1)), Float
                    .parseFloat(list.get(2)), Float.parseFloat(list.get(3)), Float.parseFloat
                       (list.get(4)), Float.parseFloat(list.get(5)));
                point[0] = Float.parseFloat(list.get(4));
                point[1] = Float.parseFloat(list.get(5));
                list.subList(0,6).clear();
            }

        }
        return path;}
    public Path svgQuadTo(Path path, List<String> list, Float[] point) {
        list.remove(0);
        while (!list.isEmpty()) {
            if (list.get(0).equals("T")) {
                path.quadTo(point[0], point[1], Integer
                        .getInteger(list.get(1)), Float.parseFloat(list.get(2)));
                point[0]= Float.parseFloat(list.get(1));
                point[1]=Float.parseFloat(list.get(2));
                list.subList(0,3).clear();
                continue;
            }
            {
               path.quadTo(Float.parseFloat(list.get(0)), Float.parseFloat(list.get(1)), Float
                    .parseFloat(list.get(2)), Float.parseFloat(list.get(3)));
                point[0]= Float.parseFloat(list.get(2));
                point[1]=Float.parseFloat(list.get(3));
                list.subList(0,4).clear();
            }

        }
        return path;}
}
