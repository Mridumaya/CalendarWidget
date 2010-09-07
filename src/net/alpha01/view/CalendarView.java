package net.alpha01.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import net.alpha01.R;
import net.alpha01.listener.DayClickListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CalendarView extends TableLayout {
	private ArrayList<DayTextView> days = new ArrayList<DayTextView>();
	private TableRow rows[] = new TableRow[6];
	private DayClickListener dayClickListener = null;
	private Calendar cal;
	private HashMap<Date,Integer> highlightedColorDate= new  HashMap<Date, Integer>();
	private TextView nextLbl, prevLbl;
	private TextView currMonthTextView;
	private Resources res;

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.DKGRAY);
		// retreive resources
		res = getResources();

		// retrieve the year and month argument
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
		setCal(GregorianCalendar.getInstance());

		int m = a.getInteger(R.styleable.CalendarView_month, 0);
		int y = a.getInteger(R.styleable.CalendarView_year, 0);
		if (m > 0) {
			cal.set(Calendar.MONTH, m - 1);
		}
		if (y > 0) {
			cal.set(Calendar.YEAR, y);
		}

		setStretchAllColumns(a.getBoolean(R.styleable.CalendarView_stretchColumns, false));

		// create the first row of buttons
		prevLbl = new TextView(context, attrs);
		prevLbl.setText("<<");
		prevLbl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cal.add(Calendar.MONTH, -1);
				refresh();
			}
		});

		nextLbl = new TextView(context, attrs);
		nextLbl.setText(">>");
		nextLbl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cal.add(Calendar.MONTH, 1);
				refresh();
			}
		});

		currMonthTextView = new TextView(context, attrs);
		currMonthTextView.setGravity(Gravity.CENTER);

		TableRow firstRow = new TableRow(context, attrs);
		firstRow.addView(prevLbl);
		android.widget.TableRow.LayoutParams firstRowLayout = firstRow.generateLayoutParams(attrs);
		firstRowLayout.span = 5;
		firstRow.addView(currMonthTextView, firstRowLayout);
		firstRow.addView(nextLbl);
		addView(firstRow, 0);

		// Second Row ( Day Of weeks row )
		TypedArray dayOfWeek = res.obtainTypedArray(R.array.DaysOfWeek);
		TableRow secondRow = new TableRow(context, attrs);
		android.widget.TableRow.LayoutParams secondRowParam = secondRow.generateLayoutParams(attrs);
		for (int i = 1; i <= 7; i++) {
			if (i == 0) {
				secondRowParam.setMargins(1, 0, 1, 1);
			} else {
				secondRowParam.setMargins(0, 0, 1, 1);
			}
			TextView dayLbl = new TextView(context, attrs);
			dayLbl.setBackgroundColor(Color.BLACK);
			dayLbl.setText(dayOfWeek.getString(i - 1));
			dayLbl.setGravity(Gravity.CENTER);
			secondRow.addView(dayLbl, secondRowParam);
		}
		addView(secondRow);

		// Set layout for each row (to do border)
		LayoutParams singleRowLayout = generateLayoutParams(attrs);
		singleRowLayout.setMargins(0, 1, 0, 0);
		for (int r = 0; r < 6; r++) {
			rows[r] = new TableRow(context, attrs);
			android.widget.TableRow.LayoutParams rowParam = rows[r].generateLayoutParams(attrs);
			for (int i = 0; i < 7; i++) {
				if (i == 0) {
					rowParam.setMargins(1, 0, 1, 0);
				} else {
					if (r < 5) {
						rowParam.setMargins(0, 0, 1, 0);
					} else {
						rowParam.setMargins(0, 0, 1, 1);
					}
				}
				int curDow = (r * 7) + (i + 1);
				DayTextView day = new DayTextView(context, attrs, getCal().getTime());
				day.setBackgroundColor(Color.BLACK);
				day.setWidth(50);
				day.setText(Integer.toString(curDow));
				days.add(day);
				rows[r].addView(day, i, rowParam);
			}
			addView(rows[r], singleRowLayout);
		}
		// refresh the current Date
		refresh();
	}

	private void disableDay(DayTextView d) {
		d.setText(" ");
		d.setEnabled(false);
	}

	public void refresh() {
		refresh(this.getCal());
	}

	public void refresh(Calendar aCal) {
		setCal(aCal);
		Calendar tmpCal = GregorianCalendar.getInstance();
		tmpCal.setTime(cal.getTime());

		TypedArray monthName = res.obtainTypedArray(R.array.MonthName);
		currMonthTextView.setText(monthName.getString(tmpCal.get(Calendar.MONTH)) + " " + tmpCal.get(Calendar.YEAR));
		// retrieve the first day of week
		int fdow = cal.get(Calendar.DAY_OF_WEEK) - 1;
		// 1 is Sunday
		if (fdow == 0) {
			fdow = 7;
		}

		Object daysAr[] = days.toArray();
		// disable the first days
		for (int i = 0; i < fdow - 1; i++) {
			disableDay((DayTextView) daysAr[i]);
		}
		// Set the day number
		int numOfDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		Log.d("CalView", "numOfDays:" + numOfDays);
		for (int i = 1; i <= numOfDays; i++) {
			int arPos = i + fdow - 2;
			Log.d("CalView", "Pos" + arPos + "CurrDay:" + tmpCal.get(Calendar.DAY_OF_MONTH));
			((DayTextView) daysAr[arPos]).setDate(tmpCal.getTime());
			((DayTextView) daysAr[arPos]).setText(Integer.toString(tmpCal.get(Calendar.DAY_OF_MONTH)));
			((DayTextView) daysAr[arPos]).setEnabled(true);
			// Check if highlighted Day
			if (highlightedColorDate.containsKey(tmpCal.getTime())) {
				((DayTextView) daysAr[arPos]).setTextColor(highlightedColorDate.get(tmpCal.getTime()));
			}
			Log.d("CalView", "day:" + i);
			// increment the day
			tmpCal.add(Calendar.DAY_OF_MONTH, 1);
		}
		for (int i = numOfDays + fdow - 1; i < daysAr.length; i++) {
			disableDay((DayTextView) daysAr[i]);
		}
	}

	public void setCal(Calendar aCal) {
		if (cal == null) {
			cal = GregorianCalendar.getInstance();
		}
		cal.setTime(aCal.getTime());
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	public Calendar getCal() {
		return cal;
	}

	public void setOnDayClickListener(DayClickListener listener) {
		this.dayClickListener = listener;
		Iterator<DayTextView> itD = days.iterator();
		while (itD.hasNext()) {
			DayTextView day = itD.next();
			day.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (dayClickListener != null) {
						dayClickListener.onDayClick(v, ((DayTextView) v).getDate());
					}
				}
			});
		}
	}

	public void setHighlightedDate(HashSet<Date> aHighlighedDate,int color) {
		cleanHighlightedDate(color);
		Iterator<Date> itD = aHighlighedDate.iterator();
		Calendar cal = GregorianCalendar.getInstance();
		while (itD.hasNext()) {
			cal.setTime(itD.next());
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			highlightedColorDate.put(cal.getTime(), color);
		}
		refresh();
	}

	public HashSet<Date> getHighlightedDate(int color) {
		Iterator<Entry<Date,Integer>> itD = this.highlightedColorDate.entrySet().iterator();
		HashSet<Date> result=new HashSet<Date>();
		while (itD.hasNext()){
			Entry<Date,Integer> entity = itD.next();
			if (entity.getValue()==color){
				result.add(entity.getKey());
			}
		}
		return result;
	}

	public void cleanHighlightedDate(int color) {
		Iterator<Entry<Date,Integer>> itD = this.highlightedColorDate.entrySet().iterator();
		while (itD.hasNext()){
			Entry<Date,Integer> entity = itD.next();
			if (entity.getValue()==color){
				highlightedColorDate.remove(entity.getKey());
			}
		}
		refresh();
	}
	
	public void cleanHighlightedDate() {
		this.highlightedColorDate.clear();
		refresh();
	}

}
