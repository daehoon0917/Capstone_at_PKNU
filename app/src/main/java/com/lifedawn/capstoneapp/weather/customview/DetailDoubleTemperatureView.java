package com.lifedawn.capstoneapp.weather.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.capstoneapp.R;

import java.util.ArrayList;
import java.util.List;

public class DetailDoubleTemperatureView extends View {

   private final int maxTemp;
   private final int minTemp;
   private final TextPaint tempPaint;
   private final Paint linePaint;
   private final Paint maxCirclePaint;
   private final Paint minCirclePaint;
   private final int circleRadius;
   private final String tempUnit = "°";
   private final Rect textRect = new Rect();


   private List<Integer> maxTempList = new ArrayList<>();
   private List<Integer> minTempList = new ArrayList<>();

   public DetailDoubleTemperatureView(Context context, List<Integer> minTempList,
                                      List<Integer> maxTempList) {
      super(context);
      circleRadius = (int) getResources().getDimension(R.dimen.circleRadiusTemperature);

      tempPaint = new TextPaint();
      tempPaint.setTextAlign(Paint.Align.CENTER);
      tempPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
      tempPaint.setColor(Color.BLACK);

      linePaint = new Paint();
      linePaint.setAntiAlias(true);
      linePaint.setStyle(Paint.Style.STROKE);
      linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, getResources().getDisplayMetrics()));
      linePaint.setColor(Color.DKGRAY);

      maxCirclePaint = new Paint();
      maxCirclePaint.setAntiAlias(true);
      maxCirclePaint.setStyle(Paint.Style.FILL);
      maxCirclePaint.setColor(Color.RED);

      minCirclePaint = new Paint();
      minCirclePaint.setAntiAlias(true);
      minCirclePaint.setStyle(Paint.Style.FILL);
      minCirclePaint.setColor(Color.BLUE);

      this.minTempList.addAll(minTempList);
      this.maxTempList.addAll(maxTempList);

      int max = Integer.MIN_VALUE;
      int min = Integer.MAX_VALUE;
      int maxTemp = 0;
      int minTemp = 0;

      for (int i = 0; i < this.minTempList.size(); i++) {
         maxTemp = this.maxTempList.get(i);
         minTemp = this.minTempList.get(i);

         // 최대,최소 기온 구하기
         if (maxTemp >= max) {
            max = maxTemp;
         }
         if (minTemp <= min) {
            min = minTemp;
         }
      }
      this.maxTemp = max;
      this.minTemp = min;
      setWillNotDraw(false);
   }

   public void setTempTextSize(int textSizeSp) {
      tempPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics()));
   }

   public void setTempTextSizePx(int textSize) {
      tempPaint.setTextSize(textSize);
   }

   public void setTextColor(int textColor) {
      tempPaint.setColor(textColor);
   }

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      String test = "20°";
      tempPaint.getTextBounds(test, 0, test.length(), textRect);
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
   }

   @Override
   protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      super.onLayout(changed, left, top, right, bottom);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      drawGraph(canvas);
   }

   private void drawGraph(Canvas canvas) {
      // 텍스트의 높이+원의 반지름 만큼 뷰의 상/하단에 여백을 설정한다.
      final float TEXT_HEIGHT = tempPaint.descent() - tempPaint.ascent();
      final float TEXT_ASCENT = -tempPaint.ascent();
      final int columnWidth = getWidth() / minTempList.size();

      final float VIEW_HEIGHT = getHeight() - ((TEXT_HEIGHT + circleRadius) * 2);
      final float SPACING = ((VIEW_HEIGHT) / (maxTemp - minTemp)) / 10f;

      int min = 0;
      int max = 0;
      float x = 0f;
      float minY = 0f;
      float maxY = 0f;

      PointF lastMinColumnPoint = new PointF();
      PointF lastMaxColumnPoint = new PointF();

      float[] minCircleXArr = new float[minTempList.size()];
      float[] minCircleYArr = new float[minTempList.size()];
      float[] maxCircleXArr = new float[minTempList.size()];
      float[] maxCircleYArr = new float[minTempList.size()];

      List<PointF> minLinePointList = new ArrayList<>();
      List<PointF> maxLinePointList = new ArrayList<>();

      for (int index = 0; index < maxTempList.size(); index++) {
         min = minTempList.get(index);
         max = maxTempList.get(index);

         x = columnWidth / 2f + columnWidth * index;
         minY = (10f * (maxTemp - min)) * SPACING + TEXT_HEIGHT + circleRadius;
         maxY = (10f * (maxTemp - max)) * SPACING + TEXT_HEIGHT + circleRadius;

         canvas.drawText(minTempList.get(index).toString() + tempUnit, x, minY + circleRadius + TEXT_HEIGHT, tempPaint);
         canvas.drawText(maxTempList.get(index).toString() + tempUnit, x, maxY - circleRadius - TEXT_HEIGHT + TEXT_ASCENT, tempPaint);

         lastMinColumnPoint.set(x, minY);
         lastMaxColumnPoint.set(x, maxY);

         minCircleXArr[index] = x;
         minCircleYArr[index] = minY;
         maxCircleXArr[index] = x;
         maxCircleYArr[index] = maxY;

         minLinePointList.add(new PointF(lastMinColumnPoint.x, lastMinColumnPoint.y));
         maxLinePointList.add(new PointF(lastMaxColumnPoint.x, lastMaxColumnPoint.y));
      }

      final int tempsCount = minTempList.size();
      PointF[] minPoints1 = new PointF[tempsCount];
      PointF[] minPoints2 = new PointF[tempsCount];

      PointF[] maxPoints1 = new PointF[tempsCount];
      PointF[] maxPoints2 = new PointF[tempsCount];

      for (int i = 1; i < tempsCount; i++) {
         minPoints1[i] = new PointF((minLinePointList.get(i).x + minLinePointList.get(i - 1).x) / 2, minLinePointList.get(i - 1).y);
         minPoints2[i] = new PointF((minLinePointList.get(i).x + minLinePointList.get(i - 1).x) / 2, minLinePointList.get(i).y);

         maxPoints1[i] = new PointF((maxLinePointList.get(i).x + maxLinePointList.get(i - 1).x) / 2, maxLinePointList.get(i - 1).y);
         maxPoints2[i] = new PointF((maxLinePointList.get(i).x + maxLinePointList.get(i - 1).x) / 2, maxLinePointList.get(i).y);
      }

      Path minPath = new Path();
      Path maxPath = new Path();
      minPath.moveTo(minLinePointList.get(0).x, minLinePointList.get(0).y);
      maxPath.moveTo(maxLinePointList.get(0).x, maxLinePointList.get(0).y);

      //곡선 그리기
      for (int i = 1; i < tempsCount; i++) {
         minPath.cubicTo(minPoints1[i].x, minPoints1[i].y, minPoints2[i].x, minPoints2[i].y, minLinePointList.get(i).x,
                 minLinePointList.get(i).y);
         maxPath.cubicTo(maxPoints1[i].x, maxPoints1[i].y, maxPoints2[i].x, maxPoints2[i].y, maxLinePointList.get(i).x,
                 maxLinePointList.get(i).y);
      }

      canvas.drawPath(minPath, linePaint);
      canvas.drawPath(maxPath, linePaint);

      for (int i = 0; i < minCircleXArr.length; i++) {
         canvas.drawCircle(minCircleXArr[i], minCircleYArr[i], circleRadius, minCirclePaint);
         canvas.drawCircle(minCircleXArr[i], maxCircleYArr[i], circleRadius, maxCirclePaint);
      }

   }
}
