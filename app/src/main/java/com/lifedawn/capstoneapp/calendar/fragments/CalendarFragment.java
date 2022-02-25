package com.lifedawn.capstoneapp.calendar.fragments;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.interfaces.IRefreshCalendar;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.CalendarDayLayoutBinding;
import com.lifedawn.capstoneapp.databinding.CalendarMonthHeaderLayoutBinding;
import com.lifedawn.capstoneapp.databinding.FragmentCalendarBinding;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CalendarFragment extends Fragment implements IRefreshCalendar {
	private final DayOfWeek FIRST_DAY_OF_WEEK = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
	private final Map<String, List<ContentValues>> eventsMap = new HashMap<>();
	private final Map<String, List<ContentValues>> attendeesMap = new HashMap<>();

	private FragmentCalendarBinding binding;
	private AccountViewModel accountViewModel;
	private CalendarViewModel calendarViewModel;

	private String myEmail;

	private ZonedDateTime firstDateTime;
	private ZonedDateTime endDateTime;
	private final ZoneId zoneId = ZoneId.systemDefault();

	private YearMonth currentMonth = YearMonth.now(ZoneId.systemDefault());
	private YearMonth firstMonth = currentMonth;
	private YearMonth lastMonth = currentMonth.plusMonths(10);

	private ContentValues calendar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountViewModel = new ViewModelProvider(getActivity()).get(AccountViewModel.class);
		calendarViewModel = new ViewModelProvider(getActivity()).get(CalendarViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentCalendarBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		myEmail = accountViewModel.lastSignInAccount().getEmail();

		binding.calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderViewContainer>() {
			final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy M");
			final DayOfWeek[] dayOfWeeks = new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
					DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
			ZonedDateTime zonedDateTime = ZonedDateTime.now();

			@NonNull
			@Override
			public MonthHeaderViewContainer create(@NonNull View view) {
				return new MonthHeaderViewContainer(view);
			}

			// 달력 상단 바인딩
			@Override
			public void bind(@NonNull MonthHeaderViewContainer monthHeaderViewContainer, @NonNull CalendarMonth calendarMonth) {
				zonedDateTime = zonedDateTime.withYear(calendarMonth.getYear());
				zonedDateTime = zonedDateTime.withMonth(calendarMonth.getMonth());
				monthHeaderViewContainer.binding.headerTextView.setText(zonedDateTime.format(dateTimeFormatter));

				if (monthHeaderViewContainer.binding.days.getTag() == null) {
					monthHeaderViewContainer.binding.days.setTag(Boolean.TRUE);
					int childCount = monthHeaderViewContainer.binding.days.getChildCount();
					for (int child = 0; child < childCount; child++) {
						((TextView) monthHeaderViewContainer.binding.days.getChildAt(child)).setText(
								dayOfWeeks[child].getDisplayName(TextStyle.SHORT, Locale.getDefault()));
					}
				}

			}
		});

		// 날짜표시와 밑에 파란색으로 약속 카운팅도 하기
		binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
			String dateText;
			List<ContentValues> eventList;
			List<ContentValues> attendeeList;
			int acceptedCount;
			int eventCount;

			@NonNull
			@Override
			public DayViewContainer create(@NonNull View view) {
				return new DayViewContainer(view);
			}

			@Override
			public void bind(@NonNull DayViewContainer viewContainer, @NonNull CalendarDay calendarDay) {
				acceptedCount = 0;
				eventCount = 0;
				dateText = calendarDay.getDate().toString();

				viewContainer.binding.calendarDayText.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));
				if (calendarDay.getOwner() == DayOwner.THIS_MONTH) {
					viewContainer.binding.calendarDayText.setTextColor(Color.BLACK);
				} else {
					viewContainer.binding.calendarDayText.setTextColor(Color.LTGRAY);
				}

				viewContainer.binding.calendarDayText.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getContext(), String.valueOf(calendarDay.getDate().getDayOfMonth()), Toast.LENGTH_SHORT).show();
					}
				});

				if (eventsMap.containsKey(dateText)) {
					eventList = eventsMap.get(dateText);
					eventCount = eventList.size();
					attendeeList = attendeesMap.get(dateText);

					for (ContentValues event : eventList) {
						if (attendeeList != null) {

							for (ContentValues attendee : attendeeList) {
								if (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(myEmail)) {
									if (attendee.getAsInteger(CalendarContract.Attendees.ATTENDEE_STATUS).equals(CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED)) {
										acceptedCount++;
									}

								}

							}

						}
					}

				}

				viewContainer.binding.acceptedPromiseCount.setText(acceptedCount > 0 ? String.valueOf(acceptedCount) : null);
				viewContainer.binding.eventCount.setText(eventCount > 0 ? String.valueOf(eventCount) : null);
			}
		});

		binding.calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {
			@Override
			public Unit invoke(CalendarMonth calendarMonth) {
				if ((calendarMonth.getYearMonth().compareTo(firstMonth) <= 3) || (calendarMonth.getYearMonth().compareTo(
						lastMonth) >= -3)) {
					binding.progressCircular.setVisibility(View.VISIBLE);

					firstMonth = firstMonth.minusMonths(10);
					lastMonth = lastMonth.plusMonths(10);
					binding.calendarView.updateMonthRangeAsync(firstMonth, lastMonth);
					refreshEvents();
				}
				return null;
			}
		});

		binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				binding.progressCircular.setVisibility(View.VISIBLE);
				syncCalendars();
			}
		});

		binding.calendarView.setup(firstMonth, lastMonth, FIRST_DAY_OF_WEEK);
		loadCalendar();
	}

	private void loadCalendar() {
		firstDateTime = ZonedDateTime.of(firstMonth.getYear(), firstMonth.getMonthValue(), 1, 0, 0, 0, 0, zoneId);
		endDateTime = ZonedDateTime.of(lastMonth.getYear(), lastMonth.getMonthValue(), 1, 0, 0, 0, 0, zoneId);

		CalendarRepository.loadCalendar(getContext(), accountViewModel.lastSignInAccount().getAccount(), new BackgroundCallback<ContentValues>() {
			@Override
			public void onResultSuccessful(ContentValues e) {
				calendar = e;
				refreshEvents();
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
	}

	@Override
	public void syncCalendars() {
		final Account account = accountViewModel.lastSignInAccount().getAccount();
		calendarViewModel.syncCalendars(account, new BackgroundCallback<Boolean>() {
			@SuppressLint("Range")
			@Override
			public void onResultSuccessful(Boolean e) {
				loadCalendar();
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
	}


	@Override
	public void refreshEvents() {
		CalendarRepository.loadAllEvents(getContext(), calendar.getAsString(CalendarContract.Calendars._ID), firstDateTime, endDateTime
				, new BackgroundCallback<List<ContentValues>>() {
					@Override
					public void onResultSuccessful(List<ContentValues> eventList) {
						CalendarRepository.loadAttendees(getContext(), firstDateTime, endDateTime, new BackgroundCallback<List<ContentValues>>() {
							@Override
							public void onResultSuccessful(List<ContentValues> attendeeList) {
								String dateText = null;
								ZonedDateTime eventDateTime = null;
								final ZoneId zoneId = firstDateTime.getZone();

								eventsMap.clear();
								attendeesMap.clear();

								for (ContentValues event : eventList) {
									eventDateTime =
											ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getAsLong(CalendarContract.Events.DTSTART)), zoneId);
									dateText = eventDateTime.toLocalDate().toString();

									if (!eventsMap.containsKey(dateText)) {
										eventsMap.put(dateText, new ArrayList<>());
									}
									eventsMap.get(dateText).add(event);
								}

								for (ContentValues attendee : attendeeList) {
									eventDateTime =
											ZonedDateTime.ofInstant(Instant.ofEpochMilli(attendee.getAsLong(CalendarContract.Attendees.DTSTART)), zoneId);
									dateText = eventDateTime.toLocalDate().toString();

									if (!attendeesMap.containsKey(dateText)) {
										attendeesMap.put(dateText, new ArrayList<>());
									}
									attendeesMap.get(dateText).add(attendee);
								}

								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										binding.refreshLayout.setRefreshing(false);
										binding.progressCircular.setVisibility(View.GONE);
										binding.calendarView.notifyCalendarChanged();
									}
								});
							}

							@Override
							public void onResultFailed(Exception e) {

							}
						});


					}

					@Override
					public void onResultFailed(Exception e) {

					}
				});
	}


	// 일 보여주는 컨테이너 바인딩
	private static class DayViewContainer extends ViewContainer {
		private CalendarDayLayoutBinding binding;

		public DayViewContainer(@NonNull View view) {
			super(view);
			binding = CalendarDayLayoutBinding.bind(view);

		}
	}

	// 달 보여주는 컨테이너 바인딩
	private static class MonthHeaderViewContainer extends ViewContainer {
		private CalendarMonthHeaderLayoutBinding binding;

		public MonthHeaderViewContainer(@NonNull View view) {
			super(view);
			this.binding = CalendarMonthHeaderLayoutBinding.bind(view);
		}
	}

}