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
        String[] svgCommands = svg.split("(?=[MHVCZQALSTmhavczqlst])");
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

    /**
     * @param path     - объект Path
     * @param list     - команда
     * @param point    - текущая точка
     * @param reflect  - контрльная точка послдней линии
     * @param relative - относительные или абсолютные координаты
     */
    public void svgSCubicTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean
            relative) {
        //выподнение команд S,s
        int iteration = 0;
        Float[] afterReflection = new Float[2];
        while (!(list.size() < iteration * 4 + 4)) {
            afterReflection[0] = point[0] - (reflect[0] - point[0]); //абсолютные координаты первой
            // контрольной точки
            afterReflection[1] = point[1] - (reflect[1] - point[1]);
            float x2 = Float.parseFloat(list.get(1 + 4 * iteration));
            float y2 = Float.parseFloat(list.get(2 + 4 * iteration));
            float x = Float.parseFloat(list.get(3 + 4 * iteration));
            float y = Float.parseFloat(list.get(4 + 4 * iteration));
            if (relative) {
                path.rCubicTo(afterReflection[0] - point[0], afterReflection[1] - point[1], x2, y2, x, y);
                reflect[0] = point[0] + x2;
                reflect[1] = point[1] + y2;
                point[0] += x;
                point[1] += y;
            }
            else {
                path.cubicTo(afterReflection[0], afterReflection[1], x2, y2, x, y);
                reflect[0] = x2;
                reflect[1] = y2;
                point[0] = x;
                point[1] = y;
            }
            iteration++;
        }
    }
    public void svgCubicTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean
            relative) {
        //команды C,c - кубическая кривая Безье
        int iteration = 0;
        while (!(list.size() < iteration * 6 + 6)) {
            float x1 = Float.parseFloat(list.get(1 + 6 * iteration));
            float y1 = Float.parseFloat(list.get(2 + 6 * iteration));
            float x2 = Float.parseFloat(list.get(3 + 6 * iteration));
            float y2 = Float.parseFloat(list.get(4 + 6 * iteration));
            float x = Float.parseFloat(list.get(5 + 6 * iteration));
            float y = Float.parseFloat(list.get(6 + 6 * iteration));
            if (relative) {
                path.rCubicTo(x1, y1, x2, y2, x, y);
                reflect[0] = point[0] + x2;
                reflect[1] = point[1] + y2;
                point[0] += x;
                point[1] += y;
            }
            else {
                path.cubicTo(x1, y1, x2, y2, x, y);
                reflect[0] = x2;
                reflect[1] = y2;
                point[0] = x;
                point[1] = y;
            }
            iteration++;
        }
    }
    public void svgQuadTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean
            relative) {
        //команды Q,q - квадратичная кривая Безье
        int iteration = 0;
        while (!(list.size() < iteration * 4 + 4)) {
            float x1 = Float.parseFloat(list.get(1 + 4 * iteration));
            float y1 = Float.parseFloat(list.get(2 + 4 * iteration));
            float x = Float.parseFloat(list.get(3 + 4 * iteration));
            float y = Float.parseFloat(list.get(4 + 4 * iteration));
            if (relative) {
                path.rQuadTo(x1, y1, x, y);
                reflect[0] = point[0] + x1;
                reflect[1] = point[1] + y1;
                point[0] += x;
                point[1] += y;
            }
            else {
                path.quadTo(x1, y1, x, y);
                reflect[0] = x1;
                reflect[1] = y1;
                point[0] = x;
                point[1] = y;
            }
            iteration++;
        }
    }
    public void svgTQuadTo(Path path, List<String> list, Float[] point, Float[] reflect, boolean
            relative) {
        //команды T,t
        int iteration = 0;
        Float[] afterReflection = new Float[2];
        while (!(list.size() < iteration * 2 + 2)) {
            afterReflection[0] = point[0] - (reflect[0] - point[0]);
            afterReflection[1] = point[1] - (reflect[1] - point[1]);
            float x = Float.parseFloat(list.get(1 + 2 * iteration));
            float y = Float.parseFloat(list.get(2 + 2 * iteration));
            if (relative) {
                path.rQuadTo(afterReflection[0] - point[0], afterReflection[1] - point[1], x, y);
                reflect[0] = afterReflection[0];
                reflect[1] = afterReflection[1];
                point[0] += x;
                point[1] += y;
            }
            else {
                path.quadTo(afterReflection[0], afterReflection[1], x, y);
                reflect[0] = afterReflection[0];
                reflect[1] = afterReflection[1];
                point[0] = x;
                point[1] = y;
            }
            iteration++;
        }
    }
}
