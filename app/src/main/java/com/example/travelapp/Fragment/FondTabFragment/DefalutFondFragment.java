package com.example.travelapp.Fragment.FondTabFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DefalutFondFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefalutFondFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_defalut_fond, container, false);
    }

}