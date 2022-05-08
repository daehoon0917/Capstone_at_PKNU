package com.lifedawn.capstoneapp.map.places;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.CustomPlaceCategoryRepository;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.BottomSheetType;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.BottomSheetController;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AroundPlacesHeaderFragment extends AbstractSearchHeaderFragment {
	private CustomPlaceCategoryRepository customPlaceCategoryRepository;
	private LocationDto promiseLocationDto;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customPlaceCategoryRepository = new CustomPlaceCategoryRepository(getContext());

		if (bundle.containsKey("locationDto")) {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
			promiseLocationDto = (LocationDto) getArguments().getSerializable("locationDto");
		} else {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.header.fragmentTitle.setText(R.string.around_place);
		init();
	}

	@Override
	protected void init() {
		super.init();

		customPlaceCategoryRepository.getAll(new OnDbQueryCallback<List<CustomPlaceCategoryDto>>() {
			@Override
			public void onResult(List<CustomPlaceCategoryDto> customPlaceCategoryDtoList) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						List<String> placeCategoryList = new ArrayList<>();
						String[] placeCategoryArr = getResources().getStringArray(R.array.KakaoLocationitems);
						placeCategoryList.addAll(Arrays.asList(placeCategoryArr));

						for (CustomPlaceCategoryDto dto : customPlaceCategoryDtoList) {
							placeCategoryList.add(dto.getName());
						}
						Bundle bundle = null;
						AroundPlacesContentsFragment.PlaceFragment placeFragment = null;
						List<AroundPlacesContentsFragment.PlaceFragment> fragmentList = new ArrayList<>();

						for (String name : placeCategoryList) {
							TabLayout.Tab tab = binding.categoryTabLayout.newTab();
							tab.setContentDescription(name);
							tab.setText(name);

							binding.categoryTabLayout.addTab(tab);

							bundle = new Bundle();
							bundle.putString("category", name);
							bundle.putSerializable("locationDto", promiseLocationDto);

							placeFragment = new AroundPlacesContentsFragment.PlaceFragment(markerOnClickListener, onPoiItemClickListener,
									AroundPlacesHeaderFragment.this);

							placeFragment.setArguments(bundle);
							fragmentList.add(placeFragment);
						}

						createTabs(fragmentList, placeCategoryList);
					}
				});
			}
		});

	}


}