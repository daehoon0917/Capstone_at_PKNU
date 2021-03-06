package com.lifedawn.capstoneapp.weather.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.capstoneapp.R;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateView extends View {
	private final int columnWidth;
	private final int viewWidth;
	private final TextPaint dateTextPaint;
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE");
	private List<DateValue> dateValueList;
	private int currentX;
	private int firstColX;

	private int padding;
	private int viewHeight;
	

	public DateView(Context context, int viewWidth, int columnWidth) {
		super(context);
		this.viewWidth = viewWidth;
		this.columnWidth = columnWidth;
		padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());

		dateTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		dateTextPaint.setTextAlign(Paint.Align.CENTER);
		dateTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, getResources().getDisplayMetrics()));
		dateTextPaint.setColor(Color.BLACK);

		setWillNotDraw(false);
	}

	public void setTextSize(int textSizeSp) {
		dateTextPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp,
				getResources().getDisplayMetrics()));
	}

	public void setTextColor(int textColor) {
		dateTextPaint.setColor(textColor);
	}

	public void init(List<ZonedDateTime> dateTimeList) {
		ZonedDateTime date = ZonedDateTime.of(dateTimeList.get(0).toLocalDateTime(), dateTimeList.get(0).getZone());
		ZonedDateTime lastDate = ZonedDateTime.of(date.toLocalDateTime(), date.getZone());

		List<DateView.DateValue> dateValueList = new ArrayList<>();
		int beginX = 0;

		for (int col = 0; col < dateTimeList.size(); col++) {
			date = ZonedDateTime.of(dateTimeList.get(col).toLocalDateTime(), lastDate.getZone());

			if (date.getHour() == 0 || col == 0) {
				if (dateValueList.size() > 0) {
					dateValueList.get(dateValueList.size() - 1).endX = columnWidth * (col - 1) + columnWidth / 2;
				}
				beginX = columnWidth * col + columnWidth / 2;
				dateValueList.add(new DateView.DateValue(beginX, date));
			}

			if (lastDate.getDayOfYear() != date.getDayOfYear()) {
				lastDate = date;
			}
		}
		dateValueList.get(dateValueList.size() - 1).endX = columnWidth * (dateTimeList.size() - 1) + columnWidth / 2;

		this.dateValueList = dateValueList;
		this.firstColX = dateValueList.get(0).beginX;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		StaticLayout staticLayout = null;
		viewHeight = Integer.MIN_VALUE;
		String str = null;

		for (DateValue val : dateValueList) {
			str = val.date.format(dateTimeFormatter);
			StaticLayout.Builder builder = StaticLayout.Builder.obtain(str, 0, str.length(), dateTextPaint,
					columnWidth);
			staticLayout = builder.build();
			viewHeight = Math.max(staticLayout.getHeight(), viewHeight);
		}

		viewHeight += padding * 2;
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		for (DateValue dateValue : dateValueList) {
			if (currentX >= dateValue.beginX - firstColX && currentX < dateValue.endX - firstColX) {
				dateValue.lastX = currentX + firstColX;
			} else if (currentX < dateValue.beginX) {
				dateValue.lastX = dateValue.beginX;
			}
			drawText(canvas, dateValue.date.format(dateTimeFormatter), dateValue.lastX);
		}
	}

	public void reDraw(int newX) {
		this.currentX = newX;
		invalidate();
	}

	private void drawText(Canvas canvas, String textOnCanvas, float x) {
		StaticLayout.Builder builder = StaticLayout.Builder.obtain(textOnCanvas, 0, textOnCanvas.length(), dateTextPaint, columnWidth);
		StaticLayout staticLayout = builder.build();

		float y = viewHeight / 2f - (staticLayout.getHeight() / 2f);

		canvas.save();
		canvas.translate(x, y);
		staticLayout.draw(canvas);
		canvas.restore();
	}


	public static class DateValue {
		public final int beginX;
		public final ZonedDateTime date;
		public int endX;
		public int lastX;

		public DateValue(int beginX, ZonedDateTime date) {
			this.beginX = beginX;
			this.date = date;
			this.lastX = beginX;
		}
	}
}