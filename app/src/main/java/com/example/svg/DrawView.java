package com.example.svg;

import android.content.Context;
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
    public static String svgpath;
    private Paint paint = new Paint();
    public DrawView(Context context) {
        super(context);
        svgpath = MainActivity.svg;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        paint.setColor(color);
        paint.setStrokeWidth(2);
        canvas.drawColor(Color.BLACK);
        Path curvePath = SvgToCanvas(getSvgArray(svgpath));
        scalePath(curvePath);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(curvePath, paint);
    }
    public void scalePath(Path path) { //масштабирование кривой
        float screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        float screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, screenWidth, screenHeight);
        matrix.setRectToRect(rectF, viewRect, Matrix.ScaleToFit.CENTER);
        path.transform(matrix);
    }
    public List<List<String>> getSvgArray(String svg) {
        //получаем из svg-path массив с командами
        List<List<String>> list = new ArrayList<>();
        svg = svg.replaceAll(",", " ");
        svg = svg.replaceAll("([a-zA-z])", " $1 ");
        svg = svg.trim().replaceAll("\\s{2,}", " ");
        String[] svgCommands = svg.split("(?=[MHVCZQLSTmhvczqlst])");
        for (int i = 0; i < svgCommands.length; i++) {
            list.add(new ArrayList(Arrays.asList(svgCommands[i].split(" "))));
        }
        return list;
    }
    public Path SvgToCanvas(List<List<String>> svgCommands) {
        Path path = new Path();
        Float[] currentPoint = new Float[2]; //текущая позиция
        Float[] startPoint = new Float[2];  //начальная точка
        Float[] beforeReflection = new Float[2]; //последняя контрольная точка кривой
        boolean newSubpath = true; //начало новой фигуры или нет
        for (int i = 0; i < svgCommands.size(); i++) {
            List<String> line = svgCommands.get(i);
            switch (line.get(0)) {   //какая команда в начале
                case "m":
                    currentPoint[0] += Float.parseFloat((line.get(1)));
                    currentPoint[1] += Float.parseFloat(line.get(2));
                    path.moveTo(currentPoint[0], currentPoint[1]);
                    beforeReflection = currentPoint.clone();
                    if (newSubpath) {
                        startPoint = currentPoint.clone();
                        newSubpath = false;
                    }
                    break;
                case "l":
                    currentPoint[0] += Float.parseFloat((line.get(1)));
                    currentPoint[1] += Float.parseFloat(line.get(2));
                    path.lineTo(currentPoint[0], currentPoint[1]);
                    beforeReflection = currentPoint.clone();
                    break;
                case "v":
                    currentPoint[1] += Float.parseFloat(line.get(1));
                    path.lineTo(currentPoint[0], currentPoint[1]);
                    beforeReflection = currentPoint.clone();
                    break;
                case "h":
                    currentPoint[0] += Float.parseFloat(line.get(1));
                    path.lineTo(currentPoint[0], currentPoint[1]);
                    beforeReflection = currentPoint.clone();
                    break;
                case "q":
                    svgQuadTo(path, line, currentPoint, beforeReflection, true);
                    break;
                case "c":
                    svgCubicTo(path, line, currentPoint, beforeReflection, true);
                    break;
                case "s":
                    svgSCubicTo(path, line, currentPoint, beforeReflection, true);
                    break;
                case "t":
                    svgTQuadTo(path, line, currentPoint, beforeReflection, true);
                    break;
                case "M":
                    currentPoint[0] = Float.parseFloat((line.get(1)));
                    currentPoint[1] = Float.parseFloat(line.get(2));
                    path.moveTo(currentPoint[0], currentPoint[1]);
                    beforeReflection = currentPoint.clone();
                    if (newSubpath) {
                        startPoint = currentPoint.clone();
                        newSubpath = false;
                    }
                    break;
                case "Z":
                case "z":
                    path.close();
                    currentPoint = startPoint.clone();
                    newSubpath = true;
                    break;
                case "L":
                    currentPoint[0] = Float.parseFloat((line.get(1)));
                    currentPoint[1] = Float.parseFloat(line.get(2));
                    path.lineTo(currentPoint[0], currentPoint[1]);
                    beforeReflection = currentPoint.clone();
                    break;
                case "V":
                    currentPoint[1] = Float.parseFloat(line.get(1));
                    beforeReflection = currentPoint.clone();
                    break;
                case "H":
                    currentPoint[0] = Float.parseFloat(line.get(1));
                    path.lineTo(currentPoint[0], currentPoint[1]);
                    beforeReflection = currentPoint.clone();
                    break;
                case "Q":
                    svgQuadTo(path, line, currentPoint, beforeReflection, false);
                    break;
                case "C":
                    svgCubicTo(path, line, currentPoint, beforeReflection, false);
                    break;
                case "S":
                    svgSCubicTo(path, line, currentPoint, beforeReflection, false);
                    break;
                case "T":
                    svgTQuadTo(path, line, currentPoint, beforeReflection, false);
                    break;
                //case "A":
            }
        }
        return path;
    }


    public Path svgSCubicTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean relative) {
        //выподнение команд S,s
        int iteration = 0;
        Float[] afterReflection = new Float[2];
        afterReflection[0] = point[0] - (reflect[0] - point[0]); //абсолютные координаты первой
        // контрольной точки
        afterReflection[1] = point[1] - (reflect[1] - point[1]);
        while (!(list.size() < iteration * 4 + 4)) {
            if (relative) {
                path.rCubicTo(afterReflection[0] - point[0], afterReflection[1] - point[1], Float.parseFloat(list.get(1 + 4 * iteration)), Float.parseFloat(list.get(2 + 4 * iteration)), Float.parseFloat(list.get(3 + 4 * iteration)), Float.parseFloat(list.get(4 + 4 * iteration)));
                reflect[0] = point[0] + Float.parseFloat(list.get(1 + 4 * iteration));
                reflect[1] = point[1] + Float.parseFloat(list.get(2 + 4 * iteration));
                point[0] += Float.parseFloat(list.get(3 + 4 * iteration));
                point[1] += Float.parseFloat(list.get(4 + 4 * iteration));
            }
            else {
                path.cubicTo(afterReflection[0], afterReflection[1], Float.parseFloat(list.get(1 + 4 * iteration)), Float.parseFloat(list.get(2 + 4 * iteration)), Float.parseFloat(list.get(3 + 4 * iteration)), Float.parseFloat(list.get(4 + 4 * iteration)));
                reflect[0] = Float.parseFloat(list.get(1 + 4 * iteration));
                reflect[1] = Float.parseFloat(list.get(2 + 4 * iteration));
                point[0] = Float.parseFloat(list.get(3 + 4 * iteration));
                point[1] = Float.parseFloat(list.get(4 + 4 * iteration));
            }
            iteration++;
        }
        return path;
    }
    public Path svgCubicTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean relative) {
        //команды C,c - кубическая кривая Безье
        int iteration = 0;
        while (!(list.size() < iteration * 6 + 6)) {
            if (relative) {
                path.rCubicTo(Float.parseFloat(list.get(1 + 6 * iteration)), Float.parseFloat(list.get(2 + 6 * iteration)), Float.parseFloat(list.get(3 + 6 * iteration)), Float.parseFloat(list.get(4 + 6 * iteration)), Float.parseFloat(list.get(5 + 6 * iteration)), Float.parseFloat(list.get(6 + 6 * iteration)));
                reflect[0] = point[0] + Float.parseFloat(list.get(3 + 6 * iteration));
                reflect[1] = point[1] + Float.parseFloat(list.get(4 + 6 * iteration));
                point[0] += Float.parseFloat(list.get(5 + 6 * iteration));
                point[1] += Float.parseFloat(list.get(6 + 6 * iteration));
            }
            else {
                path.cubicTo(Float.parseFloat(list.get(1 + 6 * iteration)), Float.parseFloat(list.get(2 + 6 * iteration)), Float.parseFloat(list.get(3 + 6 * iteration)), Float.parseFloat(list.get(4 + 6 * iteration)), Float.parseFloat(list.get(5 + 6 * iteration)), Float.parseFloat(list.get(6 + 6 * iteration)));
                reflect[0] = Float.parseFloat(list.get(3 + 6 * iteration));
                reflect[1] = Float.parseFloat(list.get(4 + 6 * iteration));
                point[0] = Float.parseFloat(list.get(5 + 6 * iteration));
                point[1] = Float.parseFloat(list.get(6 + 6 * iteration));
            }
            iteration++;
        }
        return path;
    }
    public Path svgQuadTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean relative) {
        //команды Q,q - квадратичная кривая Безье
        int iteration = 0;
        while (!(list.size() < iteration * 4 + 4)) {
            if (relative) {
                path.rQuadTo(Float.parseFloat(list.get(1 + 4 * iteration)), Float.parseFloat(list.get(2 + 4 * iteration)), Float.parseFloat(list.get(3 + 4 * iteration)), Float.parseFloat(list.get(4 + 4 * iteration)));
                reflect[0] = point[0] + Float.parseFloat(list.get(1 + 4 * iteration));
                reflect[1] = point[1] + Float.parseFloat(list.get(2 + 4 * iteration));
                point[0] += Float.parseFloat(list.get(3 + 4 * iteration));
                point[1] += Float.parseFloat(list.get(4 + 4 * iteration));
            }
            else {
                path.quadTo(Float.parseFloat(list.get(1 + 4 * iteration)), Float.parseFloat(list.get(2 + 4 * iteration)), Float.parseFloat(list.get(3 + 4 * iteration)), Float.parseFloat(list.get(4 + 4 * iteration)));
                reflect[0] = Float.parseFloat(list.get(1 + 4 * iteration));
                reflect[1] = Float.parseFloat(list.get(2 + 4 * iteration));
                point[0] = Float.parseFloat(list.get(3 + 4 * iteration));
                point[1] = Float.parseFloat(list.get(4 + 4 * iteration));
            }
            iteration++;
        }
        return path;
    }
    public Path svgTQuadTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean relative) {
        //команды T,t
        int iteration = 0;
        Float[] afterReflection = new Float[2];
        afterReflection[0] = point[0] - (reflect[0] - point[0]);
        afterReflection[1] = point[1] - (reflect[1] - point[1]);
        while (!(list.size() < iteration * 2 + 2)) {
            if (relative) {
                path.rQuadTo(afterReflection[0] - point[0], afterReflection[1] - point[1], Float.parseFloat(list.get(1 + 2 * iteration)), Float.parseFloat(list.get(2 + 2 * iteration)));
                reflect[0] = afterReflection[0];
                reflect[1] = afterReflection[1];
                point[0] += Float.parseFloat(list.get(1 + 2 * iteration));
                point[1] += Float.parseFloat(list.get(2 + 2 * iteration));
            }
            else {
                path.quadTo(afterReflection[0], afterReflection[1], Float.parseFloat(list.get(1 + 2 * iteration)), Float.parseFloat(list.get(2 + 2 * iteration)));
                reflect[0] = afterReflection[0];
                reflect[1] = afterReflection[1];
                point[0] = Float.parseFloat(list.get(1 + 2 * iteration));
                point[1] = Float.parseFloat(list.get(2 + 2 * iteration));
            }
            iteration++;
        }
        return path;
    }
}
